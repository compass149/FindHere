package com.projectdemo1.controller;

import com.projectdemo1.domain.User;
import com.projectdemo1.repository.UserRepository;
import com.projectdemo1.service.BoardServiceImpl;
import com.projectdemo1.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserServiceImpl userServiceImpl;
    private final BoardServiceImpl boardServiceImpl;

    @GetMapping("user/join")
    public void join() {
        log.info("회원가입 페이지 요청");
    }

    @PostMapping("/user/join")
    public String register(User user, BindingResult bindingResult) {
        log.info("회원가입 시도: {}", user);
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        user.setRole("USER"); // 'USER' 역할 부여

        if (bindingResult.hasErrors()) {
            log.error("폼 검증 오류 발생: {}", bindingResult.getAllErrors());
            return "user/join"; // 폼 검증 오류가 있으면 회원가입 페이지로 돌아감
        }

        userRepository.save(user);
        log.info("회원가입 성공: {}", user.getUsername());
        return "redirect:/user/login"; // 회원가입 후 로그인 페이지로 이동
    }

    @GetMapping("/user/login")
    public String login() {
        log.info("로그인 페이지 요청");
        return "user/login";
    }

    @GetMapping("/user/edit-profile") // 프로필 수정 페이지
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("사용자 프로필 수정 페이지 요청, 사용자: {}", userDetails.getUsername());
        // 로그인한 사용자의 username을 통해 DB에서 User 엔티티를 조회
        User user = userRepository.findByUsername(userDetails.getUsername());

        // 조회한 사용자 정보를 모델에 추가
        model.addAttribute("user", user);
        return "user/edit-profile"; // 'edit-profile.html' 파일 경로 반환
    }

    @PostMapping("/user/edit-profile")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model, @ModelAttribute User user) {
        log.info("사용자 프로필 수정 시도, 사용자명: {}", userDetails.getUsername());

        // 현재 인증된 사용자의 사용자명 가져오기
        String username = userDetails.getUsername();

        // 해당 사용자 정보를 데이터베이스에서 찾기
        User existingUser = userRepository.findByUsername(username);

        if (existingUser == null) {
            log.error("사용자 존재하지 않음: {}", username);
            return "redirect:/login"; // 사용자 없으면 로그인 페이지로 리디렉션
        }

        // 비밀번호 업데이트
        String rawPassword = user.getPassword();
        if (rawPassword != null && !rawPassword.isEmpty()) {
            String encPassword = bCryptPasswordEncoder.encode(rawPassword);
            existingUser.setPassword(encPassword);
        }

        // 다른 필드 업데이트 (입력값이 비어있지 않은 경우만)
        if (user.getMobile() != null && !user.getMobile().isEmpty()) {
            existingUser.setMobile(user.getMobile());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            existingUser.setEmail(user.getEmail());
        }

        // 수정된 사용자 정보 저장
        userRepository.save(existingUser);
        log.info("사용자 프로필 수정 완료: {}", existingUser);

        // 수정 완료 페이지로 리디렉션
        return "redirect:/user/profile";
    }

    @GetMapping("/user/delete-account")
    public String deleteAccountPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("사용자 계정 삭제 페이지 요청, 사용자: {}", userDetails.getUsername());
        User user = userRepository.findByUsername(userDetails.getUsername());
        model.addAttribute("username", user.getNickname());
        return "user/delete-account";
    }

    @PostMapping("/user/delete-account")
    public String deleteAccount() {
        log.info("사용자 계정 삭제 요청");
        userServiceImpl.deleteAccount(); // UserServiceImpl의 deleteAccount 메서드를 호출
        return "redirect:/logout"; // 삭제 후 로그아웃으로 리디렉션
    }

    @GetMapping("/user/check-username")
    @ResponseBody
    public Map<String, Object> checkUserId(@RequestParam String username) {
        log.info("아이디 중복 체크 요청: {}", username);
        Map<String, Object> response = new HashMap<>();
        boolean isAvailable = userRepository.findByUsername(username) == null; // 아이디 중복 체크
        response.put("available", isAvailable);
        return response;
    }

    @GetMapping("/user/check-nickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        log.info("닉네임 중복 체크 요청: {}", nickname);
        Map<String, Object> response = new HashMap<>();
        boolean isAvailable = userRepository.findByNickname(nickname) == null; // 닉네임 중복 체크
        response.put("available", isAvailable);
        return response;
    }

    @GetMapping("/admin/list") // 관리자가 사용자 목록 보기
    public String adminList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("관리자 사용자 목록 페이지 요청");
        List<User> users = userRepository.findAll();

        // 모든 사용자 정보를 모델에 추가
        model.addAttribute("users", users);

        return "admin/list"; // admin/list.html 템플릿 반환
    }
}
