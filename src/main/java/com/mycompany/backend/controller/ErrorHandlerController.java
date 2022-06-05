package com.mycompany.backend.controller;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class ErrorHandlerController implements ErrorController {
  
  @RequestMapping("/error")
  public ResponseEntity<String> error(HttpServletResponse response) {
    int status = response.getStatus();
    if(status == 404) {
      return ResponseEntity
          .status(HttpStatus.MOVED_PERMANENTLY)//HttpStatus.MOVED_PERMANENTLY: redirect의 응답코드 번호(301)
          .location(URI.create("/")) //redirect로 갈 URL
          .body("");
    } else if(status == 403) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)//403
          .body("403");
    } else {
      return ResponseEntity.status(status).body("");
    }
  }

}
