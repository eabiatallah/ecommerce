package com.eaa.identity.service;

import com.eaa.identity.entity.UserInfo;
import com.eaa.identity.repository.UserInfoRepository;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class IdentityService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public UserResponse registerUser(UserRegistrationRequest request) {
        UserResponse response = new UserResponse();
        try {
            Optional<UserInfo> user = userInfoRepository.findByEmail(request.getEmail());
            if (user.isPresent()) {
                response.setMessage("User Already Exists");
                response.setStatus("Ok");
            }
            UserInfo newUser = populateUserData(request);
            userInfoRepository.save(newUser);
            response.setMessage("User Successfully Added");
            response.setStatus("OK");

        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setStatus("Server Error");
        }
        return response;
    }

    private UserInfo populateUserData(UserRegistrationRequest request) {
        UserInfo userInfo = new UserInfo(request.getUsername(), request.getEmail(),
                bcryptEncoder.encode(request.getPassword()), request.getRole());
        return userInfo;
    }


}
