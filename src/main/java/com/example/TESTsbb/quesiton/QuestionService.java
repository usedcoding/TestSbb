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

    //page 와 kw 객체를 파라미터로 받는다.
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
    //id 값을 파라미터로 받는다.
    public Question getQuestion(Integer id) {
         //매개변수 id값을 사용하여 questionRepository에서 id 값을 찾아 새로운 Optional<Question>객체를 생성하고 이 값을 question 변수에 담는다.
        Optional<Question> question = this.questionRepository.findById(id);
        //만약 question 데이터가 존재 한다면
        if (question.isPresent()) {
            //question 데이터를 새로운 Question 객체의 question1 변수애 저장한다.
            Question question1 = question.get();
            //question1의 view 데이터에 1을 더하여 view에 다시 세팅한다.
            question1.setView(question1.getView() + 1);
            //세팅된 question1 데이터를 questionRepository에 저장한다.
            this.questionRepository.save(question1);
            //question1 객체를 반환한다.
            return question1;
        } else {
            //오류를 발생시키고 question not found 메세지를 출력 한다.
            throw new DataNotFoundException("question not found");
        }
    }
    //siteUser 객체를 파라미터로 받는다.
    public List<Question> getQuestionByAuthor(SiteUser siteUser) {
         //새로운 ArrayList 객체를 생성하고 이를 sorts 변수에 저장한다.
        List<Sort.Order> sorts = new ArrayList<>();
        //sorts 변수에 id 속성을 기준으로 오름차순 정렬을 수행한다.
        sorts.add(Sort.Order.asc("id"));
        //매개변수 siteUser를 사용하여 questionRepository에서 author 데이터를 찾고 이를 반환한다.
        return this.questionRepository.findByAuthor(siteUser);

    }

    //subject, content, siteUser, theme 객체를 파라미터로 받는다.
    public void create(String subject, String content, SiteUser siteUser, String theme){
         //새로운 Question 객체를 생성하고 이를 q 변수에 저장한다.
        Question q = new Question();
        //매개변수 subject를 객체 q의 subject 변수에 저장한다.
        q.setSubject(subject);
        //매개변수 content를 객체 q의 content 변수에 저장한다.
        q.setContent(content);
        //매개변수 LocalDataTime.now()를 객체 q의 createDate 변수에 저장한다.
        q.setCreateDate(LocalDateTime.now());
        //매개변수 siteUser를 객체 q의 author 변수에 저장한다.
        q.setAuthor(siteUser);
        //매개변수 theme을 객체 q 의 theme 변수에 저장한다.
        q.setTheme(theme);
        //매개변수 q의 데이터를 questionRepository에 저장한다.
        this.questionRepository.save(q);
    }

    //question, subject, content 객체를 파라미터로 받는다.
    public void modify(Question question, String subject, String content) {
         //매개변수 subject를 question객체의 subejct 변수에 저장한다.
        question.setSubject(subject);
        //매개변수 content를 question 객체의 content 변수에 저장한다.
        question.setContent(content);
        //매개변수 LocalDateTime.now()를 question 객체의 modifyDate 변수에 저장한다.
        question.setModifyDate(LocalDateTime.now());
        //매개변수 question의 데이터를 questionRepository에 저장한다.
        this.questionRepository.save(question);
    }

    //question 객체를 파라미터로 받는다.
    public void delete(Question question) {
         //매개변수 question 데이터를 questionRepository에서 삭제한다.
        this.questionRepository.delete(question);
    }

    //question, siteUser 객체를 파라미터로 받는다.
    public void vote(Question question, SiteUser siteUser) {
         //question 객체의 voter변수에 매개변수 siteUser의 데이터를 추가한다.
        question.getVoter().add(siteUser);
        //매개변수 question의 데이터를 questionRepository에 저장한다.
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