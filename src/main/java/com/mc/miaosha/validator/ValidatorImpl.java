package com.mc.miaosha.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;

    public ValidationResult validate(Object bean) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> validateSet = validator.validate(bean);
        if (validateSet.size() > 0) {
            result.setHasErrors(true);
            validateSet.forEach(constraintViolation -> {
                String errMsg = constraintViolation.getMessage();
                String errPathProperty = constraintViolation.getPropertyPath().toString();
                result.getErrMsgMap().put(errPathProperty,errMsg);
            });
        }
        return result;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
         this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
