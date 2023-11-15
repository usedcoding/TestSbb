package com.example.TESTsbb.comment;

import com.example.TESTsbb.answer.Answer;
import com.example.TESTsbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public void create(Answer answer, String content, SiteUser siteUser) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAnswer(answer);
        comment.setAuthor(siteUser);
        this.commentRepository.save(comment);
    }

    public List<Comment> getCommentByAuthor(SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("id"));
        return this.commentRepository.findByAuthor(siteUser);

    }
}
