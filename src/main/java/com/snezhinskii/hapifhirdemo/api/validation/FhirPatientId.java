package com.snezhinskii.hapifhirdemo.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "patientId must not be blank")
@Pattern(regexp = "[a-zA-Z0-9\\-\\.]+", message = "PatientId must contain only alphanumeric characters")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Constraint(validatedBy = {})
public @interface FhirPatientId {
    String message() default "Invalid PatientId";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
