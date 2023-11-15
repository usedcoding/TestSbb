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

    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, MultipartFile thumbnail) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try {
        userService.create(userCreateForm.getUsername(),userCreateForm.getEmail(), userCreateForm.getPassword1(),thumbnail);
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/modify")
    public String userModify(Model model, Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        UserForm userForm = new UserForm();
        model.addAttribute("user", siteUser);
        model.addAttribute("userForm", userForm);
        return "userModify_form";
    }


    @PostMapping("/modify")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public String userEdit(UserForm userForm, Principal principal, BindingResult bindingResult) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            return "다시 시도해 주세요. ";
        }else if (userForm.getPassword1().equals("")) {
            return "변경할 비밀번호를 입력해주세요";
        }else if (!userForm.getPassword1().equals(userForm.getPassword2())) {
            return "비밀번호 2개가 일치하지 않습니다.";
        }else {
            this.userService.modifyUser(siteUser,userForm.getPassword1());
            return "성공적으로 수정되었습니다.";
        }
    }




}
