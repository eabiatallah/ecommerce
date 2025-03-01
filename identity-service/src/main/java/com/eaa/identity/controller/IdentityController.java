package com.eaa.identity.controller;

import com.eaa.identity.request.AuthRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.service.IdentityService;
import com.eaa.identity.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to this endpoint, is not secure";
    }

    @GetMapping("/welcome2")
    public String welcome2(Authentication auth) {

        System.out.println("--- Secure Authorities ----" + auth.getAuthorities());
        System.out.println("--- Secure Details ----" + auth.getDetails());
        System.out.println("--- Secure Name ----" + auth.getName());
        return "Welcome to this secured endpoint. The secure user is "+auth.getPrincipal()+" and Credentials "+auth.getCredentials();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> userRegistration(@RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok().body(service.registerUser(request));
    }

    @PostMapping("/authenticate")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if(authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getUsername());
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }

    }
}
