package com.misc.shafagh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
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
                        + "LOAN: مدیریت محصولات تسهیلات؛ DPST: سپرده که فعلاً کنترلر REST ندارد. "
                        + "از فهرست بالای صفحه، همه APIها یا ماژول موردنظر را انتخاب کنید.")
                .version("1.0.0"))
                .addTagsItem(new Tag().name("LOAN | محصولات تسهیلات").description("ProductLoanController؛ ثبت، دریافت، فهرست صفحه‌بندی‌شده، ویرایش و حذف محصول تسهیلات صندوق فعال"))
                .addTagsItem(new Tag().name("BASE | شهرها").description("CityController؛ مدیریت شهر و استان و اطلاعات جغرافیایی مشترک"))
                .addTagsItem(new Tag().name("BASE | صندوق‌ها").description("FundController؛ مدیریت صندوق و مالک آن در CIF"))
                .addTagsItem(new Tag().name("BASE | شعبه‌ها").description("BranchController؛ مدیریت شعبه‌های وابسته به صندوق"))
                .addTagsItem(new Tag().name("CIF | اشخاص حقیقی").description("CustomerController؛ ثبت، ویرایش و جست‌وجوی اشخاص حقیقی داخلی و خارجی"))
                .addTagsItem(new Tag().name("CIF | اشخاص حقوقی").description("CustomerController؛ ثبت، ویرایش و جست‌وجوی اشخاص حقوقی داخلی و خارجی"))
                .addTagsItem(new Tag().name("CIF | جست‌وجوی شناسه").description("CustomerController؛ دریافت مشتری بر اساس شناسه شناسایی"));
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
