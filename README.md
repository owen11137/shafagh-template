# Shafagh — سامانه بانکی ماژولار

پروژهٔ **Shafagh** یک برنامهٔ چندماژولی Java/Spring Boot با Maven و پایگاه‌دادهٔ Oracle است. توسعهٔ جاری در برنچ `koosha_test` انجام می‌شود. این README وضعیت فعلی مخزن را شرح می‌دهد؛ وجود ماژول الزاماً به معنای تکمیل قابلیت‌های کسب‌وکار آن نیست.

## پیش‌نیازها

- **Java JDK 21**
- **Maven** سازگار با Spring Boot 4 (ترجیحاً Maven 3.9+)
- **Oracle Database** و اسکیماها/مجوزهای موردنیاز
- IntelliJ IDEA یا IDE دلخواه؛ برای کار با APIها ابزار Swagger UI در برنامه موجود است.

نسخه‌های اصلی طبق `pom.xml`: **Spring Boot 4.0.6**، **Java 21**؛ ORM بر مبنای Spring Data JPA/Hibernate است.

## ساختار مخزن

```text
shafagh-base/             shared base modules (api / impl)
shafagh-cif/              customer information / CIF (api / impl)
shafagh-dpst/             deposit module (api / impl)
shafagh-loan/             loan module (api / impl)
shafagh-orchestration/    orchestration module
shafagh-app/              Spring Boot executable application
shafagh-architecture-tests/  ArchUnit architecture checks
scripts/                  Oracle SQL scripts and DBA instructions
.github/workflows/        CI for koosha_test
```

**کد اجرایی اصلی:** `com.misc.shafagh.ShafaghApplication` در `shafagh-app`. ساختار `api` برای قراردادهای بین ماژولی و `impl` برای پیاده‌سازی طراحی شده است. تست‌های معماری، وابستگی غیرمجاز بین پیاده‌سازی ماژول‌ها را بررسی می‌کنند.

**وضعیت فعلی:** CIF شامل Entityها و سرویس‌های مشتری، شهر، صندوق و شعبه است. ماژول‌های Deposit و Loan پس از حذف Entityها/سرویس‌های نمونهٔ آزمایشی، هنوز نیازمند پیاده‌سازی واقعی هستند؛ صرف وجود ماژول‌ها را به‌عنوان API کامل عملیاتی در نظر نگیرید.

## Build و تست

از پوشهٔ اصلی مخزن:

```bash
mvn clean verify
```

برای ساخت بسته‌ها:

```bash
mvn clean install
```

در GitHub Actions، Workflow مربوط به `koosha_test` با JDK 21 دستور `mvn -B -ntp clean verify` را اجرا می‌کند. **موفقیت CI جایگزین آزمایش اتصال و DDL روی Oracle واقعی نیست.**

### IntelliJ IDEA

پروژه را از `pom.xml` ریشه Import کنید، **Project SDK = JDK 21** و SDK ماژول‌ها را `Project SDK` قرار دهید و **Reload All Maven Projects** بزنید. برنامه را از کلاس `ShafaghApplication` در ماژول `shafagh-app` اجرا کنید.

## تنظیمات دیتابیس Oracle

منبع تنظیمات: `shafagh-app/src/main/resources/application.yml`. برای محیط خودتان متغیرهای زیر را در **Run Configuration / Environment Variables** یا محیط اجرای برنامه تعریف کنید:

| متغیر | کاربرد |
|---|---|
| `DB_URL` | نشانی JDBC دیتابیس Oracle، مثلاً `jdbc:oracle:thin:@//db-host:1521/service_name` |
| `DB_USERNAME` | کاربر اتصال برنامه با مجوزهای مناسب |
| `DB_PASSWORD` | گذرواژهٔ کاربر اتصال؛ خارج از Git نگهداری شود |
| `DB_DDL_AUTO` | پیش‌فرض `none`؛ برای بررسی ساختار در محیط آزمایشی `validate` |

**مهم:** فایل پیکربندی فعلی مقادیر پیش‌فرض توسعه‌ای برای اتصال Oracle دارد. در استقرار واقعی، `DB_URL`، `DB_USERNAME` و `DB_PASSWORD` را صراحتاً از محیط امن تأمین کنید و هیچ اطلاعات ورود واقعی را Commit نکنید.

### سیاست ایجاد جدول و Sequence

