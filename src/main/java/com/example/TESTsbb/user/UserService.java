package com.example.TESTsbb.user;

import com.example.TESTsbb.DataNotFoundException;
import com.example.TESTsbb.email.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    @Value("${custom.genFileDirPath}")
    private  String genFileDirPath;


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public SiteUser create(String username, String email, String password, MultipartFile thumbnail) {
        String thumbnailRelPath = "siteUser/" + UUID.randomUUID().toString()+".jpg";
        File thumbnailFile = new File(genFileDirPath + "/" + thumbnailRelPath);


        try{
            thumbnail.transferTo(thumbnailFile);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));
        user.setThumbnailImg(thumbnailRelPath);
        this.userRepository.save(user);
        return user;

    }

    public void modifyUser(SiteUser siteUser, String password) {
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    public SiteUser getUserInfo(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("SiteUser not found");
        }
    }



    public String generateTempPassword() {
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }


    public void initiatePasswordRecovery(String userEmail) {
        SiteUser siteUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다"));

        String tempPw = generateTempPassword();
        mailService.sendPasswordRecoveryEmail(siteUser.getEmail(), siteUser.getUsername(), tempPw);

        // 데이터베이스에서 사용자의 비밀번호를 업데이트합니다
        modifyUser(siteUser, tempPw);
    }





}
