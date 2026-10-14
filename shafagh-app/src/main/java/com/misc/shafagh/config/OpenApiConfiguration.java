package com.misc.shafagh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** مستندات مشترک REST؛ دسته‌بندی ماژول‌ها بدون وابسته کردن قراردادهای دامنه به Swagger. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI shafaghOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Shafagh API")
                .description("مستندات API شفق. BASE: شهر، شعبه و صندوق؛ CIF: مشتری حقیقی و حقوقی. "
                        + "LOAN: مدیریت محصولات تسهیلات؛ DPST: محصولات و افتتاح حساب سپرده. "
                        + "از فهرست بالای صفحه، همه APIها یا ماژول موردنظر را انتخاب کنید.")
                .version("1.0.0"));
    }

    @Bean GroupedOpenApi allModules() { return group("00-all", "همه ماژول‌ها", "/**"); }
    @Bean GroupedOpenApi baseModule() { return group("01-base", "BASE ـ اطلاعات پایه", "/base/**"); }
    @Bean GroupedOpenApi cifModule() { return group("02-cif", "CIF ـ مشتریان", "/customer/**"); }
    @Bean GroupedOpenApi depositModule() { return group("03-dpst", "DPST ـ سپرده", "/dpst/**"); }
    @Bean GroupedOpenApi loanModule() { return group("04-loan", "LOAN ـ تسهیلات", "/loan/**"); }

    private GroupedOpenApi group(String name, String label, String path) {
        return GroupedOpenApi.builder().group(name).displayName(label).pathsToMatch(path).build();
    }
}
