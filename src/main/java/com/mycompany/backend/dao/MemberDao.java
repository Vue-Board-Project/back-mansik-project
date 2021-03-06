package com.mycompany.backend.dao;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.backend.dto.Member;

@Mapper
public interface MemberDao {
	public int insert(Member member);	
	public Member selectByMid(String mid);
  public Member selectByEmail(String memail);
  public void updatePW(Member member);
  public void updateMember(Member member);
}
