package com.example.TESTsbb.quesiton;

import com.example.TESTsbb.DataNotFoundException;
import com.example.TESTsbb.answer.Answer;
import com.example.TESTsbb.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    //pasge 와 kw 객체를 파라미터로 받는다.
    public Page<Question> getList(int page, String kw) {
        //새로운 ArrayList 객체를 생성하고 이를 List 타입의 sorts 변수에 저장한다.
        List<Sort.Order> sorts = new ArrayList<>();
        //sorts 리스트에 새로운 정렬 기준을 추가한다
        //createDate 속성을 기준으로 내림차순 한다.
        sorts.add(Sort.Order.desc("createDate"));
        //현재 페이지 번호, 페이지 당 아이템 수, sorts에 저장된 정렬기준에 따라 새로운 pageable 객체를 생성한다.
        Pageable pageable = PageRequest.of(page, 10,Sort.by(sorts));
        //주어진 매개변수 kw를 사용하여 search 메서드를 실행하고 생성된 객체를 spec 변수에 저장한다.
        Specification<Question> spec = search(kw);
        //주어진 매개변수 spec와 pageable을 사용하여 questionRepository에서 질문 목록을 조회하고 결과를 반환한다.
        return this.questionRepository.findAll(spec, pageable);
    }

    //    public Question getQuestion(Integer id) {
//        Optional<Question> question = this.questionRepository.findById(id);
//        if (question.isPresent()) {
//            return question.get();
//        } else {
//            throw new DataNotFoundException("question not found");
//        }
//    }
    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            Question question1 = question.get();
            question1.setView(question1.getView() + 1);
            this.questionRepository.save(question1);
            return question1;
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public List<Question> getQuestionByAuthor(SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("id"));
        return this.questionRepository.findByAuthor(siteUser);

    }

    public void create(String subject, String content, SiteUser siteUser, String theme){
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(siteUser);
        q.setTheme(theme);
        this.questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자
            }
        };
    }


    public Page<Question> allTheme(int page){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, 16,Sort.by(sorts));
        return this.questionRepository.findAll(pageable);
    }

    public Specification<Question> searchTheme(String sortkey) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Season 컬럼을 기준으로 검색 조건 생성
            if (sortkey != null) {
                Path<String> seasonPath = root.get("theme");
                Predicate seasonPredicate = criteriaBuilder.equal(seasonPath, sortkey);
                predicates.add(seasonPredicate);
            }

            // 다른 조건들을 추가하고 싶다면 여기에 추가

            // 검색 조건들을 조합하여 최종 검색 조건 생성
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Page<Question> getTheme(int page, String key) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(page, 16, Sort.by(sorts));
        Specification<Question> spec = searchTheme(key);
        return this.questionRepository.findAll(spec, pageable);
    }
}