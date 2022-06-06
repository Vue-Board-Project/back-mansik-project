package com.mycompany.backend.service;

import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.HtmlEmail;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.backend.dao.MemberDao;
import com.mycompany.backend.dto.Member;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Log4j2
public class MemberService {
	//열거 타입 선언
	public enum JoinResult {
		SUCCESS,
		FAIL,
		DUPLICATED
	}
	public enum LoginResult {
		SUCCESS,
		FAIL_MID,
		FAIL_MPASSWORD,
		FAIL
	}
  //비번 찾기 결과
  public enum FindPWResult{
    SUCCESS, FAIL
  }
	
  //유저 ID와 이메일이 서로 맞는지
  public enum CheckID{
    SUCCESS, FAIL
  }
  
  //아이디 찾기 결과
  public enum FindIdResult{
    SUCCESS, FAIL
  }
  
  //아이디 찾기 결과
  public enum UpdateMember{
    SUCCESS, FAIL
  }
  
	@Resource
	private MemberDao memberDao;
	
	@Resource
  private PasswordEncoder passwordEncoder;
	
	//회원 가입을 처리하는 비즈니스 메소드(로직)
	public JoinResult join(Member member) {
		try {
			//이미 가입된 아이디인지 확인
			Member dbMember = memberDao.selectByMid(member.getMid()); 
			//log.info(dbMember.getMid());
			
			//DB에 회원 정보를 저장
			if(dbMember == null) {
				memberDao.insert(member);
				return JoinResult.SUCCESS;
			} else {
			  log.info("~~~~~");
				return JoinResult.DUPLICATED;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return JoinResult.FAIL;
		}
	}

	public LoginResult login(Member member) {
		try {
			//이미 가입된 아이디인지 확인
			Member dbMember = memberDao.selectByMid(member.getMid()); 
			PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			//확인 작업
			if(dbMember == null) {
				return LoginResult.FAIL_MID;
			} else if(!passwordEncoder.matches(member.getMpassword(), dbMember.getMpassword())) {
				return LoginResult.FAIL_MPASSWORD;
			} else {
				return LoginResult.SUCCESS;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return LoginResult.FAIL;
		}
	}
	
	public Member getMember(String mid) {
		return memberDao.selectByMid(mid);
	}
	
//이메일 발송
  public void sendmail(Member member, String div, String rpw) {
    log.info(member.getMemail());
    log.info(member.getMname());
    // Mail Server 설정
    String charSet = "utf-8";
    String hostSMTP = "smtp.gmail.com"; //네이버 이용시 smtp.naver.com
    String hostSMTPid = "o.molaire99@gmail.com";
    String hostSMTPpwd = "Omolaire123";

    // 보내는 사람 EMail, 제목, 내용
    String fromEmail = "o.molaire99@gmail.com";
    String fromName = "강아지방위대";
    String subject = "";
    String msg = "";

    if(div.equals("findPassword")) {
      subject = "강아지방위대 임시 비밀번호 입니다. 마이페이지  회원정보 수정에서 비밀번호를 수정해주세요";
      msg += "<div align='center' style='border:1px solid black; font-family:verdana'>";
      msg += "<h3 style='color: blue;'>";
      msg += member.getMname() + "님의 임시 비밀번호 입니다. 비밀번호를 변경하여 사용하세요.</h3>";
      msg += "<p>임시 비밀번호 : ";
      msg += rpw + "</p></div>";
    } else {
      subject = "강아지방위대 아이디입니다.";
      msg += "<div align='center' style='border:1px solid black; font-family:verdana'>";
      msg += "<h3 style='color: blue;'>";
      msg += member.getMname() + "님의 아이디입니다..</h3>";
      msg += "<p>아이디 : ";
      msg += rpw + "</p></div>";
    }

    // 받는 사람 E-Mail 주소
    String mail = member.getMemail();
    try {
      HtmlEmail email = new HtmlEmail();
      email.setDebug(true);
      email.setCharset(charSet);
      email.setSSL(true);
      email.setHostName(hostSMTP);
      email.setSmtpPort(465); //네이버 이용시 587

      email.setAuthentication(hostSMTPid, hostSMTPpwd);
      email.setTLS(true);
      email.addTo(mail, charSet);
      email.setFrom(fromEmail, fromName, charSet);
      email.setSubject(subject);
      email.setHtmlMsg(msg);
      email.send();
    } catch (Exception e) {
      System.out.println("메일발송 실패 : " + e);
    }
  }
  
  //비밀번호 찾기
  @Transactional
  public FindPWResult findPW(Member member) throws Exception {

    //response.setContentType("text/html;charset=utf-8");
    //PrintWriter out = response.getWriter();
    log.info(member.getMemail());
    // 가입된 이메일이 없으면
    if(memberDao.selectByEmail(member.getMemail()) == null) {
      return FindPWResult.FAIL;
    } else {
      Member usersDto = memberDao.selectByEmail(member.getMemail());
      log.info(usersDto.getMname());
      log.info("임시 비밀번호 생성");
      // 임시 비밀번호 생성
      String pw = "";
      for (int i = 0; i < 12; i++) {
        pw += (char) ((Math.random() * 26) + 97);
      }
      //암호화 되기전 비번
      String realPW = pw; 
      PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      member.setMpassword(passwordEncoder.encode(realPW));

      // 비밀번호 변경
      memberDao.updatePW(member);
      // 비밀번호 변경 메일 발송
      sendmail(usersDto, "findPassword", realPW);

      return FindPWResult.SUCCESS;
    }
  }
  //이메일로 유저 정보 가져오기
  public Member selectByEmail(String memail) {
    return memberDao.selectByEmail(memail);
  }
  
  //아이디와 이메일 정보가 같은지
  public CheckID checkID(Member member) {
    Member dbMember = memberDao.selectByMid(member.getMid()); 
    if(dbMember == null) {
      return CheckID.FAIL;
    } else {
      if(dbMember.getMemail().equals(member.getMemail())) {
        return CheckID.SUCCESS;
      } else {
        return CheckID.FAIL;
      }      
    }
  }
  
  //아이디찾기
  public FindIdResult findId(Member member) {
    Member dbEmail = memberDao.selectByEmail(member.getMemail());
    if(dbEmail == null) {
      return FindIdResult.FAIL;
    } else {
      sendmail(dbEmail, "findId", dbEmail.getMid());
      return FindIdResult.SUCCESS;
    }
  }
  
  //회원정보수정
  @Transactional
  public UpdateMember memberUpdate(Member member) {
    log.info(member.getMid());
    if(memberDao.selectByMid(member.getMid()) == null) {
      return UpdateMember.FAIL;
    } else {
      Member dbMember = member;
      log.info(dbMember.getMid());
      PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      dbMember.setMpassword(passwordEncoder.encode(dbMember.getMpassword()));
      memberDao.updateMember(dbMember);
      return UpdateMember.SUCCESS;
    }
  }
}








