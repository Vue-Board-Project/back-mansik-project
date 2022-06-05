package com.mycompany.backend.security;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
//@Component //관리객체
//인증필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  //@Resource
  private RedisTemplate redisTemplate;
  public void setRedisTemplate(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    log.info("실행");
    //request 이걸로 요청 넘어옴
    
    //요청 헤더로부터 Authorization 헤더 값 얻기
    String authorization = request.getHeader("Authorization");
    
    //AccessToken 추출
    String accessToken = Jwt.getAccessToken(authorization);
    //log.info(accessToken);
    //검증 작업
    if(accessToken != null && Jwt.validateToken(accessToken)) {
      //Redis에 존재 여부 확인
      ValueOperations<String, String> vo = redisTemplate.opsForValue();
      String redisRefreshToken = vo.get(accessToken);
      
      if(redisRefreshToken != null) {
        //인증 처리
        Map<String, String> userInfo = Jwt.getUserInfo(accessToken);
        String mid = userInfo.get("mid");
        String authority = userInfo.get("authority");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
      }
    }  
    
    //다음 필터 실행
    filterChain.doFilter(request, response);//다음 필터 적용(실행)
    
  }

 

}
