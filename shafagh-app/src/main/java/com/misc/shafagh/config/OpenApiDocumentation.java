package com.misc.shafagh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

/** توضیحات فارسی عملیات و DTOها برای سند اصلی و تمام گروه‌های ماژول‌ها. */
@Component
public class OpenApiDocumentation implements GlobalOpenApiCustomizer {
    @Override
    public void customise(OpenAPI api) {
        if (api.getPaths() != null) {
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                describeOperation(path, method.name(), operation);
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

    private void describeOperation(String path, String method, Operation operation) {
        if (path.startsWith("/base/")) {
            String resource = path.split("/")[2];
            String label = switch (resource) {
                case "city" -> "شهر";
                case "fund" -> "صندوق";
                case "branch" -> "شعبه";
                default -> resource;
            };
            String tag = switch (resource) {
                case "city" -> "BASE | شهرها";
                case "fund" -> "BASE | صندوق‌ها";
                case "branch" -> "BASE | شعبه‌ها";
                default -> "BASE";
            };
            operation.setTags(List.of(tag));
            String details = switch (resource) {
                case "city" -> "نام شهر الزامی است. parentId=0 معرف استان است؛ برای شهر، parentId شناسه والد موجود است.";
                case "fund" -> "کد و نام صندوق الزامی‌اند. ownerId باید شناسه مشتری موجود در CIF و صندوق فعال برنامه باشد.";
                case "branch" -> "کد و نام شعبه الزامی‌اند. fundId باید شناسه صندوق موجود باشد. کد شعبه در هر صندوق یکتا است.";
                default -> "";
            };
            switch (method) {
                case "GET" -> {
                    boolean single = path.endsWith("/{id}");
                    operation.setSummary(single ? "دریافت " + label + " با شناسه" : "فهرست " + label + "ها");
                    operation.setDescription(single ? "مشخصات رکورد با شناسه مسیر را برمی‌گرداند؛ شناسه ناموجود پاسخ 404 دارد."
                            : resource.equals("city") ? "فهرست شهرها همراه نام استان؛ رکوردهای استان با parentId=0 در این فهرست نمی‌آیند."
                            : "فهرست همه رکوردهای ثبت‌شده؛ خروجی فعلی صفحه‌بندی ندارد.");
                }
                case "POST" -> {
                    operation.setSummary("ثبت " + label + " جدید");
                    operation.setDescription("در بدنه درخواست id نفرستید؛ شناسه هنگام ثبت تولید می‌شود. " + details);
                }
                case "PUT" -> {
                    operation.setSummary("ویرایش " + label);
                    operation.setDescription("رکورد با شناسه مسیر ویرایش می‌شود؛ شناسه رکورد تغییر نمی‌کند. " + details);
                }
                default -> { }
            }
            if (!method.equals("GET")) {
                operation.getRequestBody().setDescription("اطلاعات " + label + "؛ فیلدها در Schema بدنه توضیح داده شده‌اند.");
                operation.getResponses().addApiResponse("400", new ApiResponse().description("نام یا کد لازم ارسال نشده، یا شناسه در ثبت جدید ارسال شده است."))
                        .addApiResponse("404", new ApiResponse().description("رکورد یا مرجع شهر، مشتری مالک یا صندوق پیدا نشد."));
            } else if (path.endsWith("/{id}")) {
                operation.getResponses().addApiResponse("404", new ApiResponse().description("رکورد با شناسه درخواست‌شده وجود ندارد."));
            }
        } else if (path.startsWith("/customer/")) {
            boolean real = path.startsWith("/customer/real/");
            boolean legal = path.startsWith("/customer/legal/");
            String label = real ? "حقیقی" : "حقوقی";
            operation.setTags(List.of(real ? "CIF | اشخاص حقیقی" : legal ? "CIF | اشخاص حقوقی" : "CIF | جست‌وجوی شناسه"));
            if (path.endsWith("/save")) {
                operation.setSummary("ثبت شخص " + label);
                operation.setDescription("id نفرستید. شناسه شناسایی یکتا، نوع شخص، تاریخ ثبت، شهر موجود، حداقل یک آدرس و یک راه ارتباطی لازم است. "
                        + "صندوق فعال و وضعیت ACTIVE توسط سرویس تعیین می‌شوند. "
                        + (real ? "نوع REAL یا FOREIGN_REAL؛ نام، نام خانوادگی، نام پدر، جنسیت و وضعیت تأهل الزامی‌اند."
                        : "نوع LEGAL یا FOREIGN_LEGAL؛ هنگام ثبت، نوع آدرس WORK_PLACE تعیین می‌شود."));
            } else if (path.endsWith("/update")) {
                operation.setSummary("ویرایش شخص " + label);
                operation.setDescription("id مشتری در بدنه الزامی است. اطلاعات کامل موردنیاز ثبت را ارسال کنید؛ customerCode حفظ می‌شود. "
                        + "شناسه آدرس و تماس موجود باید متعلق به همین مشتری باشد؛ اعضای حذف‌شده از فهرست آدرس و تماس حذف می‌شوند.");
            } else if (path.contains("/getById/")) {
                operation.setSummary("دریافت شخص " + label + " با شناسه");
                operation.setDescription("مشخصات مشتری از نوع مجاز همین سرویس، همراه شهر، آدرس‌ها، تماس‌ها و امضاها را برمی‌گرداند.");
            } else if (path.endsWith("/filter")) {
                operation.setSummary("جست‌وجوی اشخاص " + label);
                operation.setDescription("فیلترهای بدنه اختیاری‌اند؛ فقط انواع شخص مجاز همین سرویس جست‌وجو می‌شوند. "
                        + "page، size و sort را در query ارسال کنید؛ خروجی شامل رکوردها و اطلاعات صفحه‌بندی است.");
            } else {
                operation.setSummary("دریافت مشتری با شناسه شناسایی");
                operation.setDescription("identifier را در مسیر ارسال کنید. پیاده‌سازی فعلی بر اساس شناسه جست‌وجو می‌کند و نوع شخص را محدود نمی‌کند؛ اگر مشتری وجود نداشته باشد، بدنه پاسخ خالی است.");
            }
            if (operation.getRequestBody() != null) {
                operation.getRequestBody().setDescription(path.endsWith("/filter") ? "فیلترهای جست‌وجو؛ برای جست‌وجوی بدون فیلتر، {} ارسال کنید." : "اطلاعات مشتری و فهرست‌های آدرس، تماس و امضا.");
            }
        }
        if (path.equals("/dpst/deposits")) {
            operation.setTags(List.of("DPST | افتتاح حساب"));
            operation.setSummary("افتتاح حساب سپرده");
            operation.setDescription("شماره ملی معتبر مشتری حقیقی فعال و productCode محصول فعال در صندوق جاری را ارسال کنید. حساب با مانده صفر و وضعیت OPN به همراه مالک ثبت می‌شود. شماره حساب داخلی ۲۰ رقمی از شناسه یکتای سپرده تولید می‌شود؛ شبا نیست.");
            operation.getResponses().addApiResponse("400", new ApiResponse().description("شماره ملی یا کد محصول نامعتبر است."))
                .addApiResponse("404", new ApiResponse().description("مشتری یا محصول در صندوق پیدا نشد."))
                .addApiResponse("409", new ApiResponse().description("محصول غیرفعال یا خارج از بازه اعتبار است."));
        }
        if (path.equals("/loan/products") || path.equals("/loan/products/{id}")) {
            operation.setTags(List.of("LOAN | محصولات تسهیلات"));
            operation.setSummary(switch (method) {
                case "POST" -> "ثبت محصول تسهیلات";
                case "PUT" -> "ویرایش محصول تسهیلات";
                case "DELETE" -> "حذف محصول تسهیلات";
                default -> path.endsWith("/{id}") ? "دریافت محصول تسهیلات" : "فهرست محصولات تسهیلات";
            });
            operation.setDescription(switch (method) {
                case "POST" -> "کد یکتا با حداکثر ۲۰ کاراکتر، نوع محصول و امکان تسویه زودهنگام الزامی‌اند. شناسه، صندوق فعال و اطلاعات ثبت توسط برنامه تعیین می‌شوند.";
                case "PUT" -> "ویرایش کامل فیلدهای محصول با شناسه مسیر در صندوق فعال؛ شناسه، مالکیت و زمان ثبت حفظ می‌شوند. حداقل‌ها نباید از حداکثرها بیشتر باشند.";
                case "DELETE" -> "حذف رکورد محصول متعلق به صندوق فعال؛ پاسخ موفق 204 و بدون بدنه است.";
                default -> path.endsWith("/{id}") ? "مشخصات محصول با شناسه مسیر؛ محصول ناموجود یا متعلق به صندوق دیگر پاسخ 404 دارد."
                        : "فهرست محصولات صندوق فعال؛ page از صفر، size پیش‌فرض ۲۰ و sort به شکل id,asc است.";
            });
            operation.getResponses().addApiResponse("400", new ApiResponse().description("ورودی نامعتبر؛ مقدار منفی، طول غیرمجاز، فیلد الزامی خالی یا بازه نامعتبر."));
            if (path.endsWith("/{id}")) operation.getResponses().addApiResponse("404", new ApiResponse().description("محصول در صندوق فعال پیدا نشد."));
            if (method.equals("POST") || method.equals("PUT")) {
                operation.getRequestBody().setDescription("اطلاعات کامل محصول؛ مقادیر مالی و تعدادها نامنفی، حداقل <= حداکثر و تاریخ پایان >= شروع باشند.");
                operation.getResponses().addApiResponse("409", new ApiResponse().description("کد محصول تکراری است؛ یکتایی کد در کل اسکیما برقرار است."));
            }
            if (method.equals("POST")) operation.getResponses().get("201").setDescription("محصول ثبت شد؛ شناسه و اطلاعات ثبت در پاسخ است.");
            if (method.equals("DELETE")) operation.getResponses().get("204").setDescription("محصول حذف شد؛ بدنه ندارد.");
        }
        if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
            operation.getResponses().get("200").setDescription("عملیات موفق؛ ساختار نتیجه در Schema پاسخ نمایش داده شده است.");
        }
    }

    private void describeSchema(String name, Schema schema) {
        String description = switch (name) {
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
        if (name.equals("OpenDepositRequest")) description = "ورودی افتتاح حساب: nationalCode شماره ملی ده رقمی معتبر و productCode کد محصول سپرده؛ description توضیحات اختیاری تا ۲۵۵ کاراکتر.";
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
                Map.entry("fundId", name.equals("BranchDto") ? "شناسه صندوق موجود که شعبه به آن وابسته است." : "شناسه صندوق فعال؛ در ثبت و ویرایش مشتری توسط سرویس تعیین می‌شود."),
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
