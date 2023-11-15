package com.example.TESTsbb.email;

import com.example.TESTsbb.DataNotFoundException;
import com.example.TESTsbb.user.SiteUser;
import com.example.TESTsbb.user.UserRepository;
import com.example.TESTsbb.user.UserService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@Controller
public class MailController {

    private final JavaMailSender mailSender;

    private final UserService userService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/user/findPw/sendEmail")
    @ResponseBody
    public void sendEmailForPw(@RequestParam("email") String userEmail, String userName) {

        String tempPw = userService.generateTempPassword();
        String from = "tjqls2013@gmail.com";//보내는 이 메일주소
        String to = userEmail;
        String title = "임시 비밀번호입니다.";
        String content = userName + "님의" + "[임시 비밀번호] " + tempPw + " 입니다. <br/> 접속한 후 비밀번호를 변경해주세요";
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");

            mailHelper.setFrom(from);
            mailHelper.setTo(to);
            mailHelper.setSubject(title);
            mailHelper.setText(content, true);

            mailSender.send(mail);

            SiteUser siteUser = userService.getUser(userName);
            siteUser.setPassword(passwordEncoder.encode(tempPw));
            userRepository.save(siteUser);

        } catch (Exception e) {
            throw new DataNotFoundException("error");
        }
    }

    public boolean confirmPassword(String password, SiteUser siteUser) {
        return passwordEncoder.matches(password, siteUser.getPassword());
    }

    public SiteUser modifyPassword(String password, SiteUser siteUser) {
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
        return siteUser;
    }

    @GetMapping("/user/forgotPassword")
    public String showPasswordRecoveryForm() {
        // 비밀번호 찾기 화면을 보여주는 로직 추가
        return "user_forgotPassword"; // 이 부분은 실제 비밀번호 찾기 화면의 Thymeleaf 템플릿 이름으로 수정해야 합니다.
    }

    @PostMapping("/user/forgotPassword")
    @ResponseBody
    public void forgotPassword(@RequestParam("email") String userEmail) {
        userService.initiatePasswordRecovery(userEmail);
    }
}
