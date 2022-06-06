package com.mycompany.backend.dto;

import java.util.Date;

import lombok.Data;

@Data
public class Comment {
  int cno;
  int bno;
  String comments;
  Date cdate;
  String cuserid;

}
