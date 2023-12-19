package com.example.TESTsbb.quesiton;

import com.example.TESTsbb.answer.Answer;
import com.example.TESTsbb.answer.AnswerForm;
import com.example.TESTsbb.answer.AnswerService;
import com.example.TESTsbb.user.SiteUser;
import com.example.TESTsbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;

    @GetMapping("/list")
    public String list(Model model,@RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw, Principal principal) {
        Page<Question> paging = this.questionService.getList(page, kw);

        if (principal != null) {
            SiteUser siteUser = this.userService.getUser(principal.getName());
            model.addAttribute("siteUser", siteUser);
            model.addAttribute("userImg", siteUser.getThumbnailImg());
        }

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "question_list";
    }

    @GetMapping(value = "/list/{theme}")
    public String questionList(Model model, @PathVariable("theme") String key, @RequestParam(value = "page", defaultValue = "0") int page, Principal principal) {
        String themeKey;
        if (key.equals("Free")) {
            themeKey = "Free";
            Page<Question> paging = this.questionService.getTheme(page, themeKey);
            model.addAttribute("paging", paging);
            model.addAttribute("themeKey",themeKey);
        } else if (key.equals("English")) {
            themeKey = "English";
            Page<Question> paging = this.questionService.getTheme(page, themeKey);
            model.addAttribute("paging", paging);
            model.addAttribute("themeKey",themeKey);
        } else if (key.equals("Korea")) {
            String themeName = "Korea";
            Page<Question> paging = this.questionService.getTheme(page,key);
            model.addAttribute("paging", paging);
            model.addAttribute("themeKey",themeName);
        } else if (key.equals("ETC")) {
            String themeName = "ETC";
            Page<Question> paging = this.questionService.getTheme(page, key);
            model.addAttribute("paging", paging);
            model.addAttribute("themeKey",themeName);
        } else if (key.equals("all")){
            String themeName = "All";
            Page<Question> paging = this.questionService.allTheme(page);
            model.addAttribute("paging",paging);
            model.addAttribute("themeKey",themeName);
        }

        if (principal != null) {
            SiteUser siteUser = this.userService.getUser(principal.getName());
            model.addAttribute("siteUser", siteUser);
            model.addAttribute("userImg", siteUser.getThumbnailImg());
        }

        return "question_list";
    }


    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,@RequestParam(value = "answerPage", defaultValue = "0") int answerPage) {
        Question question = this.questionService.getQuestion(id);
        Page<Answer> answerPaging =  this.answerService.getList(question, answerPage);
        model.addAttribute("question", question);
        model.addAttribute("answerPaging", answerPaging);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()){
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.create(questionForm.getSubject(),questionForm.getContent(), siteUser, questionForm.getTheme());
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}
