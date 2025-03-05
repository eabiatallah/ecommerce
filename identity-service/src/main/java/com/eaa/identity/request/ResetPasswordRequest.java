package com.eaa.identity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResetPasswordRequest {
    private String newPassword;
    private String confirmPassword;
    private String resetPasswordToken;
}
