package com.projectdemo1.controller;

import com.projectdemo1.auth.PrincipalDetails;
import com.projectdemo1.domain.Board;
import com.projectdemo1.domain.User;
import com.projectdemo1.domain.boardContent.color.PetColor;
import com.projectdemo1.domain.boardContent.color.PetColorType;
import com.projectdemo1.dto.BoardDTO;
import com.projectdemo1.dto.PageRequestDTO;
import com.projectdemo1.dto.PageResponseDTO;
import com.projectdemo1.dto.upload.UploadFileDTO;
import com.projectdemo1.repository.UserRepository;
import com.projectdemo1.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/board")
@Log4j2
@RequiredArgsConstructor
public class BoardController {

    @Value("${com.projectdemo1.board4.upload.path}")
    private String uploadPath;

    private final BoardService boardService;

    // 공통 파일 업로드 메서드
    private List<String> fileUpload(UploadFileDTO uploadFileDTO) {
        List<String> list = new ArrayList<>();
        uploadFileDTO.getFiles().forEach(multipartFile -> {
            String originalName = multipartFile.getOriginalFilename();
            log.info(originalName);

            String uuid = UUID.randomUUID().toString();
            Path savePath = Paths.get(uploadPath, uuid + "_" + originalName);
            boolean image = false;
            try {
                multipartFile.transferTo(savePath); // 서버에 파일저장
                if (Files.probeContentType(savePath).startsWith("image")) {
                    image = true;
                    File thumbFile = new File(uploadPath, "s_" + uuid + "_" + originalName);
                    Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200); // 썸네일 생성
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            list.add(uuid + "_" + originalName);
        });
        return list;
    }

    // 게시글 등록 폼
    @GetMapping("/register")
    public String register() {
        return "board/register";
    }

    // 게시글 등록 처리
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute Board board,
                           UploadFileDTO uploadFileDTO, BoardDTO boardDTO,
                           @RequestParam PetColorType petColorType,
                           PrincipalDetails principal) {
        List<String> strFileNames = null;
        if (uploadFileDTO.getFiles() != null && !uploadFileDTO.getFiles().get(0).getOriginalFilename().equals("")) {
            strFileNames = fileUpload(uploadFileDTO);
            log.info(strFileNames.size());
        }
        boardDTO.setFileNames(strFileNames);
        PetColor petColor = new PetColor(petColorType);  // PetColor 객체 생성
        board.setPetColor(petColor);
        boardService.register(board, principal.getUser()); // 서비스에 등록

        return "redirect:/board/list"; // 등록 후 목록 페이지로 리다이렉트
    }

    // 게시글 수정 폼
    @GetMapping({"/read", "/modify"})
    public void read(Long bno, PageRequestDTO pageRequestDTO, Model model) {
        BoardDTO boardDTO = boardService.findById(bno);
        if (boardDTO.getUser() == null) {
            User defaultUser = new User();
            defaultUser.setNickname("Default Nickname");
            boardDTO.setUser(defaultUser);
        }
        log.info(boardDTO);
        model.addAttribute("dto", boardDTO);
    }

    // 게시글 수정 처리
    @PostMapping("/modify")
    public String modify(UploadFileDTO uploadFileDTO, PageRequestDTO pageRequestDTO,
                         @Valid BoardDTO boardDTO, BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        log.info("board POST modify.........." + boardDTO);

        List<String> strFileNames = null;
        if (uploadFileDTO.getFiles() != null && !uploadFileDTO.getFiles().get(0).getOriginalFilename().equals("")) {

            List<String> fileNames = boardDTO.getFileNames();
            if (fileNames != null && fileNames.size() > 0) {
                removeFile(fileNames); // 기존 파일 삭제
            }

            strFileNames = fileUpload(uploadFileDTO); // 새 파일 업로드
            log.info(strFileNames.size());
            boardDTO.setFileNames(strFileNames); // 수정된 파일 리스트 설정
        }

        if (bindingResult.hasErrors()) {
            log.info("has errors");
            String link = pageRequestDTO.getLink();
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            redirectAttributes.addAttribute("bno", boardDTO.getBno());
            return "redirect:/board/modify?" + link;
        }

        boardService.modify(boardDTO); // 게시글 수정
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("bno", boardDTO.getBno());
        return "redirect:/board/read";
    }

    // 게시글 삭제
    @PostMapping("/remove")
    public String remove(@RequestParam Long bno) {
        boardService.remove(bno); // 게시글 삭제
        return "redirect:/board/list"; // 삭제 후 목록 페이지로 리다이렉트
    }

    // 게시글 목록
    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model) {
        List<Board> lists = boardService.list();
        model.addAttribute("lists", lists);
        PageResponseDTO<BoardDTO> responseDTO = boardService.list(pageRequestDTO);
        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
    }

    // 파일 조회
    @GetMapping("/view/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> viewFileGet(@PathVariable("fileName") String fileName) {
        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    // 파일 삭제
    private void removeFile(List<String> fileNames) {
        log.info("File removal: " + fileNames.size());
        for (String fileName : fileNames) {
            log.info("Removing file: " + fileName);
            Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                boolean removed = resource.getFile().delete(); // 파일 삭제
                if (contentType.startsWith("image")) {
                    String originalFileName = fileName.replace("s_", "");
                    File originalFile = new File(uploadPath + File.separator + originalFileName);
                    originalFile.delete(); // 썸네일 원본 파일 삭제
                }
            } catch (Exception e) {
                log.error("Error while deleting file: " + e.getMessage());
            }
        }
    }
}
