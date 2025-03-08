package com.eaa.identity.service;

import com.eaa.identity.constants.Constants;
import com.eaa.identity.entity.UserInfo;
import com.eaa.identity.repository.UserInfoRepository;
import com.eaa.identity.request.ResetPasswordRequest;
import com.eaa.identity.request.UserRegistrationRequest;
import com.eaa.identity.request.UserUpdateRequest;
import com.eaa.identity.response.UserResponse;
import com.eaa.identity.utils.ServiceUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
                return response;
            }
            String verificationCode = UUID.randomUUID().toString();
            UserInfo newUser = populateUserData(request, verificationCode);
            userInfoRepository.save(newUser);
            response.setMessage(verificationEmail(verificationCode, ServiceUtils.applicationUrl(httpRequest), "/identity/verifyRegistration?verificationCode="));
            response.setStatus("Ok");

        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setStatus("Failed");
        }
        return response;
    }

    private UserInfo populateUserData(UserRegistrationRequest request, String verificationCode) {
        return new UserInfo(request.getUsername(),
                request.getEmail(),
                bcryptEncoder.encode(request.getPassword()),
                request.getRole(), verificationCode);
    }

    private String verificationEmail(String verificationCode, String applicationUrl, String api) {
        String url = applicationUrl + api + verificationCode;
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

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserResponse response = new UserResponse();
        try {
            UserInfo userInfo = userInfoRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            UserInfo updatedUser = populateUserUpdateData(request, userInfo);
            userInfoRepository.save(updatedUser);
            response.setMessage("User Updated Successfully");
            response.setStatus("Success");
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setStatus("Failed");
        }
        return response;
    }

    private UserInfo populateUserUpdateData(UserUpdateRequest request, UserInfo userInfo) {
        userInfo.setUsername(StringUtils.hasText(request.getUsername()) ? request.getUsername() : userInfo.getUsername());
        userInfo.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : userInfo.getEmail());
        userInfo.setPassword(StringUtils.hasText(request.getPassword()) ? bcryptEncoder.encode(request.getPassword()) : userInfo.getPassword());
        return userInfo;
    }

    public UserResponse forgotPassword(String email, HttpServletRequest httpRequest) {
        UserResponse response = new UserResponse();
        try {
            // 1. Check if email exists in database and user is enabled.
            UserInfo userInfo = userInfoRepository.findByEmail(email).filter(UserInfo::isEnabled)
                    .orElseThrow(() -> new UsernameNotFoundException("User is disabled or not found with email: " + email));

            // 2. Generate token and save it to the user
            String resetToken = UUID.randomUUID().toString();
            userInfo.setResetPasswordToken(resetToken);
            userInfo.setResetPasswordExpirationTime(ServiceUtils.calculateExpirationDate(Constants.RESET_PWD_EXPIRATION_TIME));
            userInfoRepository.save(userInfo);

            // 3. Send email with reset link
            response.setMessage(verificationEmail(resetToken, ServiceUtils.applicationUrl(httpRequest), "/identity/reset-password?token="));
            response.setStatus("Success");
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setStatus("Failed");
        }
        return response;
    }

    public UserResponse resetPasswordForm(String token) {
        UserResponse userResponse = new UserResponse();
        Calendar cal = Calendar.getInstance();
        try {
            //1. Validate token
            UserInfo userInfo = userInfoRepository.findByResetPasswordToken(token)
                    .filter(f -> f.getResetPasswordExpirationTime().getTime() - cal.getTime().getTime() > 0)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid reset password token: " + token));
            //2. If token is valid then return reset password form.
            userResponse.setStatus("Success");
            userResponse.setMessage("Valid reset password token: "+token);
        } catch (Exception e) {
            userResponse.setStatus("Failed");
            userResponse.setMessage(e.getMessage());
        }
        return userResponse;
    }

    public UserResponse resetPassword(ResetPasswordRequest request) {
        Calendar cal = Calendar.getInstance();
        UserResponse userResponse = new UserResponse();
        try {
            // 1. Validate token
            UserInfo userInfo = userInfoRepository.findByResetPasswordToken(request.getResetPasswordToken())
                    .filter(f -> f.getResetPasswordExpirationTime().getTime() - cal.getTime().getTime() > 0)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid reset password token: " + request.getResetPasswordToken()));
            // 2. Update user's password
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new Exception("the password you entered does not match with confirm password");
            }
            userInfo.setPassword(bcryptEncoder.encode(request.getNewPassword()));

            userInfoRepository.save(userInfo);
            userResponse.setStatus("Success");
            userResponse.setMessage("Password reset successfully.");
        } catch (Exception e) {
            userResponse.setStatus("Failed");
            userResponse.setMessage(e.getMessage());
        }
        return userResponse;
    }
}
