package com.mycompany.backend.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mycompany.backend.dao.PhotoDao;
import com.mycompany.backend.dto.Photo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhotoService {
  
  @Resource
  PhotoDao photoDao;
  
  public void insertPhoto(Photo photo) {
    
    photoDao.insertPhoto(photo);
  }
  public List<Photo> selectPhoto(int bno){
    return photoDao.selectPhoto(bno);
  }
}
