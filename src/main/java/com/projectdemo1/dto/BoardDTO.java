package com.projectdemo1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projectdemo1.domain.Board;
import com.projectdemo1.domain.Comment;
import com.projectdemo1.domain.User;
import com.projectdemo1.domain.boardContent.BoardImage;
import com.projectdemo1.domain.boardContent.Status;
import com.projectdemo1.domain.boardContent.color.PetColor;
import com.projectdemo1.domain.boardContent.color.PetColorType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardDTO {

    private Long bno;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> fileNames;
    private String postType;
    private Long hitCount;
    private Set<BoardImage> imageSet;
    private List<Comment> comments;
    private Status status;
    private String petDescription;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lostDate;

    private String location;
    private String locationDetail;
    private String petBreeds;
    private String petGender;
    private String petAge;
    private String petWeight;
    private String petType;
    private String petName;
    private Long uno;

    // PetColor 객체와 PetColorType 추가
    private PetColor petColor;
    private PetColorType petColorType;

    private User user;
    private String mobile;
    private String email;

    // Board 객체로부터 생성자
    public BoardDTO(Board board) {
        this.bno = board.getBno();
        this.title = board.getTitle();
        this.content = board.getContent();

        // PetColor 및 PetColorType 처리
        this.petColor = board.getPetColor();
        if (board.getPetColor() != null) {
            this.petColorType = board.getPetColor().getColor(); // PetColorType을 가져옵니다.
        }

        // createdAt, updatedAt 처리: null일 경우 현재 시간으로 설정
        this.createdAt = board.getCreatedAt() != null ? board.getCreatedAt() : LocalDateTime.now();
        this.updatedAt = board.getUpdatedAt() != null ? board.getUpdatedAt() : LocalDateTime.now();

        // User 정보 처리: user가 null일 경우 처리를 추가
        this.user = board.getUser();
        this.hitCount = board.getHitCount();
        this.imageSet = board.getImageSet();
        this.comments = board.getComments();
        this.status = board.getStatus();
        this.petDescription = board.getPetDescription();
        this.lostDate = board.getLostDate();
        this.location = board.getLocation();
        this.locationDetail = board.getLocationDetail();
        this.petBreeds = board.getPetBreeds();
        this.petGender = board.getPetGender();
        this.petAge = board.getPetAge();
        this.petWeight = board.getPetWeight();
        this.petType = board.getPetType();
        this.petName = board.getPetName();
        this.uno = (board.getUser() != null) ? board.getUser().getUno() : null; // User가 null일 경우 처리를 추가

        // 이메일, 전화번호 등 추가적인 필드 처리 (optional)
        this.mobile = board.getUser() != null ? board.getUser().getMobile() : null;
        this.email = board.getUser() != null ? board.getUser().getEmail() : null;
    }
}
