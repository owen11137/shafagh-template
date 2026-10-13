package com.misc.shafagh.config;
import com.misc.shafagh.common.context.CurrentUserContext;
import com.misc.shafagh.common.context.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
/** Temporary application-wide identity until authentication is implemented. */
@AutoConfiguration(beforeName="org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration")
public class CurrentUserContextConfiguration {
    @Bean
    @ConditionalOnMissingBean(CurrentUserContext.class)
    CurrentUserContext temporaryCurrentUserContext(@Value("${shafagh.fund-id:1}") long fundId,
            @Value("${shafagh.user-id:1}") long userId, @Value("${shafagh.branch-id:1}") long branchId) {
        UserContext context = new UserContext(fundId, userId, branchId);
        return () -> context;
    }
}
