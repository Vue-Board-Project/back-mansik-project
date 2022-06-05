package com.mycompany.backend.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.backend.dto.Member;
import com.mycompany.backend.service.MemberService;
import com.mycompany.backend.service.MemberService.CheckID;
import com.mycompany.backend.service.MemberService.FindIdResult;
import com.mycompany.backend.service.MemberService.FindPWResult;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/member")
@Log4j2
public class FindPasswordController {
	
  @Resource
  private MemberService memberService;
	
	//비번찾기 인풋값 post
	@PostMapping("/findPassword")
	public Map<String, Object> findPassword(@RequestBody Member member) {
		log.info("실행");
		log.info(member.getMemail());
		//og.info(member.getMname());
		Map<String, Object> map = new HashMap<>();
		
		CheckID ci = memberService.checkID(member);
		if(ci == CheckID.SUCCESS) {
		  try {
		    FindPWResult fr = memberService.findPW(member);
		    if(fr == FindPWResult.FAIL) {
		      log.info("등록되지 않은 이메일입니다.");
		      map.put("result", "fail");
		      //model.addAttribute("errorPW", );
		      return map;
		    } else {
		      log.info("이메일로 임시 비밀번호를 발송하였습니다.");
		      //model.addAttribute("successPW", );
		      map.put("result", "success");
		      return map;
		    }
		    
		  } catch (Exception e) {
		    //e.printStackTrace();
		    map.put("result", "fail");
		    return map;
		  }		  
		} else {
		  map.put("result", "id or email fail");
		  return map;
		}
	}
	
	@PostMapping("/findId")
	public Map<String, Object> findId(@RequestBody Member member){
	  Map<String, Object> map = new HashMap<>();
	  FindIdResult fr = memberService.findId(member);
	  if(fr == FindIdResult.SUCCESS) {
	    map.put("result", "success");
      return map;
	  } else {
	    map.put("result", "fail");
      return map;
	  }
	}

}
