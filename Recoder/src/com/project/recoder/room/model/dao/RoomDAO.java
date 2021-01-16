package com.project.recoder.room.model.dao;

import static com.project.recoder.common.JDBCTemplate.*;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.project.recoder.room.model.vo.Room;
import com.project.recoder.room.model.vo.RoomImg;

public class RoomDAO {
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rset = null;
	
	private Properties prop = null;
	
	public RoomDAO() {
		String fileName = RoomDAO.class.getResource("/com/project/recoder/sql/room/room-query.xml").getPath();
		try {
			prop = new Properties();
			prop.loadFromXML(new FileInputStream(fileName)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 다음 게시글 번호 조회
	 * @param conn
	 * @return result
	 * @throws Exception
	 */
	public int selectNextNo(Connection conn) throws Exception{
		int result = 0;
		String query = prop.getProperty("selectNextNo");
		
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(query);
			if(rset.next()) {
				result = rset.getInt(1);
			}
			
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return result;
		
	}

	/** 매물 삽입 DAO
	 * @param conn
	 * @param map
	 * @return result
	 * @throws Exception
	 */
	public int roomInsert(Connection conn, Map<String, Object> map) throws Exception{
		int result = 0;
		String query = prop.getProperty("roomInsert");
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, (int)map.get("roomNo"));
			pstmt.setString(2, (String) map.get("roomAddr"));
			pstmt.setString(3, (String) map.get("typeOfRent"));
			
			pstmt.setInt(4, (int) map.get("deposit"));
			pstmt.setInt(5, (int) map.get("monthRent"));
			
			pstmt.setInt(6, (int) map.get("careFee"));
			pstmt.setInt(7, (int) map.get("pubSize"));
			pstmt.setInt(8, (int) map.get("realSize"));
			pstmt.setString(9, (String) map.get("roomFloor"));
			pstmt.setString(10, (String) map.get("roomStruc"));
			
			pstmt.setString(11, (String) map.get("roomCount"));
			pstmt.setString(12, (String) map.get("tv"));
			pstmt.setString(13, (String) map.get("internet"));
			pstmt.setString(14, (String) map.get("airCon"));
			pstmt.setString(15, (String) map.get("washing"));
			
			pstmt.setString(16, (String) map.get("fridge"));
			pstmt.setString(17, (String) map.get("bed"));
			pstmt.setString(18, (String) map.get("closet"));
			pstmt.setString(19, (String) map.get("womanOnly"));
			pstmt.setString(20, (String) map.get("pet"));
			
			pstmt.setString(21, (String) map.get("parking"));
			pstmt.setString(22, (String) map.get("roomTitle"));
			pstmt.setString(23, (String) map.get("roomInfo"));
			pstmt.setString(24, (String) map.get("stationAddr"));
			pstmt.setInt(25, (int)map.get("roomBroker")); // 회원번호

			
			result = pstmt.executeUpdate();
		} finally{
			close(pstmt);
		}
		return result;
	}

	/** 사진 파일 삽입 DAO
	 * @param conn
	 * @param img
	 * @return result
	 * @throws Exception
	 */
	public int insertImg(Connection conn, RoomImg img) throws Exception{
		int result=0;
		String query = prop.getProperty("insertImg");
		
		try {
			pstmt = conn.prepareStatement(query);
			
			pstmt.setString(1, img.getRoomImgName());
			pstmt.setString(2, img.getRoomImgPath());
			pstmt.setInt(3, img.getRoomImgLevel());
			pstmt.setInt(4, img.getParentRoomNo());
			
			result = pstmt.executeUpdate();
			
		} finally {
			close(pstmt);
		}
		
		return result;
	}

	public int updateRoomStatus(Connection conn, int roomNo) throws Exception{
		int result = 0;
		String query = prop.getProperty("updateRoomStatus");
		
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, roomNo);
			
			result = pstmt.executeUpdate();
			
		} finally {
			close(pstmt);
		}
		return result;
	}

	public Room selectRoom(Connection conn, int roomNo) throws Exception{
		Room room = null;
		String query = prop.getProperty("selectRoom");
		
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, roomNo);
			
			rset = pstmt.executeQuery();
			
			if (rset.next()) {
				room = new Room();
				
				room.setRoomNo(rset.getInt("ROOM_NO"));
				room.setRoomAddr(rset.getString("ROOM_ADDR"));
				room.setTypeOfRent(rset.getString("TYPE_OF_RENT"));
				room.setDeposit(rset.getInt("DEPOSIT"));
				room.setMonthRent(rset.getInt("MONTH_RENT"));
				room.setCareFee(rset.getInt("CARE_FEE"));
				room.setPubSize(rset.getInt("PUB_SIZE"));
				room.setRealSize(rset.getInt("REAL_SIZE"));
				room.setRoomFloor(rset.getString("ROOM_FLOOR"));
				room.setRoomStruc(rset.getString("ROOM_STRUC"));
				
				room.setRoomCount(rset.getInt("ROOM_COUNT"));
				room.setTv(rset.getString("TV"));
				room.setInternet(rset.getString("INTERNET"));
				room.setAirCon(rset.getString("AIR_CON"));
				room.setWashing(rset.getString("WASHING"));
				room.setFridge(rset.getString("FRIDGE"));
				room.setBed(rset.getString("BED"));
				room.setCloset(rset.getString("CLOSET"));
				room.setWomanOnly(rset.getString("WOMEN_ONLY"));
				room.setPet(rset.getString("PET"));
				
				room.setParking(rset.getString("PARKING"));
				room.setRoomTitle(rset.getString("ROOM_TITLE"));
				room.setRoomInfo(rset.getString("ROOM_INFO"));
				room.setStationAddr(rset.getString("STATION_ADDR"));
				room.setgMemNo(rset.getInt("GMEM_NO"));
				
				
				
			}
			
			
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return room;
	}

	public List<RoomImg> selectRoomImg(Connection conn, int roomNo) throws Exception{
		List<RoomImg> mList = null;
		String query = prop.getProperty("selectRoomImg");
		
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, roomNo);
			
			rset = pstmt.executeQuery();
			
			mList = new ArrayList<RoomImg>();
			while (rset.next()) {
				RoomImg rm = new RoomImg(
						rset.getInt("ROOM_IMG_NO"), 
						rset.getString("ROOM_IMG_NAME"), 
						rset.getInt("ROOM_IMG_LEVEL"));
				
						rm.setRoomImgPath(rset.getNString("ROOM_IMG_PATH"));
						
				mList.add(rm);
			}
			
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return mList;
	}

	public int roomUpdate(Connection conn, Map<String, Object> map) throws Exception{
		// TODO Auto-generated method stub
		return 0;
	}

	public int insertImgFile(Connection conn, RoomImg newFile) throws Exception{
		int result = 0;
		String query = prop.getProperty("insertImgFile");
		
		try {
			
	
			pstmt = conn.prepareStatement(query);
			
			result = pstmt.executeUpdate();
		}finally {
			close(pstmt);
		}
		return result;
	}

	public int updateImgFile(Connection conn, RoomImg newFile) throws Exception {
		int result = 0;
		String query = prop.getProperty("updateAttachment");
		
				
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, newFile.getRoomImgPath());
			pstmt.setString(2, newFile.getRoomImgName());
			pstmt.setInt(3,newFile.getRoomImgNo());
			
			result = pstmt.executeUpdate();
		} finally {
			close(pstmt);
		}
		
		return result;
	}

	
}
