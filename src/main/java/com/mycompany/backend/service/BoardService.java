package com.mycompany.backend.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mycompany.backend.dao.BoardDao;
import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.LikeBoard;
import com.mycompany.backend.dto.Pager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardService {
	
	@Resource
	private BoardDao boardDao;
	
  //추천 중복 체크
  public enum LikeInfo{
    UP, DOWN, FAIL
  }
  
	public List<Board> getBoards(Pager pager) {
		log.info("실행");
		return boardDao.selectByPage(pager);
	}
	
	public Board getBoard(int bno, boolean hit) {
		log.info("실행");
		if(hit) {
			boardDao.updateBhitcount(bno);
		}
		return boardDao.selectByBno(bno);
	}
	
	public int getTotalBoardNum() {
		log.info("실행");
		return boardDao.count();
	}
	
	public void writeBoard(Board board) {
		log.info("실행");
		boardDao.insert(board);
	}
	
	public void updateBoard(Board board) {
		log.info("실행");
		boardDao.update(board);
	}
	
	public void removeBoard(int bno) {
		log.info("실행");
		boardDao.deleteByBno(bno);
	}

	public List<Board> getBoardsByCount(Pager pager) {
		return boardDao.selectByPageCount(pager);
	}

	public List<Board> getBoardsBySearch(Pager pager) {
		return boardDao.selectByPageSearch(pager);
	}

	public int getTotalBoardNumSearch(Pager pager) {
		return boardDao.countSearch(pager);
	}
	
	//추천 수 증가
	public int updateLike(int bno) {
	  return boardDao.updateLike(bno);
	}
	
	public int cancelLike(int bno) {
	  return boardDao.cancelLike(bno);
	}
	
	public int upLikeInfo(int bno, String mid) {
	  return boardDao.upLikeInfo(bno, mid);
	}
	
	 public int downLikeInfo(int bno, String mid) {
	   return boardDao.downLikeInfo(bno, mid);
	  }
	//추천 중복 확인
	public int likeCheck(int bno, String mid) {
    return boardDao.selectLikeMidbno(bno, mid);
  }

}








