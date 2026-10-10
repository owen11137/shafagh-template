package com.misc.shafagh.cif.impl.config;

import org.springframework.data.domain.AuditorAware;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author m_kor
 * date 3/8/2025
 */
public class MyAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(List.of("test1", "test2", "test3").get((new Random()).nextInt(3)));
    }
}
