package com.eaa.identity.controller;

import com.eaa.identity.request.AuthRequest;
import com.eaa.identity.request.ResetPasswordRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.request.UserUpdateRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.service.IdentityService;
import com.eaa.identity.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity")
public class IdentityController {


    private final IdentityService service;


    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public IdentityController(IdentityService service, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.service = service;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/welcome2")
    public String welcome2(Authentication auth) {
        return "Welcome to this secured endpoint. The secure user is " + auth.getPrincipal() + " and Credentials " + auth.getCredentials();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest userRequest, HttpServletRequest httpRequest) {
        return ResponseEntity.ok().body(service.registerUser(userRequest, httpRequest));
    }

    @GetMapping("/verifyRegistration")
    public ResponseEntity<String> verifyRegistration(@RequestParam("verificationCode") String verificationCode) {
        return ResponseEntity.ok().body(service.verifyRegistration(verificationCode));
    }

    @PostMapping("/authenticate")
    public String authenticateUser(@RequestBody AuthRequest authRequest)  {
        String token = "";
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            if (authentication.isAuthenticated()) {
                token = jwtService.generateToken(authRequest.getUsername());
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return token;
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok().body(service.updateUser(id, request));
    }

    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<UserResponse> forgotPassword(@PathVariable String email, HttpServletRequest httpRequest) {
        return ResponseEntity.ok().body(service.forgotPassword(email, httpRequest) );
    }

    @GetMapping("/reset-password")
    public ResponseEntity<UserResponse> resetPasswordForm(@RequestParam("token") String token)  {
        return ResponseEntity.ok().body(service.resetPasswordForm(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok().body(service.resetPassword(request));
    }

}
