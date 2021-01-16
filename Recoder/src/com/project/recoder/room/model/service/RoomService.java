package com.project.recoder.room.model.service;

import static com.project.recoder.common.JDBCTemplate.*;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.project.recoder.room.model.dao.RoomDAO;
import com.project.recoder.room.model.vo.Room;
import com.project.recoder.room.model.vo.RoomImg;

public class RoomService {
	
	private RoomDAO dao = new RoomDAO();

	/** 매물 삽입
	 * @param map
	 * @return result
	 * @throws Exception
	 */
	public int roomInsert(Map<String, Object> map) throws Exception{
		Connection conn = getConnection();
		
		int result = 0;
		
		int roomNo = dao.selectNextNo(conn);
		
		if(roomNo > 0) {
			
			map.put("roomNo", roomNo);
			
			map.put("roomTitle", replaceParameter((String)map.get("roomTitle")));
			map.put("roomInfo", replaceParameter((String)map.get("roomInfo")));
			
			try {
				result = dao.roomInsert(conn, map);
				List<RoomImg> mList = (List<RoomImg>)map.get("mList");
				if(result > 0 && !mList.isEmpty()) {
					 
					result = 0; 
					
					for(RoomImg img : mList) {
						
						img.setParentRoomNo(roomNo);
						result = dao.insertImg(conn, img);
						
						if(result == 0) { 
							throw new FileInsertFailedException("파일 정보 삽입 실패");
							
						}
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
				List<RoomImg> mList = (List<RoomImg>)map.get("mList");
				if (!mList.isEmpty()) {
					for(RoomImg img : mList) {
						String filePath = img.getRoomImgPath();
						String fileName = img.getRoomImgName();
						
						File deleteFile = new File(filePath + fileName);
						
						if (deleteFile.exists()) {
							deleteFile.delete(); 
							
						}
					}
				}
				
				throw e;
			} 
			
			if(result > 0) {
				commit(conn);
				
				result = roomNo;
				
			}else {
				rollback(conn);
			}
		
		}
		close(conn);
		
		return result;
	}

	
	private String replaceParameter(String param) {
		
		String result = param;

		if(param != null) {
			result = result.replaceAll("&", "&amp;");
			result = result.replaceAll("<", "&lt;");
			result = result.replaceAll(">", "&gt;");
			result = result.replaceAll("\"", "&quot;");
		}
		return result;
	}

	public Room updateView(int roomNo) throws Exception {
		Connection conn = getConnection();
		Room room = dao.selectRoom(conn, roomNo);
		room.setRoomInfo(room.getRoomInfo().replaceAll("<br>", "\r\n"));
		close(conn);
		return room;
	}
	

	public int roomUpdate(Map<String, Object> map) throws Exception {
		Connection conn = getConnection();
		int result = 0;
		
		
		System.out.println(map);
		List<RoomImg> deleteFiles = null; 
		
		map.put("roomTitle", replaceParameter((String)map.get("roomTitle")));
		map.put("roomInfo", replaceParameter((String)map.get("roomInfo")));
		try {
			result = dao.roomUpdate(conn, map);
			List<RoomImg> newImgList = (List<RoomImg>)map.get("mList");
			System.out.println(newImgList);
			if(result > 0 && !newImgList.isEmpty()) {
				
				List<RoomImg> oldImgList = dao.selectRoomImg(conn, (int)map.get("boardNo"));
				
				result = 0; // result 재활용
				deleteFiles = new ArrayList<RoomImg>();
				
				
				for(RoomImg newFile: newImgList) {
					
					// flag가 false인 경우 : 새 이미지와 기존 이미지의 파일 레벨이 중복되는 경우 -> update
					// flag가 true인 경우 : 새 이미지와 기존 이미지의 파일 레벨이 중복되지 않은 경우 -> insert
					boolean flag = true;
					
					// 기존 이미지 정보 반복 접근
					for (RoomImg oldFile: oldImgList) {
						
						// 새로운 이미지와 기존 이미지의 파일레벨이 동일한 파일이 있다면 
						if(newFile.getRoomImgLevel() == oldFile.getRoomImgLevel()	) {
							
							// 기존 파일을 삭제 List에 추가
							deleteFiles.add(oldFile);
							
							// 새 이미지 정보에 이전 파일 번호를 추가 -> 파일 번호를 이용한 수정 진행
							newFile.setRoomImgNo(oldFile.getRoomImgNo());
							
							flag = false;
						
							break;
						}
						
					}
					
					// flag 값에 따라 파일 정보 insert 또는 update 수행
					if(flag) {
						result = dao.insertImg(conn, newFile);
					}else {
						result = dao.updateImgFile(conn, newFile);
					}
					
					// 파일 정보 삽입 또는 수정 중 실패 시 
					if(result == 0) {
						// 강제로 사용자 정의 예외 발생
						throw new FileInsertFailedException("사진 정보 삽입 또는 수정 실패");
					}
					
				}
				
			}
			
		}catch (Exception e) {
			List<RoomImg> mList = (List<RoomImg>)map.get("mList");
			
			System.out.println(mList);
			
			if (!mList.isEmpty()) {
				for(RoomImg img : mList) {
					String filePath = img.getRoomImgPath();
					String fileName = img.getRoomImgName();
					
					File deleteFile = new File(filePath + fileName);
					
					if (deleteFile.exists()) {
						deleteFile.delete(); 
						
					}
				}
			}
			
			throw e;
		}
		
				 
		result = dao.roomUpdate(conn, map);
		
			if(result > 0) {
				commit(conn);
				
				// DB 정보와 맞지 않는 파일 (deleteFiles) 삭제 진행
				if(deleteFiles != null) {
					for(RoomImg img : deleteFiles) {
						
						String filePath = img.getRoomImgPath();
						String fileName = img.getRoomImgName();
						
						File deleteFile = new File(filePath + fileName);
						
						if(deleteFile.exists()) {
							deleteFile.delete();
						}
					}
				}
			} else {
				rollback(conn);
			}
		
		close(conn);
		return result;
	}


	public int updateRoomStatus(int roomNo) throws Exception {
		Connection conn = getConnection();
		int result =0;
		
		result = dao.updateRoomStatus(conn, roomNo);
		
		if (result > 0) {
			commit(conn);
		} else {
			rollback(conn);
		}
		
		close(conn);
		
		return result;
	}


	public Room selectRoom(int roomNo) throws Exception{
		
		Connection conn = getConnection();
		Room room = dao.selectRoom(conn, roomNo);
		
		
		if(room != null) {
			commit(conn);
		}else {
			rollback(conn);
		}
			      
		close(conn);
		return room;
	}
	
	public List<RoomImg> selectRoomImg(int roomNo) throws Exception {
		Connection conn = getConnection();
		List<RoomImg> mList = dao.selectRoomImg(conn, roomNo);
		close(conn);
		return mList;
	}




}
