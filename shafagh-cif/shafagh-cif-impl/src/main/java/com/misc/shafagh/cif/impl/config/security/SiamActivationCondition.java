package com.misc.shafagh.cif.impl.config.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * @author m_kor
 * date 12/2/2025
 */
public class SiamActivationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        return Objects.equals(environment.getProperty("siam.active-service-name"), "identifier") || Objects.equals(environment.getProperty("siam.active-service-name"), "shahabCode");
    }
}
