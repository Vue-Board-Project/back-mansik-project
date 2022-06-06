package com.mycompany.backend.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.backend.dto.Photo;

@Mapper
public interface PhotoDao {
  
  public void insertPhoto(Photo photo);
  public List<Photo> selectPhoto(int bno);
}
