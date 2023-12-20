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

    //유효성 검증 annotation
    //사용자 인증이 없는 경우 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인트로 get 요청을 보낸다.
    @GetMapping("/create")
    //questionForm 객체를 파라미터로 받는다.
    public String questionCreate(QuestionForm questionForm) {
        //매서드 실행시 "question_form"을 반환한다.
        return "question_form";
    }
    //유효성 검증 annotation
    //사용자 인증이 없는 경우 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인트로 Post 요청을 보낸다.
    @PostMapping("/create")
    //questionForm 데이터를 검증하고 이 결과를 bindingResult 객체에 저장한다. principal을 파라미터로 받는다.
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        //만약 bindingResult에 오류가 있다면 "question_form"을 반환한다.
        if(bindingResult.hasErrors()){
            return "question_form";
        }
        //현재 사용자의 name 데이터를 통해 userService에서 user 데이터를 찾고 이 데이터를 siteUser 변수에 저장한다.
        SiteUser siteUser = this.userService.getUser(principal.getName());
        //questionForm에 저장된 subject, content, siteUser, theme을 매개변수로 받아 questionService의 create 매서드를 호출한다.
        this.questionService.create(questionForm.getSubject(),questionForm.getContent(), siteUser, questionForm.getTheme());
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }

    //유효성 검증 annotation
    //isAuthenticated()이 있는 경우에는 사용자 인증이 없으면 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인트로 get 요청을 보낸다.
    @GetMapping("/modify/{id}")
    //questionForm, id, principal을 파라미터로 받고, id는 url에서 얻은 id 값을 매개변수로 얻는다.
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
        //매개변수 id 값을 이용해 questionService에서 question 데이터를 얻고 이를 question 변수에 저장한다.
        Question question = this.questionService.getQuestion(id);
        //만약 question의 저장된 author 데이터중 username 데이터와 현재 사용자의 name데이터가 같이 않다면
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            //해당 오류를 발생시키고 "수정권한이 없습니다."라는 메세지를 출력한다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        //questionForm의 subject 객체에 question의 subject 데이터를 저장한다.
        questionForm.setSubject(question.getSubject());
        //questionForm의 content 객체에 question의 content 데이터를 저장한다.
        questionForm.setContent(question.getContent());
        //"question_form"을 반환한다.
        return "question_form";
    }

    //유효성 검증 annotation
    //isAuthenticated()이 있는 경우 사용자 인증이 없으면 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인트로 Post 요청을 보낸다.
    @PostMapping("/modify/{id}")
    //questionForm, bindingResult, principal, id를 파라미터로 받는다.
    //questionForm을 검증하고 결과를 bindingResult에 저장한다.
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        //만약 bindingResult에 오류가 발생한 경우
        if (bindingResult.hasErrors()) {
            //"question_form"을 반환한다.
            return "question_form";
        }
        //매개변수 id 값을 이용해 questionService에서 question 데이터를 찾고 이를 question 변수에 저장한다.
        Question question = this.questionService.getQuestion(id);
        //만약 question의 author 데이터 중 username과 현재 사용자의 name 값이 다른 경우
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            //오류를 발생 시키고 오류 메시지를 출력 한다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        //question, questionForm에 저장된 subject, questionForm에 저장된 content를 매개변수로 questionService의 modify 메서드를 실행한다.
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        //지정된 엔드포인트로 redirect 한다.
        return String.format("redirect:/question/detail/%s", id);
    }

    //유효성 검증 annotation
    //isAutenticated()가 있으면 사용자 인증이 없는 경우 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인트로 get 요청
    @GetMapping("/delete/{id}")
    //principal과 id를 파라미터로 받는다.
    //파라미터 id에는 url에서 id 값을 받아 저장한다.
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        //매개변수 id값을 사용하여 questionService에서 question 데이터를 찾고 이를 question 변수에 저장한다.
        Question question = this.questionService.getQuestion(id);
        //만약 question의 author 데이터 중 username 데이터와 현재 사용자의 name 데이터가 다른경우
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            //오류를 발생시키고 오류메세지를 출력한다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        //매개변수 question 데이터를 삭제한다.
        this.questionService.delete(question);
        //지정된 엔드포인트로 redirect 한다.
        return "redirect:/";
    }

    //유효성 검증 annotation
    //isAuthenticated()가 있는 경우 사용자 인증이 되지 않으면 호출할 수 없다.
    @PreAuthorize("isAuthenticated()")
    //지정된 엔드포인드로 get 요청을 보낸다.
    @GetMapping("/vote/{id}")
    //principal과 id 객체를 파라미터로 받는다.
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        //매개변수 id값을 사용하여 questionService에서 question 데이터를 찾은 후 이를 question 변수에 저장한다.
        Question question = this.questionService.getQuestion(id);
        //현재 사용자의 name 값을 매개변수로 userService에서 user 데이터를 찾은 후 이를 siteUser변수에 저장한다.
        SiteUser siteUser = this.userService.getUser(principal.getName());
        //매개변수 question과 siteUser를 questionService의 vote 메서드를 실행한다.
        this.questionService.vote(question, siteUser);
        //지정된 엔드포인트로 redirect 한다.
        return String.format("redirect:/question/detail/%s", id);
    }
}
