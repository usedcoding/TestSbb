package com.example.TESTsbb;

import com.example.TESTsbb.answer.Answer;
import com.example.TESTsbb.answer.AnswerRepository;
import com.example.TESTsbb.quesiton.Question;
import com.example.TESTsbb.quesiton.QuestionRepository;
import com.example.TESTsbb.quesiton.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TesTsbbApplicationTests {

	@Autowired
	private QuestionService questionService;

	@Test
	void testJpa() {
		for (int i = 1; i <= 300; i++) {
			String subject = String.format("테스트 데이터입니다:[%03d]", i);
			String content = "내용무";
			this.questionService.create(subject, content,null);
		}
	}
}
