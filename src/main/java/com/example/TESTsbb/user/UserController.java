package com.example.TESTsbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    //의존성 주입 userService
    private final UserService userService;

    //엔드포인트 /signup
    // /signup을 입력했을 때 요청 받는다
    @GetMapping("/signup")
    //UserCreateForm 객페를 파라미터로 받는다.
    public String signup(UserCreateForm userCreateForm) {
        //Get 요청을 받았을 때 signup_form을 반환한다.
        return "signup_form";
    }

    // /signup 을 엔드포인트로 Post요청이 처리된다.
    @PostMapping("/signup")
    //userCreateForm, bindingResult, thumbnail 객체를 파라미터로 받는다.
    //userCreateForm의 유효성 검사를 실행한 후 결과를 bindingResult에 저장한다.
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, MultipartFile thumbnail) {
        //만약 bindingResult에 에러가 발생한 경우
        if (bindingResult.hasErrors()) {
            //signup_form으로 반환한다.
            return "signup_form";
        }
        //만약 userCreateForm에 저장된 password1과 password2의 값이  다른 경우
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            //bindingResult.rejectValue로 password2 필드에 대한 오류를 추가한다.
            //passwordInCorrect 오류를 추가하고, 2개의 패스워드가 일치 하지 않습니다.라는 오류 메시지를 출력한다.
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            //signup_form으로 반환한다.
            return "signup_form";
        }

        try {
        //userService의 create 매서드를 실행 하고, userCreateForm에 저장된 username, email,password1, thumbnail 데이터를 저장 한다.
        this.userService.create(userCreateForm.getUsername(),userCreateForm.getEmail(), userCreateForm.getPassword1(),thumbnail);
        //DataIntegrityViolationException/데이터 무결성 오류가 발생하면
        }catch(DataIntegrityViolationException e) {
            //어떤 오류가 발생 한지 알 수 있게 해주는 메서드
            e.printStackTrace();
            //signupFailed 오류 코드가 발생 하고 이미 등록된 사용자 입니다. 라는 오류 메시지를 출력 한다.
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            //signup_form을 반환한다.
            return "signup_form";
            //오류가 발생하면
        }catch(Exception e) {
            //어떤 오류가 발생했는지 알려준다.
            e.printStackTrace();
            //signupFailed의 에러코드로 저장된 에러 메세지를 출력한다.
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        //에러가 발생하지 않았다면 루트 페이지로 redirect 된다.
        return "redirect:/";
    }

    // /login을 엔드포인트로 get 요청
    @GetMapping("/login")
    public String login() {
        //login_form을 반환 한다.
        return "login_form";
    }

    // /modify를 엔드포인트로 get요청
    @GetMapping("/modify")
    //model과 principal 객체를 파라미터로 받는다.
    public String userModify(Model model, Principal principal) {
        //principal에 저장된 name 데이터 (현재 사용자의 이름)를 이용해 사용자의 정보를 조회 하고 이 데이터를 siteUser 변수에 저장 한다.
        SiteUser siteUser = this.userService.getUser(principal.getName());
        //새로운 UserForm을 생성하고 userForm에 저장한다.
        UserForm userForm = new UserForm();
        //model에 siteUser를 user 이름으로 저장 한다.
        model.addAttribute("user", siteUser);
        //model에 userForm을 userForm이름올 저장한다.
        model.addAttribute("userForm", userForm);
        //suerModify_form을 반환한다.
        return "userModify_form";
    }

    // /modify를 엔드포인트로 Post요청
    @PostMapping("/modify")
    //@PreAuthorize는 메서드나 클래스에 보안을 적용하는 annotation
    //isAuthenticated()는 사용자가 인증된 경우에만 호출할 수 있다.
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    //userForm, principal, bindingResult 객체를 파라미터로 받아온다.
    // userForm은 사용자가 제출한 폼데이터를 받고, principal는 기존 사용자 정보를 받는다.
    public String userEdit(UserForm userForm, Principal principal, BindingResult bindingResult) {
        //현재 사용자의 이름을 통해 현재 사용자의 정보를 받아 siteUser 변수에 저장한다.
        SiteUser siteUser = this.userService.getUser(principal.getName());
        //bindingResult가 오류가 있는 경우
        if (bindingResult.hasErrors()) {
            //메세지를 반환한다.
            return "다시 시도해 주세요. ";
            //만약 userForm의 저장된 password1이 빈 값인 경우
        }else if (userForm.getPassword1().equals("")) {
            //메세지를 반환힌다.
            return "변경할 비밀번호를 입력해주세요";
            //userForm의 저장된 password1값과 passwor2값이 다른 경우
        }else if (!userForm.getPassword1().equals(userForm.getPassword2())) {
            //메세지를 출력한다.
            return "비밀번호 2개가 일치하지 않습니다.";
        }else {
            //userService에 있는 modifyUser를 수행하여 현재 사용자 (siteUser)를 수정한다. 사용자가 입력한 password1을 저장한다.
            this.userService.modifyUser(siteUser,userForm.getPassword1());
            //메세지 값을 반환한다.
            return "성공적으로 수정되었습니다.";
        }
    }




}
