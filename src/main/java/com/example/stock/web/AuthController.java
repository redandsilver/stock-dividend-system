package com.example.stock.web;

import com.example.stock.model.Auth;
import com.example.stock.security.TokenProvider;
import com.example.stock.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    // 회원가입을 위한 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup (@RequestBody Auth.SignUp request){
        var result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }
    // 로그인을 위한 API
    @PostMapping("/signin")
    public ResponseEntity<?> signin (@RequestBody Auth.SignIn request){
        // password 인증
        var memeber
                = this.memberService.authenticate(request);
        var token
                = this.tokenProvider.generateToken(
                        memeber.getUsername(),memeber.getRoles());
        log.info("user login -> "+request.getUsername());
        return ResponseEntity.ok(token);
    }
}
