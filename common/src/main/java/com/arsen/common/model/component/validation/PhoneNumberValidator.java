package com.arsen.common.model.component.validation;

import com.arsen.common.model.component.annotation.CellPhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<CellPhoneNumber, String> {
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private String defaultRegion;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            Phonenumber.PhoneNumber parsed = phoneNumberUtil.parse(value, defaultRegion);
            phoneNumberUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
            return phoneNumberUtil.isValidNumber(parsed);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void initialize(CellPhoneNumber constraintAnnotation) {
        defaultRegion = constraintAnnotation.defaultRegion();
    }
}
