package rniesler.gphotoshare.utils;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOauth2UserSecurityContextFactory.class)
public @interface WithMockOauth2User {
    String value() default "test@test";
}
