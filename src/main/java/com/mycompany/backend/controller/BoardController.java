package com.mycompany.backend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.Pager;
import com.mycompany.backend.service.BoardService;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/board")
public class BoardController {
	@Resource
	private BoardService boardService;
	
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
		log.info("실행");
		
		int totalRows = boardService.getTotalBoardNum();
		Pager pager = new Pager(8, 5, totalRows, pageNo);
		List<Board> list = boardService.getBoards(pager);
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		return map;
	}
	
	@GetMapping("/list/sort")
	public Map<String, Object> listChangeSort(@RequestParam(defaultValue = "1") int pageNo) {
		log.info("실행");
		
		int totalRows = boardService.getTotalBoardNum();
		Pager pager = new Pager(8, 5, totalRows, pageNo);
		List<Board> list = boardService.getBoardsByCount(pager);
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		return map;
	}
	
	@GetMapping("/list/search")
	public Map<String, Object> listSearchBar(@RequestParam(defaultValue = "1") int pageNo, String searchText, String searchOption, String sortOption) {
		log.info("실행");
		
		Pager pagerr = new Pager(8, 5, 0, pageNo);
		
		if(searchOption.equals("title")) {
			pagerr.setSearchOption("btitle");
		}else if(searchOption.equals("writer")){
			pagerr.setSearchOption("mid");
		}
		
		pagerr.setSearch(searchText);
		int totalRowsSearch = boardService.getTotalBoardNumSearch(pagerr);

		Pager pager = new Pager(8, 5, totalRowsSearch, pageNo);

		
		if(sortOption.equals("latest")) {
			pager.setSortOption("bno");
		}else if(sortOption.equals("count")){
			pager.setSortOption("bhitcount");
		}
		
		if(searchOption.equals("title")) {
			pager.setSearchOption("btitle");
		}else if(searchOption.equals("writer")){
			pager.setSearchOption("mid");
		}
		
		pager.setSearch(searchText);
		
		List<Board> list = boardService.getBoardsBySearch(pager);

		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		return map;
	}
	
	@PostMapping("/")
	public Board create(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() +"-"+ mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("C:/Temp/uploadfiles/"+board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				log.error(e.getMessage());
			} 
		}
		boardService.writeBoard(board);
		Board dbBoard = boardService.getBoard(board.getBno(), false);
		return dbBoard;
	}
	
	@PutMapping("/")
	public Board update(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() +"-"+ mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("C:/Temp/uploadfiles/"+board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				log.error(e.getMessage());
			} 
		}
		boardService.updateBoard(board);
		Board dbBoard = boardService.getBoard(board.getBno(), false);
		return dbBoard;
	} 
	
	//http://localhost/board/3
	@GetMapping("/{bno}")
	public Board read(@PathVariable int bno,@RequestParam(defaultValue = "false") boolean hit) {
		log.info("실행");
		Board board = boardService.getBoard(bno, hit);
		return board;
	}
	
	//@PathVariable 이렇게 넘기는 데이터는 다른사람이 봐도 중요하지 않은 정보들
	@DeleteMapping("/{bno}")
	public Map<String, String> delete(@PathVariable int bno) {
		log.info("실행");
		boardService.removeBoard(bno);
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		return map;
	}
	
	@GetMapping("/battach/{bno}")
	public ResponseEntity<InputStreamResource> download(@PathVariable int bno) throws Exception{
		Board board = boardService.getBoard(bno, false);
		String battachoname = board.getBattachoname();
		if(battachoname == null) return null;
		
		//파일 이름이 한글일 경우
		battachoname = new String(battachoname.getBytes("UTF-8"),"ISO-8859-1");
		
		//파일 입력 스트림 생성
		FileInputStream fis = new FileInputStream("C:/Temp/uploadfiles/"+board.getBattachsname());
		InputStreamResource resource = new InputStreamResource(fis);
		
		//응답 생성
		//CONTENT_DISPOSITION 실제 저장되는 파일 이름, 이거 안들어가면 다운 안되고 이미지파일만 브라우저에서 보여짐
		//\" : "
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+battachoname+"\";")
				.header(HttpHeaders.CONTENT_TYPE, board.getBattachtype())
				.body(resource);
		
	}
	@PutMapping("/{bno}")
  public Board updateLike(@PathVariable int bno) {
	  log.info("실행");
    boardService.updateLike(bno);
    Board dbBoard = boardService.getBoard(bno, false);
    return dbBoard;
	}
}
