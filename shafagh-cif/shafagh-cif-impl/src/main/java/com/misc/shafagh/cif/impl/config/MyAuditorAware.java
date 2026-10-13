package com.misc.shafagh.cif.impl.config;
import com.misc.shafagh.common.context.CurrentUserContext;
import org.springframework.data.domain.AuditorAware;
import java.util.Optional;
/** Compatibility adapter; application auditing is configured centrally. */
public class MyAuditorAware implements AuditorAware<String> {
    private final CurrentUserContext context;
    public MyAuditorAware(CurrentUserContext context) { this.context = context; }
    public Optional<String> getCurrentAuditor() { return Optional.of(Long.toString(context.userId())); }
}
