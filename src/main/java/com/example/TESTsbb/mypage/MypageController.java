package com.example.TESTsbb.mypage;


import com.example.TESTsbb.answer.Answer;
import com.example.TESTsbb.answer.AnswerService;
import com.example.TESTsbb.comment.Comment;
import com.example.TESTsbb.comment.CommentService;
import com.example.TESTsbb.quesiton.Question;
import com.example.TESTsbb.quesiton.QuestionService;
import com.example.TESTsbb.user.SiteUser;
import com.example.TESTsbb.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;
    private final CommentService commentService;

    @GetMapping("/mypage")
    public String myqna(Model model, Principal principal){
        SiteUser siteUser = this.userService.getUserInfo(principal.getName());
        List<Question> myquestionList = this.questionService.getQuestionByAuthor(siteUser);
        List<Answer> myanswerList = this.answerService.getAnswerByAuthor(siteUser);
        List<Comment> mycommentList = this.commentService.getCommentByAuthor(siteUser);
        model.addAttribute("myquestionList",myquestionList);
        model.addAttribute("myanswerList",myanswerList);
        model.addAttribute("mycommentList", mycommentList);
        model.addAttribute("siteUser", siteUser);
        return "user_mypage";
    }



}
