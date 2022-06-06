package com.mycompany.backend.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mycompany.backend.dao.CommentDao;
import com.mycompany.backend.dto.Comment;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentService {
  
  @Resource
  CommentDao commentDao;
  
  public List<Comment> allComment(int bno) {
    
    return commentDao.selectBybno(bno);
  }
  
  public void createComment(Comment comment) {
    
    commentDao.insertComment(comment);
  }
  public void deleteComment(int cno) {
    commentDao.deleteComment(cno);
  }
  public void updateComment(Comment comment) {
    commentDao.updateComment(comment);
  }
  public Comment getComment(int cno) {
    return commentDao.getComment(cno);
  }
}
