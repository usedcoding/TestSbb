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
        //새로운 SiteUser객체를 생성하여 user 변수에 할당 한다.
        SiteUser user = new SiteUser();
        //새로운 username 데이터를 SiteUser의 username 속성에 저장 한다.
        user.setUsername(username);
        //새로운 email 데이터를 SiteUser의 email 속성에 저장 한다.
        user.setEmail(email);
        //새로운 BCryptPasswordEncoder 객체를 생성 하여 passwordEncoder 변수에 할당 한다.
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //passwordEncoder를 통해 password를 암호화 하고 SiteUser의 password 속성에 저장 한다.
        user.setPassword(passwordEncoder.encode(password));
        user.setThumbnailImg(thumbnailRelPath);
        //userRepository를 사용하여 user객체를 저장한다.
        this.userRepository.save(user);
        //생성된 user 객체를 반환 한다.
        return user;

    }

    //password 객체와 siteUser 객체를 파라미터로 받는다
    public void modifyUser(SiteUser siteUser, String password) {
        //siteUser객체에 암호화된 새로운 password를 저장한다.
        siteUser.setPassword(passwordEncoder.encode(password));
        //userRepository를 사용하여 수정된 siteUser 객체를 저장한다.
        this.userRepository.save(siteUser);
    }

    //username 객체를 파라미터로 받는다.
    public SiteUser getUser(String username) {
        //username을 통해 사용자 데이터를 찾고 Optional<SiteUser>타입의 siteUser 객체에 저장 한다.
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        //만약 siteUser의 값이 존재한다면
        if (siteUser.isPresent()) {
            //저장된 siteUser를 반환한다.
            return siteUser.get();
        } else {
            //siteUser not found 오류를 출력한다.
            throw new DataNotFoundException("siteuser not found");
        }
    }

//    public SiteUser getUserInfo(String username) {
//        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
//        if (siteUser.isPresent()) {
//            return siteUser.get();
//        } else {
//            throw new DataNotFoundException("SiteUser not found");
//        }
//    }



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
