package com.example.TESTsbb.answer;

import com.example.TESTsbb.quesiton.Question;
import com.example.TESTsbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AnswerRepository extends JpaRepository<Answer,Integer> {
    Page<Answer> findAllByQuestion(Question question, Pageable pagealbe);
    List<Answer> findByAuthor(SiteUser siteUser);
}
