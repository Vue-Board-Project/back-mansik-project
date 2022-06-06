package com.mycompany.backend.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class Photo {
  int pno;
  int bno;
  String pname;
  String psname;
  String ptype;
  private MultipartFile battach;
}
