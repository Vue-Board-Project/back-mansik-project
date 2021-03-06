package com.mycompany.backend.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.LikeBoard;
import com.mycompany.backend.dto.Pager;

@Mapper
public interface BoardDao {
	public List<Board> selectByPage(Pager pager);
	public int count();
	public Board selectByBno(int bno);
	public int insert(Board board);
	public int deleteByBno(int bno);
	public int update(Board board);
	public int updateBhitcount(int bno);
	public List<Board> selectByPageCount(Pager pager);
	public List<Board> selectByPageSearch(Pager pager);
	public int countSearch(Pager pager);
	public int updateLike(int bno);
	public int cancelLike(int bno);
	public int upLikeInfo(int bno, String mid);
  public int downLikeInfo(int bno, String mid);
  public int selectLikeMidbno(int bno, String mid);
}

