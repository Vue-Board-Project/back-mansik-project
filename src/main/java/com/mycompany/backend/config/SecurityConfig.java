package com.mycompany.backend.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mycompany.backend.security.JwtAuthenticationFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
@EnableWebSecurity //configure 자동 실행
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
//  @Resource
//  private JwtAuthenticationFilter jwtAuthenticationFilter;
  
  @Resource
  private RedisTemplate redisTemplate;
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.info("실행");
    //서버 세션 비활성화
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    //폼 로그인(인증안되면 로그인 폼 뜨게 하는거, restApi는 로그인 폼 없음 제공하면 안됨)
    http.formLogin().disable();
    //사이트간 요청 위조 방지 비활성화
    http.csrf().disable();
    //요청 경로 권한 설정
    http.authorizeHttpRequests()
      .antMatchers("/board/**").authenticated() //authenticated() 인증된 사용자만, 로그인 필요
      .antMatchers("/mypage/**").authenticated()
      .antMatchers("/**").permitAll(); // 위에 빼고 나머지는 로그인 없이 사용가능!
    //CORS 설정(다른 도메인의 JavaScript로 접근을 할 수 있도록 허용)
    http.cors();
    //Jwt 인증 필터 추가, Form 인증 필터 앞에 추가해야함(UsernamePasswordAuthenticationFilter이 위치에서 인증이 되어야하기 때문에)
    //UsernamePasswordAuthenticationFilter 폼에서 id, password 읽어서 DB에서 확인작업 이거할떄 setUserDetailsService 이거 필요함
    //setUserDetailsService DB에 있는 사용자 정보 가져오는 역할
    //Jwt는 UsernamePasswordAuthenticationFilter 안함
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); 
  }
  
  //이렇게 만들면 JwtAuthenticationFilter에 Component Resource 붙일 필요없음
  //JwtAuthenticationFilter관ㄹ객체 아니기떄문에 여기서 Bean 붙여서 관리객체로 만들어줘야함
  //@Bean //여기 안에서만 쓸거면 관리객체로 안만들어도 됨
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();
    jwtAuthenticationFilter.setRedisTemplate(redisTemplate);
    return jwtAuthenticationFilter;
  }
  
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    log.info("실행");
    //Rest API는 폼 인증 안함
    //MPA폼 인증 방식에서 사용(JWT 인증 방식에서는 사용하지 않음)
    /*DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); //DB 접근
    provider.setUserDetailsService(new CustomUserDetailsService());
    provider.setPasswordEncoder(passwordEncoder());
    auth.authenticationProvider(provider);*/
    
  }
  
  @Override
  //권한 설정
  public void configure(WebSecurity web) throws Exception {
    log.info("실행");
    DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
    defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchyImpl());  // roleHierarchyImpl() 실행 아님, 이런 이름을 가지고 있는 객체를 넣어준다
    web.expressionHandler(defaultWebSecurityExpressionHandler);
    
   //MPA에서 시큐리티를 적용하지 않은 경로설정, 시큐리티 동작하지 않을 경로, 시큐리티 무시할 경로
   /*web.ignoring()
   .antMatchers("/images/**")
   .antMatchers("/css/**")
   .antMatchers("/js/**")
   .antMatchers("/bootstrap/**")
   .antMatchers("/jquery/**")
   .antMatchers("/favicon.ico");*/
    //resources, 프론트 url
  
  }

  @Bean //@Configuration이랑 같이 써야함, @EnableWebSecurity는 Configuration붙어있음
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    //return new BCryptPasswordEncoder();
  }
  
  @Bean //관리 객체
  //사용 1. 의존성 주입 2.매개 처럼 넣기, 메소드처럼 사용
  public RoleHierarchyImpl roleHierarchyImpl() {
     log.info("실행");
     RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
     roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
     return roleHierarchyImpl;
  }
  
  //Rest API에서만 사용
  @Bean
  //corsConfigurationSource 이름 똑같이 작성하기
  //CORS가 모지...?
  public CorsConfigurationSource corsConfigurationSource() {
    log.info("실행");
      CorsConfiguration configuration = new CorsConfiguration();
      //모든 요청 사이트 허용
      configuration.addAllowedOrigin("*");
      //모든 요청 방식 허용
      configuration.addAllowedMethod("*");//어떤 방식이든 다 허용하겠다, get,post,put,delete
      //모든 요청 헤더 허용
      configuration.addAllowedHeader("*");//어떤 헤더 명이든 다 받아주겠다
      //모든 URL 요청에 대해서 위 내용을 적용
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
  
}
