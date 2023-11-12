package com.example.TESTsbb.answer;


import com.example.TESTsbb.comment.Comment;
import com.example.TESTsbb.quesiton.Question;
import com.example.TESTsbb.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    @ManyToOne
    private SiteUser author;

    private LocalDateTime modifyDate;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<SiteUser> voter;

    @ManyToOne
    private Question question;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Comment> commentList;


}
