package com.example.TESTsbb.comment;

import com.example.TESTsbb.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByAuthor(SiteUser siteUser);
}
