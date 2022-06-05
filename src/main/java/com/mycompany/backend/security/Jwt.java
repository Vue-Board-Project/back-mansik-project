package com.mycompany.backend.security;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Jwt { 
  //상수
  private static final String JWT_SECRET_KEY = "kosa12345"; //노출이 되면 안됨, 암호화 해주어야함
  private static final long ACCESS_TOKEN_DURATION = 1000*60*30; //30분
  public static final long REFRESH_TOKEN_DURATION =  1000*60*60*24; //24시간
  
  //AccessToken 생성
  public static String createAccessToken(String mid, String authority) { //아이디, 스프링시큐리티 권한
    log.info("실행");
    String accessToken = null;
    
    try {
      accessToken = Jwts.builder()
                          //헤더 설정
                          .setHeaderParam("alg", "HS256")
                          .setHeaderParam("typ", "JWT")
                          //페이로드 설정
                          .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_DURATION))//토큰 유효기간 설정
                          .claim("mid", mid)
                          .claim("authority", authority)
                          //서명 설정
                          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
                          //토큰 생성
                          .compact();
    } catch (UnsupportedEncodingException e) {
      //e.printStackTrace();
      log.error(e.getMessage());
    }
    return accessToken;
  }
  
//AccessToken 생성
  public static String createRefreshToken(String mid, String authority) { //아이디, 스프링시큐리티 권한
    log.info("실행");
    String refreshToken = null;
    
    try {
      refreshToken = Jwts.builder()
                          //헤더 설정
                          .setHeaderParam("alg", "HS256")
                          .setHeaderParam("typ", "JWT")
                          //페이로드 설정
                          .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_DURATION))//토큰 유효기간 설정
                          .claim("mid", mid)
                          .claim("authority", authority)
                          //서명 설정
                          .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY.getBytes("UTF-8"))
                          //토큰 생성
                          .compact();
    } catch (UnsupportedEncodingException e) {
      //e.printStackTrace();
      log.error(e.getMessage());
    }
    return refreshToken;
  }
  
  public static boolean validateToken(String token) {
    log.info("실행");
    log.info(token);
    boolean result = false;
    
    try {
      result = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))//서명키(비밀키)가 맞는지 확인
          .parseClaimsJws(token)//파싱 및 검증, 실패시 에러 //Claim 객체 리턴
          .getBody() //Claim 만의 Body 얻기 
          .getExpiration() //만료기간  
          .after(new Date()); //만료기간이 현재 시간보다 나중인지 -> 기간이 남았다면 true, 기간이 지났다면 false 
    } catch (Exception e) {
      log.info(e);
      //log.info(e.getMessage());
    }
    return result;
  }
  
  //토큰 만료 시간 얻기
  public static Date getExpiration(String token) {
    log.info("실행");
    Date result = null;
    
    try {
      result = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody()
          .getExpiration();
    } catch (Exception e) {
      log.info(e.getMessage());
    }
    return result;
  }
  
  //인증 사용자 정보 얻기
  public static Map<String, String> getUserInfo(String token){ //payload like claims
    log.info("실행");
    Map<String, String> result = new HashMap<>();
    
    try {
      Claims claims = Jwts.parser()
          .setSigningKey(JWT_SECRET_KEY.getBytes("UTF-8"))
          .parseClaimsJws(token)
          .getBody();
      result.put("mid", claims.get("mid",String.class));
      result.put("authority", claims.get("authority", String.class));
    } catch (Exception e) {
      log.info(e.getMessage());
    }
    return result;
  }
  
  //요청 Authorization 헤더 값에서 AccessToken 얻기
  //Bearer XXXXXXXXXXXXXXXXX.XXXXXXXXXXXXXXX.XXXXXXXXXXXXXXXx
  public static String getAccessToken(String authorization) {
    String accessToken = null;
    
    if(authorization != null && authorization.startsWith("Bearer ")) {
      accessToken = authorization.substring(7);
    }
    
    return accessToken;
  } 
  
  public static void main(String[] args) throws InterruptedException {
    String accessToken = createAccessToken("user", "ROLE_USER");
    Thread.sleep(6000);
    System.out.println(validateToken(accessToken));
    
    Date expiration = getExpiration(accessToken);
    System.out.println(expiration);
    
    Map<String,String> userInfo = getUserInfo(accessToken);
    System.out.println(userInfo);
  }
}
