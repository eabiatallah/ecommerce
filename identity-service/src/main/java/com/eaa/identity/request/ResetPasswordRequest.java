package com.eaa.identity.request;

import com.eaa.identity.validator.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@PasswordMatch
public class ResetPasswordRequest {
    private String newPassword;
    private String confirmPassword;

    @NotBlank(message = "reset Password Token is required")
    private String resetPasswordToken;
}
