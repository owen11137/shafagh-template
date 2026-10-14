package com.misc.shafagh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

/** توضیحات DTOها و پارامترهای صفحه‌بندی؛ توضیحات عملیات و دسته‌بندی در annotation کنترلرها هستند. */
@Component
public class OpenApiDocumentation implements GlobalOpenApiCustomizer {
    @Override
    public void customise(OpenAPI api) {
        if (api.getPaths() != null) {
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                if (isPaged(path, method.name())) documentPagination(operation);
                if (operation.getParameters() != null) {
                    operation.getParameters().forEach(parameter -> {
                        String text = switch (parameter.getName()) {
                            case "id" -> "شناسه رکورد موردنظر؛ در ویرایش، شناسه مسیر ملاک است.";
                            case "identifier" -> "شناسه شناسایی مشتری ثبت‌شده در CIF.";
                            case "page" -> "شماره صفحه از صفر؛ پیش‌فرض صفر است.";
                            case "size" -> "تعداد رکوردهای هر صفحه؛ پیش‌فرض ۲۰ است.";
                            case "sort" -> "مرتب‌سازی به شکل نام‌فیلد,asc یا نام‌فیلد,desc؛ قابل تکرار است.";
                            default -> parameter.getDescription();
                        };
                        parameter.setDescription(text);
                    });
                }
            }));
        }
        if (api.getComponents() != null && api.getComponents().getSchemas() != null) {
            api.getComponents().getSchemas().forEach(this::describeSchema);
        }
    }

    private boolean isPaged(String path, String method) {
        return method.equals("GET") && List.of("/base/city", "/base/fund", "/base/branch", "/dpst/products", "/loan/products").contains(path)
            || method.equals("POST") && List.of("/customer/real/filter", "/customer/legal/filter").contains(path);
    }

    /** Explicit query parameters keep the Swagger form consistent across every paged endpoint. */
    private void documentPagination(Operation operation) {
        var parameters = operation.getParameters() == null ? new java.util.ArrayList<io.swagger.v3.oas.models.parameters.Parameter>()
            : new java.util.ArrayList<>(operation.getParameters());
        parameters.removeIf(parameter -> List.of("page", "size", "sort", "pageable").contains(parameter.getName()));
        parameters.add(new io.swagger.v3.oas.models.parameters.Parameter().name("page").in("query").required(false)
            .description("شماره صفحه از صفر؛ پیش‌فرض صفر.").schema(new io.swagger.v3.oas.models.media.IntegerSchema().minimum(java.math.BigDecimal.ZERO)._default(0)));
        parameters.add(new io.swagger.v3.oas.models.parameters.Parameter().name("size").in("query").required(false)
            .description("تعداد رکورد در صفحه؛ پیش‌فرض ۲۰، حداکثر ۲۰۰۰.").schema(new io.swagger.v3.oas.models.media.IntegerSchema().minimum(java.math.BigDecimal.ONE).maximum(java.math.BigDecimal.valueOf(2000))._default(20)));
        parameters.add(new io.swagger.v3.oas.models.parameters.Parameter().name("sort").in("query").required(false)
            .description("مرتب‌سازی مانند id,asc یا title,desc؛ برای چند فیلد پارامتر را تکرار کنید.")
            .style(io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM).explode(true)
            .schema(new io.swagger.v3.oas.models.media.ArraySchema().items(new io.swagger.v3.oas.models.media.StringSchema())._default(List.of("id,asc"))));
        operation.setParameters(parameters);
    }

    private void describeSchema(String name, Schema schema) {
        String description = switch (name) {
            case "ProductDepositReqDTO" -> "DPST ـ ورودی ثبت یا ویرایش محصول سپرده؛ کد، نوع سپرده و وضعیت الزامی‌اند.";
            case "ProductDepositResponseDto" -> "DPST ـ مشخصات محصول سپرده همراه صندوق و اطلاعات ثبت و ویرایش.";
            case "ProductLoanReqDTO" -> "LOAN ـ ورودی ثبت یا ویرایش کامل محصول تسهیلات؛ فاقد شناسه و اطلاعات سیستمی.";
            case "ProductLoanResponseDto" -> "LOAN ـ مشخصات محصول تسهیلات همراه صندوق، auditing و version.";
            case "CityDto" -> "BASE ـ مشخصات شهر یا استان و نام استان والد.";
            case "FundDto" -> "BASE ـ مشخصات صندوق و شناسه مشتری مالک در CIF.";
            case "BranchDto" -> "BASE ـ مشخصات شعبه و شناسه صندوق مرتبط.";
            case "CustomerReqDTO" -> "CIF ـ بدنه مشترک ثبت و ویرایش شخص حقیقی و حقوقی؛ الزامات وابسته به نوع شخص در توضیح عملیات آمده‌اند.";
            case "CustomerFilterDTO" -> "CIF ـ فیلترهای اختیاری جست‌وجوی مشتری؛ فیلد خالی در شرط جست‌وجو لحاظ نمی‌شود.";
            case "CustomerDto" -> "CIF ـ پاسخ مشتری همراه مشخصات شهر، آدرس‌ها، تماس‌ها و امضاها.";
            case "CustomerAddressReqDTO" -> "CIF ـ آدرس مشتری؛ cityId و addressType الزامی هستند.";
            case "CustomerAddressDto" -> "CIF ـ آدرس ثبت‌شده مشتری همراه مشخصات شهر.";
            case "CustomerContactReqDTO", "CustomerContactDto" -> "CIF ـ اطلاعات تماس مشتری شامل شماره همراه و تلفن.";
            case "SignatureReqDTO" -> "CIF ـ اطلاعات امضا؛ تصویر درخواست رشته Base64 است.";
            case "SignatureDto" -> "CIF ـ امضای مشتری؛ تصویر پاسخ به صورت Base64 در JSON نمایش داده می‌شود.";
            default -> null;
        };
        if (name.equals("OpenDepositRequest")) description = "ورودی افتتاح حساب: nationalCode شماره ملی ده رقمی و productCode کد محصول سپرده؛ description توضیحات اختیاری تا ۲۵۵ کاراکتر.";
        if (name.equals("OpenDepositResponse")) description = "نتیجه افتتاح: شناسه، شماره حساب داخلی، مشتری، محصول، وضعیت، مانده و توضیحات.";
        if (description == null) return;
        schema.setDescription(description);
        if (schema.getProperties() == null) return;
        Map<String, String> fields = Map.ofEntries(
                Map.entry("id", "شناسه رکورد؛ برای ثبت جدید ارسال نشود و برای ویرایش مشتری الزامی است."),
                Map.entry("name", "نام شهر، صندوق یا شعبه؛ در ثبت و ویرایش الزامی است."),
                Map.entry("code", "کد رکورد؛ برای صندوق و شعبه الزامی است."),
                Map.entry("phoneCode", "پیش‌شماره تلفن شهر."),
                Map.entry("parentId", "شناسه والد جغرافیایی؛ مقدار صفر برای استان."),
                Map.entry("province", "نام استان والد؛ در پاسخ محاسبه می‌شود."),
                Map.entry("ownerId", "شناسه مشتری موجود در CIF به عنوان مالک صندوق."),
                Map.entry("fundId", name.equals("BranchDto") ? "شناسه صندوق جاری شعبه؛ هنگام ثبت خالی باشد از تنظیمات گرفته می‌شود؛ صندوق دیگر مجاز نیست." : "شناسه صندوق فعال؛ در ثبت و ویرایش مشتری توسط سرویس تعیین می‌شود."),
                Map.entry("toDate", "تاریخ پایان اعتبار رکورد یا امضا."),
                Map.entry("identifier", "شناسه شناسایی مشتری؛ در ثبت الزامی و یکتا است."),
                Map.entry("customerType", "نوع شخص؛ نوع مجاز باید با سرویس حقیقی یا حقوقی مطابقت داشته باشد."),
                Map.entry("registrationDate", "تاریخ ثبت مشتری؛ در ثبت و ویرایش الزامی است."),
                Map.entry("cityId", "شناسه شهر یا استان موجود در BASE."),
                Map.entry("city", "مشخصات شهر و استان برگرفته از BASE."),
                Map.entry("customerCode", "کد مشتری؛ هنگام ثبت تولید و هنگام ویرایش حفظ می‌شود."),
                Map.entry("customerStatus", "وضعیت مشتری؛ سرویس ثبت و ویرایش مقدار ACTIVE تعیین می‌کند."),
                Map.entry("firstName", "نام؛ برای شخص حقیقی الزامی است."),
                Map.entry("lastName", "نام خانوادگی؛ برای شخص حقیقی الزامی است."),
                Map.entry("fatherName", "نام پدر؛ برای شخص حقیقی الزامی است."),
                Map.entry("gender", "جنسیت؛ برای شخص حقیقی الزامی است."),
                Map.entry("maritalStatus", "وضعیت تأهل؛ برای شخص حقیقی الزامی است."),
                Map.entry("deathDate", "تاریخ فوت، در صورت وجود."),
                Map.entry("shahabCode", "کد شهاب مشتری، در صورت وجود."),
                Map.entry("addresses", "آدرس‌های مشتری؛ حداقل یک آدرس در ثبت و ویرایش لازم است."),
                Map.entry("contacts", "راه‌های ارتباطی؛ حداقل یک مورد در ثبت و ویرایش لازم است."),
                Map.entry("signatures", "امضاهای مشتری؛ تصویر هر امضا رشته Base64 است."),
                Map.entry("addressType", "نوع آدرس؛ برای شخص حقوقی هنگام ثبت WORK_PLACE تعیین می‌شود."),
                Map.entry("address", "متن نشانی پستی."),
                Map.entry("postalCode", "کد پستی نشانی."),
                Map.entry("phoneNumber", "شماره تلفن ثابت."),
                Map.entry("mobileNumber", "شماره تلفن همراه."),
                Map.entry("customerId", "شناسه مشتری صاحب امضا؛ ارتباط هنگام ذخیره با مشتری برقرار می‌شود."),
                Map.entry("fromDate", "تاریخ شروع اعتبار امضا."),
                Map.entry("description", "توضیحات تکمیلی امضا."),
                Map.entry("image", "تصویر امضا به صورت رشته Base64."));
        schema.getProperties().forEach((field, property) -> {
            String text = fields.get(field.toString());
            if (name.startsWith("ProductLoan")) {
                text = switch (field.toString()) {
                    case "title" -> "عنوان محصول؛ حداکثر ۳۰۰ کاراکتر.";
                    case "code" -> "کد یکتا در کل اسکیما؛ الزامی، حداکثر ۲۰ کاراکتر.";
                    case "description" -> "شرح محصول؛ حداکثر ۳۰۰ کاراکتر.";
                    case "productType" -> "نوع محصول؛ ADI یا VOJOH، الزامی.";
                    case "earlySettlementAllowed" -> "امکان تسویه پیش از موعد؛ ارسال true یا false الزامی است.";
                    case "fromDate", "toDate" -> "تاریخ شروع/پایان اعتبار؛ پایان نباید قبل از شروع باشد.";
                    case "status" -> "وضعیت محصول مطابق مقادیر ProductStatus.";
                    case "minAmount", "maxAmount" -> "حداقل/حداکثر مبلغ تسهیلات؛ نامنفی و حداقل <= حداکثر.";
                    case "minFee", "maxFee" -> "حداقل/حداکثر کارمزد؛ نامنفی و حداقل <= حداکثر.";
                    case "minFine", "maxFine" -> "حداقل/حداکثر جریمه؛ نامنفی و حداقل <= حداکثر.";
                    case "installmentInterval" -> "فاصله اقساط؛ عدد نامنفی، بدون تعیین واحد در مدل فعلی.";
                    case "minInstallmentCount", "maxInstallmentCount" -> "حداقل/حداکثر تعداد اقساط؛ نامنفی و حداقل <= حداکثر.";
                    case "fundId" -> "شناسه صندوق فعال؛ از ورودی دریافت نمی‌شود.";
                    case "version" -> "نسخه قفل خوش‌بینانه؛ توسط Hibernate تعیین می‌شود.";
                    case "createdBy", "createdOn", "modifiedBy", "modifiedOn" -> "اطلاعات ثبت یا ویرایش که توسط auditing برنامه تعیین می‌شود.";
                    default -> text;
                };
            }
            if (name.startsWith("ProductDeposit")) {
                text = switch (field.toString()) {
                    case "id" -> "شناسه محصول؛ در پاسخ برگردانده می‌شود و در ویرایش از مسیر گرفته می‌شود.";
                    case "code" -> "کد یکتا در اسکیما؛ الزامی، حداکثر ۲۵۵ کاراکتر؛ برای محصول دارای حساب قابل تغییر نیست.";
                    case "title" -> "عنوان اختیاری محصول؛ حداکثر ۲۵۵ کاراکتر.";
                    case "description" -> "توضیحات اختیاری محصول؛ حداکثر ۲۵۵ کاراکتر.";
                    case "depositType" -> "نوع سپرده؛ DEPOSIT1 یا DEPOSIT2، الزامی.";
                    case "status" -> "وضعیت محصول؛ ACTIVE یا INACTIVE، الزامی.";
                    case "fromDate" -> "ابتدای بازه اعتبار؛ اختیاری، شامل تاریخ شروع.";
                    case "toDate" -> "انتهای بازه اعتبار؛ اختیاری، نباید قبل از شروع باشد؛ انتهای بازه شامل نمی‌شود.";
                    case "fundId" -> "صندوق فعال؛ توسط برنامه تعیین می‌شود.";
                    case "version" -> "نسخه قفل خوش‌بینانه؛ توسط Hibernate تعیین می‌شود.";
                    case "createdBy", "createdOn", "modifiedBy", "modifiedOn" -> "اطلاعات ثبت و ویرایش؛ توسط auditing برنامه تعیین می‌شود.";
                    default -> text;
                };
            }
            if (name.equals("CustomerFilterDTO")) {
                text = switch (field.toString()) {
                    case "identifier", "customerCode", "shahabCode", "firstName", "lastName", "fatherName" -> "فیلتر اختیاری متن؛ تطابق شامل عبارت ارسالی و بدون حساسیت به حروف بزرگ و کوچک است.";
                    case "customerType" -> "فیلتر اختیاری نوع شخص؛ باید با سرویس حقیقی یا حقوقی مطابقت داشته باشد.";
                    default -> "فیلتر اختیاری " + field + "؛ تطابق دقیق مقدار ارسالی، بدون مقدار به معنی عدم اعمال شرط است.";
                };
            } else if (field.toString().equals("id") && name.endsWith("Dto")) {
                text = name.equals("CityDto") || name.equals("FundDto") || name.equals("BranchDto")
                        ? "شناسه رکورد در پاسخ؛ در ثبت ارسال نشود. در ویرایش، شناسه مسیر ملاک است."
                        : "شناسه رکورد ثبت‌شده.";
            }
            if (text != null && property instanceof Schema fieldSchema) fieldSchema.setDescription(text);
        });
    }
}
