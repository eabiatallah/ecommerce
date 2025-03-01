package com.eaa.identity.controller;

import com.eaa.identity.request.AuthRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.service.IdentityService;
import com.eaa.identity.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
        return "Welcome to this secured endpoint. The secure user is " + auth.getPrincipal() + " and Credentials " + auth.getCredentials();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegistrationRequest userRequest, HttpServletRequest httpRequest) {
        return ResponseEntity.ok().body(service.registerUser(userRequest, httpRequest));
    }

    @GetMapping("/verifyRegistration")
    public ResponseEntity<String> verifyRegistration(@RequestParam("verificationCode") String verificationCode) throws Exception {
        return ResponseEntity.ok().body(service.verifyRegistration(verificationCode));
    }

    @PostMapping("/authenticate")
    public String authenticateUser(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (DisabledException e) {
            return e.getMessage();
        } catch (BadCredentialsException e) {
            return e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return jwtService.generateToken(authRequest.getUsername());
    }

}
