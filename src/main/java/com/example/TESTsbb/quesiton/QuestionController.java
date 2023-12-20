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

    // 지정된 앤드포인트로 get 요청을 보낸다
    @GetMapping("/list")
    //model, page, kw, principal 객체를 파라미터로 받는다., 앤드포인트 요청시 page와 kw 파라미터를 요청하고 없을시 page는 0 kw는 null값을 받는다.
    public String list(Model model,@RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw, Principal principal) {
        //pasgw와 kw를 매개변수로 받아 해당하는 list를 questionService를 통해 찾고 이 데이터를 paging 변수에 저장한다.
        Page<Question> paging = this.questionService.getList(page, kw);
        //만약 현재 사용자가 있다면
        if (principal != null) {
            //현재 사용자의 이름을 통해 사용자 데이터를 얻고 이것을 siteUser 객체에 저장한다.
            SiteUser siteUser = this.userService.getUser(principal.getName());
            //model에 siteUser를 siteUser 이름으로 저장한다.
            model.addAttribute("siteUser", siteUser);
            //model에 siteUser에 저장된 thumbnailImg를 userImg의 이름으로 저장한다.
            model.addAttribute("userImg", siteUser.getThumbnailImg());
        }
        //model에 paging 데이터를 paging의 이름으로 저장한다.
        model.addAttribute("paging", paging);
        //model에 kw 데이터를 kw의 이름으로 저장한다.
        model.addAttribute("kw", kw);

        //"question_list"를 반환한다.
        return "question_list";
    }

    //엔드포인트로 get 요청
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

    //지정된 엔드포인트로 get요청을 보낸다.
    @GetMapping(value = "/detail/{id}")
    //model, id, answerForm, answerPage 객체를 파라미터로 받고 요청받는 answerPage은 기본적으로 값이 0이다.
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,@RequestParam(value = "answerPage", defaultValue = "0") int answerPage) {
       //매개변수 id를 총해 questionService에서 question 데이터를 찾고 이 데이터를 question 변수에 저장한다.
        Question question = this.questionService.getQuestion(id);
        //매개변수 question과 answerPage를 통해 answerService에서 list데이터를 찾고 이 데이터를 answerPaging 변수에 저장한다.
        Page<Answer> answerPaging =  this.answerService.getList(question, answerPage);
        //model에 question 데이터를 question의 이름으로 저장한다.
        model.addAttribute("question", question);
        //model에 answerPaging 데이터를 answerPaging의 이름으로 저장한다.
        model.addAttribute("answerPaging", answerPaging);
        //"question_detail"을 반환한다.
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
