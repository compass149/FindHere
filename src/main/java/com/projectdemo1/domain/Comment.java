package com.projectdemo1.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projectdemo1.board4.domain.Cboard;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Builder
@Entity
@Table(name = "comment", indexes = {@Index(name = "idx_comment_board", columnList = "bno")})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString(exclude = "board")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;
    private String replyText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bno", nullable = false)
    /*  @JsonIgnore*/
    private Board board;


    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    private String replyer; //댓글 작성자

    public void changeText(String text) {
        this.replyText = text;
    }
}