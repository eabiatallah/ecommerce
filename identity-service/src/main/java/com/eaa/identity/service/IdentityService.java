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

import static com.eaa.identity.constants.Constants.SUCCESS;

@Service
@Slf4j
public class IdentityService {

    private final UserInfoRepository userInfoRepository;

    private final PasswordEncoder bcryptEncoder;

    @Autowired
    public IdentityService(UserInfoRepository userInfoRepository, PasswordEncoder bcryptEncoder) {
        this.userInfoRepository = userInfoRepository;
        this.bcryptEncoder = bcryptEncoder;
    }

    public UserResponse registerUser(UserRegistrationRequest request, HttpServletRequest httpRequest) {
        UserResponse response = new UserResponse();
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
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        UserInfo updatedUser = populateUserUpdateData(request, userInfo);
        userInfoRepository.save(updatedUser);
        response.setMessage("User Updated Successfully");
        response.setStatus(SUCCESS);
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
        UserInfo userInfo = userInfoRepository.findByEmail(email).filter(UserInfo::isEnabled)
                .orElseThrow(() -> new UsernameNotFoundException("User is disabled or not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        userInfo.setResetPasswordToken(resetToken);
        userInfo.setResetPasswordExpirationTime(ServiceUtils.calculateExpirationDate(Constants.RESET_PWD_EXPIRATION_TIME));
        userInfoRepository.save(userInfo);
        response.setMessage(verificationEmail(resetToken, ServiceUtils.applicationUrl(httpRequest), "/identity/reset-password?token="));
        response.setStatus(SUCCESS);
        return response;
    }

    public UserResponse resetPasswordForm(String token) {
        UserResponse userResponse = new UserResponse();
        Calendar cal = Calendar.getInstance();

        userInfoRepository.findByResetPasswordToken(token)
                .filter(f -> f.getResetPasswordExpirationTime().getTime() - cal.getTime().getTime() > 0)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid reset password token: " + token));
        userResponse.setStatus("Success");
        userResponse.setMessage("Valid reset password token: " + token);
        return userResponse;
    }

    public UserResponse resetPassword(ResetPasswordRequest request) {
        Calendar cal = Calendar.getInstance();
        UserResponse userResponse = new UserResponse();

        UserInfo userInfo = userInfoRepository.findByResetPasswordToken(request.getResetPasswordToken())
                .filter(f -> f.getResetPasswordExpirationTime().getTime() - cal.getTime().getTime() > 0)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid reset password token: " + request.getResetPasswordToken()));
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new UsernameNotFoundException("the password you entered does not match with confirm password");
        }
        userInfo.setPassword(bcryptEncoder.encode(request.getNewPassword()));
        userInfoRepository.save(userInfo);
        userResponse.setStatus("Success");
        userResponse.setMessage("Password reset successfully.");

        return userResponse;
    }
}
