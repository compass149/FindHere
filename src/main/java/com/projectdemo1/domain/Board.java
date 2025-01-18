package com.projectdemo1.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projectdemo1.domain.boardContent.BoardImage;
import com.projectdemo1.domain.boardContent.Status;
import com.projectdemo1.domain.boardContent.color.PetColor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Table(name = "board")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uno")
    private User user;

    private String title;
    private String petDescription;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lostDate;
    private String location;
    private String postType;
    private String locationDetail;
    private String petBreeds;
    private String petGender;
    private String petAge;
    private String petWeight;
    private String petName;
    private String petType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petColorId")
    private PetColor petColor;

    private String content;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @ColumnDefault("0")
    private Long hitCount;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("board")
    private List<Comment> comments;

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<BoardImage> imageSet= new HashSet<>(); // 이미지 목록

    @PrePersist
    public void prePersist() {
        this.hitCount = this.hitCount == null ? 0 : this.hitCount;
    }

    public void addImage(String uuid, String fileName) {
        BoardImage image = BoardImage.builder()
                .uuid(uuid)
                .fileName(fileName)
                .board(this)
                .ord(imageSet.size())
                .build();
        imageSet.add(image);
    }

    public void clearImages() {
        imageSet.forEach(boardImage -> boardImage.changeBoard(null));
        this.imageSet.clear();
    }

    // change 메서드를 추가하여 제목과 내용을 변경할 수 있도록 함
    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
