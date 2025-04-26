package com.example.demo.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the validator for the EnumValue annotation
 * Verifies that a String or Enum value is valid for a specific enumeration
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {
    
    private Set<String> allowedValues;
    
    @Override
    public void initialize(EnumValue constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        allowedValues = Arrays.stream(enumClass.getEnumConstants())
                              .map(Enum::name)
                              .collect(Collectors.toSet());
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        if (value instanceof Enum) {
            return allowedValues.contains(((Enum<?>) value).name());
        } else if (value instanceof String) {
            return allowedValues.contains(value);
        }
        
        return false;
    }
}