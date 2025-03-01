package com.eaa.identity.service;

import com.eaa.identity.entity.UserInfo;
import com.eaa.identity.repository.UserInfoRepository;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.utils.ServiceUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class IdentityService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public UserResponse registerUser(UserRegistrationRequest request, HttpServletRequest httpRequest) {
        UserResponse response = new UserResponse();
        try {
            Optional<UserInfo> user = userInfoRepository.findByEmail(request.getEmail());
            if (user.isPresent()) {
                response.setMessage("User Already Exists");
                response.setStatus("Ok");
                return  response;
            }
            String verificationCode = UUID.randomUUID().toString();
            UserInfo newUser = populateUserData(request, verificationCode);
            userInfoRepository.save(newUser);
            response.setMessage(verificationEmail(verificationCode, ServiceUtils.applicationUrl(httpRequest)));
            response.setStatus("OK");

        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setStatus("Server Error");
        }
        return response;
    }

    private UserInfo populateUserData(UserRegistrationRequest request, String verificationCode) {
        UserInfo userInfo = new UserInfo(request.getUsername(),
                request.getEmail(),
                bcryptEncoder.encode(request.getPassword()),
                request.getRole(), verificationCode);
        return userInfo;
    }

    private String verificationEmail(String verificationCode, String applicationUrl) {
        String url = applicationUrl + "/identity/verifyRegistration?verificationCode=" + verificationCode;
        log.info("Click link to enable your account: {}", url);
        return url;
    }

    public String verifyRegistration(String code) {
        Calendar cal = Calendar.getInstance();
        return userInfoRepository.findByVerificationCode(code).filter(f -> f.getExpirationTime().getTime() - cal.getTime().getTime() > 0)
                .map(entity -> {
                    entity.setEnabled(true);
                    userInfoRepository.save(entity);
                    return "Update successful";
                })
                .orElse("Verification Code not Found Or Expired:" + code);
    }

}
