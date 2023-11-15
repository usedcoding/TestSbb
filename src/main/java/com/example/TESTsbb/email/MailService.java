package com.example.TESTsbb.email;

import com.example.TESTsbb.DataNotFoundException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendPasswordRecoveryEmail(String to, String userName, String tempPw) {
        String from = "tjqls2013@gmail.com"; // 보내는 이 메일주소
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
        } catch (Exception e) {
            throw new DataNotFoundException("이메일 전송 중 오류가 발생했습니다.");
        }
    }
}