**برنامه ساختار دیتابیس را هنگام Startup ایجاد یا اصلاح نمی‌کند.** مقدار پیش‌فرض Hibernate:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: ${DB_DDL_AUTO:none}
```

ساخت و تغییر جدول‌ها با **اجرای دستی اسکریپت‌ها توسط مسئول دیتابیس** انجام می‌شود. بنابراین `ddl-auto=update` یا `create` را برای راه‌اندازی معمول/عملیاتی فعال نکنید.

اسکریپت‌های فعلی در [`scripts/`](scripts/) قرار دارند:

1. `01-create-schemas-and-user.sql`: فقط برای محیط جدیدی که مالک اسکیماها هنوز ایجاد نشده‌اند؛ قبل از اجرا توسط DBA بازبینی شود.
2. `02-create-tables.sql`: DDL جداول و Sequenceهای موجود CIF (در حال حاضر ۱۲ جدول و ۱۲ Sequence).
3. `03-grants.sql`: مجوزهای موردنیاز کاربر اتصال؛ مقدار نمونهٔ `APP_USER` را پیش از اجرا جایگزین کنید.

ترتیب دقیق، هشدارهای دسترسی و محدودیت اسکریپت‌ها در [راهنمای DBA](scripts/README.md) آمده است. **این اسکریپت‌ها برای ایجاد اولیه‌اند و اجرای تکراری آن‌ها روی اسکیماهای موجود امن نیست.** اسکریپت‌های تغییرات بعدی باید جداگانه و به‌صورت Migration تهیه شوند. `99-drop-all.sql` اسکریپت حذف خودکار ساختار عملیاتی نیست.

اسکیماهای درنظرگرفته‌شده برای ماژول‌ها: `SHFQ_BASE`، `SHFQ_CIF`، `SHFQ_DPST`، `SHFQ_LOAN`. در وضعیت فعلی DDL موجود برای Entityهای واقعی CIF آماده شده و نباید انتظار ساخت خودکار جدول برای ماژول‌های دیگر داشت.

پس از اجرای دستی SQL در **محیط آزمایشی**، می‌توانید با `DB_DDL_AUTO=validate` برنامه را اجرا و عدم تطابق Entity و جدول را بررسی کنید؛ این حالت داده یا جدول جدیدی نمی‌سازد.

## اجرای برنامه و Swagger

پس از آماده‌سازی Oracle و تنظیم متغیرهای محیطی، کلاس `ShafaghApplication` را در IntelliJ اجرا کنید؛ یا از ریشهٔ پروژه:

```bash
mvn -pl shafagh-app -am spring-boot:run
```

> برای اجرای Maven در ساختار چندماژولی، اگر دستور فوق به‌دلیل اجرای Plugin در ماژول‌های غیرقابل‌اجرا مشکل داشت، ابتدا `mvn clean install` را اجرا کنید و سپس با IntelliJ ماژول `shafagh-app` را Run کنید.

با فرض پورت پیش‌فرض 8080:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

کنترلر مشتریان در `shafagh-cif/shafagh-cif-impl/src/main/java/com/misc/shafagh/cif/impl/controller/CustomerController.java` قرار دارد. ریشهٔ مسیر آن `/customer` است؛ گروه‌های `/real/` و `/legal/` عملیات ثبت، ویرایش، دریافت با ID و فیلتر را ارائه می‌کنند. Swagger برای مشاهدهٔ قراردادهاست، نه تضمین موفقیت درخواست‌ها.

### امنیت

برنامه از Spring Security و قابلیت JWT Resource Server برای یکپارچه‌سازی با Keycloak استفاده می‌کند. تنظیم JWT Decoder معتبر برای دسترسی به APIهای محافظت‌شده لازم است. **اگر JWT Decoder پیکربندی نشده باشد، تنها مسیرهای Swagger/OpenAPI بدون احراز هویت باز هستند و سایر درخواست‌ها رد می‌شوند.** تنظیمات `keycloak-management.client-id` و `keycloak-management.client-secret` مربوط به کلاینت مدیریت Keycloak هستند و از طریق پیکربندی محیطی/مجاز فراهم می‌شوند؛ اطلاعات محرمانه را داخل مخزن قرار ندهید.

## توسعه و قواعد معماری

- منطق دامنهٔ هر ماژول را در `impl` مربوط به خودش توسعه دهید و قراردادهای بین ماژولی را با دقت در `api` نگه دارید.
- Entityها باید به اسکیمای ماژول خود نگاشت شوند؛ `@Table(schema=...)` و Sequenceها را هنگام تغییر بررسی کنید.
- هر تغییر Entity که نیازمند اصلاح ساختار Oracle است باید با **اسکریپت SQL مستقل و قابل بازبینی** همراه شود؛ اجرای SQL جداگانه و دستی خواهد بود.
- تغییرات فعلی فقط در برنچ **`koosha_test`** اعمال می‌شوند؛ ادغام با `main` نیازمند فرایند مستقل است.
- تست Maven/ArchUnit و سپس تست یکپارچگی روی Oracle آزمایشی برای تغییرات بانک اطلاعاتی توصیه می‌شود.

## رفع خطاهای رایج

| علامت | مورد بررسی |
|---|---|
| `JDK isn't specified for module` | تنظیم Project SDK و Module SDK روی Java 21 و Reload Maven در IntelliJ |
| `Unable to determine Dialect without JDBC metadata` | اتصال Oracle، `DB_URL`، درستی نام Service و اعتبار دسترسی کاربر |
| `ORA-00942` یا نبودن Sequence | اجرای دستی DDL و Grantهای لازم در اسکیمای صحیح |
| Swagger UI باز نمی‌شود | اجرای برنامه، پورت 8080، مسیر `/swagger-ui/index.html` و وابستگی springdoc در `shafagh-app` |
| HTTP 401/403 برای APIها | پیکربندی JWT/Keycloak و دسترسی‌های امنیتی؛ فعال بودن Swagger به معنی باز بودن APIها نیست |

برای جزئیات SQL و ترتیب نصب، [`scripts/README.md`](scripts/README.md) را بخوانید.
