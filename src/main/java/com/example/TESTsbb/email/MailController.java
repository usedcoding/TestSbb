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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
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

            SiteUser user = userService.getUser(userName);
            user.setPassword(passwordEncoder.encode(tempPw));
            userRepository.save(user);

        } catch (Exception e) {
            throw new DataNotFoundException("error");
        }
    }

    public boolean confirmPassword(String password, SiteUser user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public SiteUser modifyPassword(String password, SiteUser user) {
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
        return user;
    }
}
