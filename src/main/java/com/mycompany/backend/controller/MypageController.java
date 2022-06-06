package com.mycompany.backend.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.backend.dto.Member;
import com.mycompany.backend.service.MemberService;
import com.mycompany.backend.service.MemberService.UpdateMember;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/mypage")
public class MypageController {
  @Resource
  private MemberService memberService;
  
  @PostMapping("/memberInfo")
  public Map<String, Object> memberInfo(@RequestBody Member member) {
    log.info("실행");
    log.info(member.getMid());
    Member dbMember = memberService.getMember(member.getMid());
    log.info(dbMember);
    Map<String, Object> map = new HashMap<>();
    if(dbMember==null) {
      map.put("result", "fail");
      return map; 
    } else {
      map.put("result", "success");
      map.put("user", dbMember);
      return map;      
    }
  }
  
  @PutMapping("/updateMember")
  public Map<String, Object> updateMember(@RequestBody Member member){
    log.info("실행");
    log.info(member.getMid());
    Map<String, Object> map = new HashMap<>();
    UpdateMember um = memberService.memberUpdate(member);
    log.info(um);
    if(um==UpdateMember.FAIL) {
      map.put("result", "fail");
      return map;
    } else {
      map.put("result", "success");
      return map;  
    }
  }
}
