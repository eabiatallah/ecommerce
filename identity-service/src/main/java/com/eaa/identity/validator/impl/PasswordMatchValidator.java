package com.eaa.identity.validator.impl;

import com.eaa.identity.validator.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        Object password = new BeanWrapperImpl(obj).getPropertyValue("newPassword");
        Object confirmPassword = new BeanWrapperImpl(obj).getPropertyValue("confirmPassword");
        return password != null && password.equals(confirmPassword);
    }
}
