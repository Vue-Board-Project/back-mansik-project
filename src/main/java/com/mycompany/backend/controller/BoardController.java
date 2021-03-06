package com.mycompany.backend.controller;

import java.io.File;
import java.io.FileInputStream;
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
import com.mycompany.backend.dto.Comment;
import com.mycompany.backend.dto.Pager;
import com.mycompany.backend.dto.Photo;
import com.mycompany.backend.service.BoardService;
import com.mycompany.backend.service.CommentService;
import com.mycompany.backend.service.PhotoService;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/board")
public class BoardController {
	@Resource
	private BoardService boardService;
	@Resource
	 private CommentService commentService;
	@Resource
	private PhotoService photoService;
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
	
		
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
		
		Board board = boardService.getBoard(bno, hit);
		return board;
	}
	
	//@PathVariable ????????? ????????? ???????????? ??????????????? ?????? ???????????? ?????? ?????????
	@DeleteMapping("/{bno}")
	public Map<String, String> delete(@PathVariable int bno) {
	
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
		
		//?????? ????????? ????????? ??????
		battachoname = new String(battachoname.getBytes("UTF-8"),"ISO-8859-1");
		
		//?????? ?????? ????????? ??????
		FileInputStream fis = new FileInputStream("C:/Temp/uploadfiles/"+board.getBattachsname());
		InputStreamResource resource = new InputStreamResource(fis);
		
		//?????? ??????
		//CONTENT_DISPOSITION ?????? ???????????? ?????? ??????, ?????? ??????????????? ?????? ????????? ?????????????????? ?????????????????? ?????????
		//\" : "
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+battachoname+"\";")
				.header(HttpHeaders.CONTENT_TYPE, board.getBattachtype())
				.body(resource);
		
	}

  @GetMapping("/comment/{bno}")
  public Map<String,Object> searchComment(@PathVariable int bno){
    
    List<Comment> comment=commentService.allComment(bno);
 
    Map<String,Object> map=new HashMap<>();
    map.put("comment", comment);
    
    return map;
    
  }
  
  @PostMapping("/comment")
  public void  createComment(Comment comment){
    commentService.createComment(comment);
   }
  
  @DeleteMapping("/delete/{cno}")
  public Map<String, String> deleteComment(@PathVariable int cno) {
  
    commentService.deleteComment(cno);
    Map<String, String> map = new HashMap<>();
    map.put("result", "success");
    return map;
  }
  
  @PutMapping("/update")
  public Map<String, String> updateComment(Comment comment) {
 
    Map<String, String> map = new HashMap<>();
    commentService.updateComment(comment);
    map.put("result", "success");
    return map;
  }
  
  @GetMapping("/getcomment/{cno}")
  public Comment readcomment(@PathVariable int cno) {
    Comment comment=commentService.getComment(cno);
    return comment;
  }
  @PostMapping("/photo")
  public void  createphoto(Photo photo){
    
    if(photo.getBattach() != null && !photo.getBattach().isEmpty()) {
      log.info("~~~~~~~~~~~~~~~~~~~~~~"+photo);
      MultipartFile mf = photo.getBattach();
      photo.setPname(mf.getOriginalFilename());
      photo.setPsname(new Date().getTime() +"-"+ mf.getOriginalFilename());
      photo.setPtype(mf.getContentType());
      try {
        File file = new File("C:/Temp/uploadfiles/"+photo.getPsname());
        mf.transferTo(file);
      } catch (Exception e) {
        log.error(e.getMessage());
      } 
    }
    photoService.insertPhoto(photo);
  }
  
  @GetMapping("/photoread")
  public List<Photo> readPhoto(@PathVariable int bno){
    List<Photo> photo=photoService.selectPhoto(bno);
    return photo;
  }

	@PutMapping("/{bno}")
  public int updateLike(@PathVariable int bno, @RequestParam(defaultValue = "user") String mid) {
	  log.info("??????");
	  log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+bno+" "+mid);
	  int likeCount=boardService.likeCheck(bno, mid);
	  log.info(likeCount);
	  if(likeCount==0) {
	    log.info("????????????"+likeCount);
	    boardService.updateLike(bno);
      boardService.upLikeInfo(bno, mid);
	  }else if(likeCount==1){
	    log.info("?????????22222"+likeCount);
	    boardService.downLikeInfo(bno, mid);
	    boardService.cancelLike(bno);
	  }
	  return likeCount;
	}
	
}
