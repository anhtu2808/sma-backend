package com.sma.core.controller;


import com.sma.core.dto.response.ApiResponse;
import com.sma.core.service.AuthService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/v1/auth")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {

    AuthService authService;

//    @PostMapping("/register")
//    public ApiResponse<> register(){
//
//    }
//
//    @PostMapping("/google-login")
//    public ApiResponse<> loginWithGoogle(@RequestParam("idToken") String idToken) {
//
//    }

}
