package com.project.recoder.board.model.service;

import com.project.recoder.board.model.dao.BoardDAO;
import com.project.recoder.board.model.vo.Board;
import com.project.recoder.board.model.vo.BoardImg;
import com.project.recoder.board.model.vo.PageInfo;
import com.project.recoder.room.model.service.FileInsertFailedException;

import static com.project.recoder.common.JDBCTemplate.*;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardService {

	private BoardDAO dao = new BoardDAO();
	
	/** 페이징 처리를 위한 값 계산 Service
	 * @param cp
	 * @return pInfo
	 * @throws Exception
	 */
	public PageInfo getPageInfo(String cp) throws Exception{
		Connection conn = getConnection();
		
		int currentPage  = cp == null ? 1 : Integer.parseInt(cp);
		int listCount = dao.getListCount(conn);
		
		close(conn);
		
		return new PageInfo(currentPage, listCount);
	}

	/** 게시글 목록 조회 service
	 * @param pInfo
	 * @return bList
	 * @throws Exception
	 */
	public List<Board> selectBoardList(PageInfo pInfo) throws Exception{
		Connection conn = getConnection();
		
		List<Board> bList = dao.selectBoardList(conn, pInfo);
		
		close(conn);
		
		return bList;
	}

	/** 게시글 상세조회 service
	 * @param boardNo
	 * @return board
	 * @throws Exception
	 */
	public Board selectBoard(int boardNo) throws Exception{
		Connection conn = getConnection();
		
		Board board = dao.selectBoard(conn, boardNo);
		
		if(board != null) { //DB에서 조회 성공 시
			
			//조회수 증가
			int result = dao.increaseReadCount(conn, boardNo);
			
			if(result > 0) {
				commit(conn);
				
				//반환되는 Board 데이터에는 조회수가 증가되어있지 않기 때문에
				// 조회수 1 증가 시켜주기
				board.setReadCount(board.getReadCount()+1);
			}
			else {
				rollback(conn);
			}
				
			
		}
		
		close(conn);
		
		return board;
	}

	
	

	/** 게시글 작성 service
	 * @param map
	 * @return result
	 * @throws Exception
	 */
	public int insertBoard(Map<String, Object> map) throws Exception {
		Connection conn = getConnection();
		
		int result = 0;
		
		String boardContent = (String) map.get("boardContent");
		
		//이미지 태그들 이름 가져오는 service 실행
		List<String> imageNameList  = getImgName(boardContent);
		
        //서버 사진 저장 주소
		String path = "C:\\Users\\imsor\\workspace\\teamRepository\\recoder\\Recoder\\WebContent\\resources/images/boardImg/";
		
		int boardNo= dao.selectNextNo(conn);
		
		if(boardNo >0) {
			map.put("boardNo", boardNo);
		}
		
			try {
				//게시글 부분만 삽입****
				result = dao.insertBoard(conn, map); 
				List<BoardImg> bimgs = new ArrayList<BoardImg>();
				
				if(result > 0 && !imageNameList.isEmpty()) {
					result = 0;
					int index=0;
					
					//이미지 세팅..
					for(String imgname : imageNameList) {
						
						//이미지정보를 boardImg에 넣기
						BoardImg bImg = new BoardImg(path, imgname, index++, boardNo);		
						bimgs.add(bImg);
					}
						for(BoardImg b : bimgs) {
						result = dao.insertImgs(conn, b);
						
							if(result == 0) { 
								throw new FileInsertFailedException("파일 정보 삽입 실패");
							}
						}
					}
					
					
				}catch (Exception e) {
				
				if(!imageNameList.isEmpty()) {
					for(String img : imageNameList) {
						
						File deleteFile = new File(path + img);
						
						if(deleteFile.exists()) {
							deleteFile.delete();
						}
					}
				}
				
				throw e;
			}
		
		if(result > 0) {
			commit(conn);
			result = boardNo;
		}else {
			rollback(conn);
		}
		
		close(conn);
		
		return result;
	}
	
	/** 게시글 삭제
	 * @param boardNo
	 * @return result
	 * @throws Exception
	 */
	public int deleteBoard(int boardNo) throws Exception{
		Connection conn = getConnection();
		
		int result = dao.deleteBoard(conn, boardNo);
		
		if(result >0) { //게시글 삭제 됬을 때 디비, 서버에서 이미지 삭제
			result = dao.deleteBoardImg(conn, boardNo);
		}
		
		if(result > 0) commit(conn);
		else			rollback(conn);
		
		return result;
	}

	/** 게시글 수정 뷰 service
	 * @param boardNo
	 * @return board
	 * @throws Exception
	 */
	public Board updateView(int boardNo) throws Exception{
		Connection conn = getConnection();
		
		Board board = dao.updateView(conn, boardNo);
		
		close(conn);
		
		return board;
	}

	/** 게시글 수정 service
	 * @param map
	 * @return result
	 * @throws Exception
	 */
	public int updateBoard(Map<String, Object> map) throws Exception{
		Connection conn = getConnection();
		int result = 0;
		int boardNo = (int)map.get("boardNo");
		
		String boardContent = (String) map.get("boardContent");
		List<String> imageNameList  = getImgName(boardContent);
		
		
        //서버 사진 저장 주소
		String path = "C:\\Users\\imsor\\workspace\\teamRepository\\recoder\\Recoder\\WebContent\\resources/images/boardImg/";
		
		try {
			result = dao.updateBoard(conn, map); //디비에서 게시글 지우기
		
			if(result > 0) {//게시글 지우기 성공하면 이미지 지우기
				result = 0;
				result = dao.deleteBoardImg(conn, boardNo);
			}
		
			if(result > 0 && !imageNameList.isEmpty()) {//이미지 지우기 성공하면 이미지 새로 넣기
				result = 0;
				int index=0;
				
				List<BoardImg> bimgs = new ArrayList<BoardImg>();
				
			//이미지 세팅..
			for(String imgname : imageNameList) {
				
				//이미지정보를 boardImg에 넣기
				BoardImg bImg = new BoardImg(path, imgname, index++, boardNo);		
				bimgs.add(bImg);
			}
				for(BoardImg b : bimgs) {
				result = dao.insertImgs(conn, b);
					if(result == 0) { 
						throw new FileInsertFailedException("파일 정보 삽입 실패");
					}
				}
			}
			
			
		}catch (Exception e) {
		
		if(!imageNameList.isEmpty()) {
			for(String img : imageNameList) {
				
				File deleteFile = new File(path + img);
				
				if(deleteFile.exists()) {
					deleteFile.delete();
				}
			}
		}
		
		throw e;

		}
		if(result > 0) commit(conn);
		else			rollback(conn);
		
		return result;
	}
	
	
	/** 이미지 태그들 이름 가져오는 service
	 * @param boardContent
	 * @param imageList
	 * @return imageNameList
	 * @throws Exception
	 */
	public List<String> getImgName(String boardContent) throws Exception{
		List<String> imageNameList = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>"); // img 태그 src 추출 정규표현식
        Matcher matcher = pattern.matcher(boardContent);
        while (matcher.find()) {
            String src = matcher.group(1).toString();

            src = src.substring(src.lastIndexOf("/") + 1);
            
            imageNameList.add(src);
         }
        
		return imageNameList;
	}
	
}
