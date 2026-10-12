package com.misc.shafagh.cif.impl.config.security;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Legacy factory; HTTPS uses configured trust stores and normal hostname verification. */
public final class RestTemplateBypass {
    private RestTemplateBypass() {}

    @Deprecated
    public static RestTemplate createUnsafeRestTemplate() {
        return new RestTemplate(new SimpleClientHttpRequestFactory());
    }
}
