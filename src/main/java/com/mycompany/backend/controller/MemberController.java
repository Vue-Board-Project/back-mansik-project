package com.mycompany.backend.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.mycompany.backend.dto.Member;
import com.mycompany.backend.security.Jwt;
import com.mycompany.backend.service.MemberService;
import com.mycompany.backend.service.MemberService.JoinResult;
import com.mycompany.backend.service.MemberService.LoginResult;

import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/member")
public class MemberController {
  
  @Resource
  private MemberService memberService;
  
  @Resource
  private PasswordEncoder passwordEncoder;
  
  @Resource
  private RedisTemplate<String, String> redisTemplate;
  
  @PostMapping("/join")
  public Map<String, Object> join(@RequestBody Member member){//@RequestBody 요청할때 json으로 요청해야 파싱됨
    //계정 활성화
    member.setMenabled(true);
    //패스워드 암호화
    member.setMpassword(passwordEncoder.encode(member.getMpassword()));
    //회원 가입 처리
    JoinResult joinResult = memberService.join(member);
    log.info(joinResult);
    
    //응답 내용 설정
    Map<String, Object> map = new HashMap<>();
    if(joinResult==JoinResult.SUCCESS) {
      map.put("result", "success");
      map.put("member", member);
    }
    else if(joinResult==JoinResult.DUPLICATED) {
      map.put("result", "duplicated");
    }else {
      map.put("result", "fail");
    }
   
    return map;
  }
  
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody Member member){
    log.info("실행");
    
    //mid와 mpassword가 없을 경우
    //최종 프로젝트에는 이 방법보다는 validate를 사용해서 유효성 검사 정확하게 진행할 것
    if(member.getMid()==null||member.getMpassword()==null) {
      return ResponseEntity.status(401)//401err=인증되지 않은 데이터로 인해 발생하는 에러, 403=권한 에러
          .body("mid or mpassword cannot be null");
    }
    
    //로그인 결과 얻기
    LoginResult loginResult=memberService.login(member);
    if(loginResult!=LoginResult.SUCCESS) {
      return ResponseEntity.status(401)//401 id,password 잘못됨, 인증에 문제가 있음
          .body("mid or mpassword is wrong");
    }
    Member dbMember=memberService.getMember(member.getMid());
    String accessToken=Jwt.createAccessToken(member.getMid(), dbMember.getMrole());
    String refreshToken=Jwt.createRefreshToken(member.getMid(), dbMember.getMrole());
    
    //Redis에 저장
    ValueOperations<String, String> vo = redisTemplate.opsForValue();
    vo.set(accessToken, refreshToken,Jwt.REFRESH_TOKEN_DURATION , TimeUnit.MILLISECONDS);
    //key : accessToken
    //value : refreshToken
    //저장기간(만료기간) : refreshToken만료기간과 동일하게
    //단위
    
    //Cookie 생성
    String refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
      .httpOnly(true)//클라이언트에서 js 접근 불가능,document.cookie와 같은 자바스크립트로 쿠키를 조회하는 것을 막는 옵션
      .secure(false)//true=> https만 가능, false=>http와 https 모두 가능
      .path("/")//어떤 api더라도 가능하도록 공통 경로
      .maxAge(Jwt.REFRESH_TOKEN_DURATION/1000)//쿠키가 살아있는 시간(=토큰의 만료 시간), 초단위로 변환
      .domain("localhost")
      .build()
      .toString();
    
    //본문 생성, accesstoken과 id를 클라이언트에 전달하기 위함
    String json=new JSONObject()
        .put("accessToken", accessToken)
        .put("mid", member.getMid())
        .toString();
    
    //응답 설정
    return ResponseEntity 
        //응답 상태 코드 : 200
        .ok()
        //응답 헤더 추가
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)//응답 헤더 추가
        .header(HttpHeaders.SET_COOKIE, "application/json")
        //응답 body 추가
        .body(json);
  }
  
  @GetMapping("/refreshToken")
  public ResponseEntity<String> refreshToken(
      @RequestHeader("Authorization") String authorization,
      @CookieValue("refreshToken") String refreshToken
      ){
    //AccessToken 얻기
    String accessToken = Jwt.getAccessToken(authorization);
    if(accessToken == null) {
      return ResponseEntity.status(401).body("no access token");
    }
    
    //RefreshToken 여부
    if(refreshToken == null) {
      return ResponseEntity.status(401).body("no refresh token");
    }
    
    //동일한 토큰인지 확인
    ValueOperations<String, String> vo = redisTemplate.opsForValue();
    String redisRefreshToken = vo.get(accessToken);
    if(redisRefreshToken == null) {
      return ResponseEntity.status(401).body("invalidate access token");
    }
    if(!refreshToken.equals(redisRefreshToken)) {
      return ResponseEntity.status(401).body("invalidate refresh token");
    }
    
    //새로운 AccessToken 생성
    Map<String, String> userInfo = Jwt.getUserInfo(refreshToken);
    String mid = userInfo.get("mid");
    String authority = userInfo.get("authority");
    String newAccessToken = Jwt.createAccessToken(mid, authority);
    
    //Redis에 저장되어 있는 정보 변경 새로운 accessToken으로
    //Redis에 저장된 기존 정보를 삭제
    redisTemplate.delete(accessToken);
    
    //Redis에 새로운 정보를 저장
    //vo.set(accessToken, refreshToken,Jwt.REFRESH_TOKEN_DURATION , TimeUnit.MILLISECONDS);
    //이렇게 하면 안됨 refreshToken은 새로 생성되지않기때문에 만료기간에서 남은 시간만 넣어주어야함
    Date expiration = Jwt.getExpiration(refreshToken);
    vo.set(newAccessToken, refreshToken, expiration.getTime()- new Date().getTime(), TimeUnit.MILLISECONDS);
    
    //응답 설정
    String json = new JSONObject().put("accessToken", newAccessToken)
                                            .put("mid", mid)
                                            .toString();
    
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .body(json);
  }
  
  @GetMapping("/logout")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorization) {
    //AccessToken 얻기
    String accessToken = Jwt.getAccessToken(authorization);
    //if(accessToken == null || !Jwt.validateToken(accessToken)) {
    if(accessToken == null) {
      return ResponseEntity.status(401).body("invalide access token");
    }
    
    //Redis에 저장된 인증 정보를 삭제
    redisTemplate.delete(accessToken);
    
    //RefreshToken 쿠키 삭제
    String refreshTokenCookie = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(false)//true=> https만 가능, false=>http와 https 모두 가능
        .path("/")//어떤 api더라도 가능하도록 공통 경로
        .maxAge(0)//쿠키가 살아있는 시간(=토큰의 만료 시간), 초단위로 변환
        .domain("localhost")
        .build()
        .toString();
    
    //응답설정
    return ResponseEntity.ok()
                          .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
                          .body("success");
    
  }
  
  
   
}
