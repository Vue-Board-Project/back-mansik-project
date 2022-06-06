package com.mycompany.backend.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.backend.dto.Comment;

@Mapper
public interface CommentDao {
  
  public List<Comment> selectBybno(int bno);
  
  public void insertComment(Comment comment);
  
  public void deleteComment(int cno);
  public void updateComment(Comment comment);
  public Comment getComment(int cno);

}
