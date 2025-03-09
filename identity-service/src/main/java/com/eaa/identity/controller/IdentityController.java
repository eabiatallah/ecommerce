package com.eaa.identity.controller;

import com.eaa.identity.request.AuthRequest;
import com.eaa.identity.request.ResetPasswordRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.request.UserUpdateRequest;
import com.eaa.identity.service.IdentityService;
import com.eaa.identity.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity")
public class IdentityController {

    @Autowired
    private IdentityService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/welcome2")
    public String welcome2(Authentication auth) {
        return "Welcome to this secured endpoint. The secure user is " + auth.getPrincipal() + " and Credentials " + auth.getCredentials();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest userRequest, HttpServletRequest httpRequest) {
        return new ResponseEntity<>(service.registerUser(userRequest, httpRequest),  HttpStatus.CREATED);
    }

    @GetMapping("/verifyRegistration")
    public ResponseEntity<?> verifyRegistration(@RequestParam("verificationCode") String verificationCode) throws Exception {
        return new ResponseEntity<>(service.verifyRegistration(verificationCode), HttpStatus.CREATED);
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
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return new ResponseEntity<>(service.updateUser(id, request), HttpStatus.CREATED);
    }

    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<?> forgotPassword(@PathVariable String email, HttpServletRequest httpRequest) {
        return new ResponseEntity<>(service.forgotPassword(email, httpRequest), HttpStatus.CREATED);
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPasswordForm(@RequestParam("token") String token) throws Exception {
        return new ResponseEntity<>(service.resetPasswordForm(token), HttpStatus.CREATED);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return new ResponseEntity<>(service.resetPassword(request), HttpStatus.CREATED);
    }

}
