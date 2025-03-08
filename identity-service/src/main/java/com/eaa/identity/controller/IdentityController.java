package com.eaa.identity.controller;

import com.eaa.identity.request.AuthRequest;
import com.eaa.identity.request.ResetPasswordRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.request.UserUpdateRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.service.IdentityService;
import com.eaa.identity.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    /* New users should be able to create accounts by providing necessary information such as username, password, and email.
      Spring Security can be used to handle password encoding and storage.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegistrationRequest userRequest, HttpServletRequest httpRequest) {
        return ResponseEntity.ok().body(service.registerUser(userRequest, httpRequest));
    }

    /* New users may need to activate their accounts through email verification before they can log in.*/
    @GetMapping("/verifyRegistration")
    public ResponseEntity<String> verifyRegistration(@RequestParam("verificationCode") String verificationCode) throws Exception {
        return ResponseEntity.ok().body(service.verifyRegistration(verificationCode));
    }

    /* Registered users should be able to log in securely using their credentials.
       Spring Security's authentication mechanisms can be employed to verify user identities.
    */
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

    // update User info
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok().body(service.updateUser(id, request));
    }

    /* click forget password link in UI, pop up will open and asking for you email address,
     if valid email reset token will be generated and sent to this email.*/
    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<UserResponse> forgotPassword(@PathVariable String email, HttpServletRequest httpRequest) {
        return ResponseEntity.ok().body(service.forgotPassword(email, httpRequest));
    }

    // link sent from /forgot-password to user email, if valid allow user to reset-password
    @GetMapping("/reset-password")
    public ResponseEntity<UserResponse> resetPasswordForm(@RequestParam("token") String token) throws Exception {
        return ResponseEntity.ok().body(service.resetPasswordForm(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok().body(service.resetPassword(request));
    }

    // TO DO: We can add user Address, as well. One User can have multiple addresses.

}
