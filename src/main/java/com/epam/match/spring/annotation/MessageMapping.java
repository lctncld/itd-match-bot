package com.epam.match.spring.annotation;

import com.epam.match.service.session.ProfileSetupStep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageMapping {

  String value() default "";

  ProfileSetupStep step() default ProfileSetupStep.UNKNOWN;

  TelegramUpdateType type() default TelegramUpdateType.MESSAGE;

}
