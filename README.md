# shafagh-template
shafagh-template

## صفحه‌بندی REST و Swagger

فهرست‌های `GET /base/city`، `/base/fund`، `/base/branch`، `/dpst/products` و `/loan/products` و جست‌وجوهای `POST /customer/real/filter` و `/customer/legal/filter` پارامترهای query مشترک دارند:

- `page`: شماره صفحه از صفر؛ پیش‌فرض ۰.
- `size`: تعداد رکورد؛ پیش‌فرض ۲۰، حداکثر ۲۰۰۰.
- `sort`: نام فیلد و جهت؛ پیش‌فرض `id,asc`، قابل تکرار برای چند فیلد.

مثال: `/dpst/products?page=0&size=10&sort=title,asc&sort=id,desc`.

پاسخ فهرست‌ها شیء صفحه با `content`, `number`, `size`, `totalElements`, `totalPages` است؛ فهرست‌های BASE نیز اکنون همین ساختار را دارند و آرایه ساده نیستند. شهرهای دارای `parentId=0` استان‌اند و از فهرست شهرها و شمارش صفحات حذف می‌شوند. توضیحات عملیات و DTOهای محصولات سپرده و تسهیلات و ورودی‌های صفحه‌بندی در Swagger عمومی و گروه‌های ماژول‌ها موجودند: `/swagger-ui/index.html`.

## Tenant صندوق قرض‌الحسنه

`TenantAwareEntity` در common از `BaseEntity` ارث می‌برد و `fundId` را با `@TenantId` و ستون `FUND_ID` نگه می‌دارد. ستون الزامی و در UPDATE غیرقابل‌ویرایش است. Hibernate در INSERT شناسه صندوق را از `CurrentTenantIdentifierResolver<Long>` می‌گیرد و برای کوئری‌های Entity متعلق به tenant شرط صندوق اعمال می‌کند. تمام واحدهای JPA ماژول‌ها از همین resolver استفاده می‌کنند.

فعلاً `FixedFundTenantIdentifierResolver` مقدار ثابت `shafagh.fund-id` را برمی‌گرداند؛ در تنظیمات اصلی `FUND_ID` پیش‌فرض ۱ دارد. مقدار باید مثبت باشد. Entityهای صندوقی از `TenantAwareEntity` ارث می‌برند؛ لازم نیست fundId در بدنه درخواست برای آنها ارسال شود. مقادیر صریح صندوق دیگر هنگام درج رد می‌شوند. رکوردها را با تغییر fundId بین صندوق‌ها جابه‌جا نکنید.

در آینده پیاده‌سازی Bean `fundTenantIdentifierResolver` باید fundId را از توکن معتبر و SecurityContext کاربر بخواند. قرارداد استاندارد Hibernate حفظ می‌شود؛ هیچ ساختار موقت userId/branchId اضافه نشده است. tenant باید پیش از بازشدن session/transaction مشخص باشد و در طول آن ثابت بماند. داده‌های بومی SQL/JDBC و اطلاعات عمومی بدون @TenantId خودکار فیلتر نمی‌شوند.

این سازوکار به Entityهای دارای @TenantId محدود است. در BASE شعبه و آدرس/تماس شعبه هم TenantAware هستند؛ شهرها و تعریف صندوق‌ها اطلاعات مشترک‌اند. آدرس و تماس شعبه فقط به شعبه همان صندوق متصل می‌شوند. نشانی/تماس مستقیم صندوق هنوز TenantAware نیستند.

## یکتایی شناسه مشتری در هر صندوق

جدول CUSTOMER در SchemaName.CIF دارای قید `UK_CUSTOMER_FUND_ID_IDENTIFIER` روی `(FUND_ID, IDENTIFIER)` است. نام‌گذاری قید در خود Entity تعریف شده و ایندکس پشتیبان به‌صورت خودکار توسط Oracle ساخته می‌شود. شناسه یکسان در صندوق‌های متفاوت مجاز و در همان صندوق ممنوع است؛ نوع یا وضعیت مشتری این قاعده را تغییر نمی‌دهد. ثبت و ویرایش، تکرار را در صندوق جاری بررسی می‌کنند؛ constraint دیتابیس درخواست‌های هم‌زمان را هم کنترل می‌کند. تکرار شناسه پاسخ HTTP 409 دارد. در حالت عملیاتی مشترک (`ddl-auto=validate`) جدول‌ها باید از قبل توسط مالکان اسکیما ساخته شده باشند.

## بررسی وجود مشتری در صندوق جاری

`POST /customer/exists?identifier=0012345678` برای کد ملی یا شماره ثبت شرکت، پاسخ رشته‌ای JSON (`"EXIST"`/`"NOT_EXIST"`) با HTTP 200 می‌دهد. این مسیر در Swagger عمومی و گروه CIF موجود است. ورودی الزامی، فقط شامل ارقام 0 تا 9 و حداکثر ۲۰ رقم است؛ ورودی نامعتبر 400 دارد. identifier به‌صورت متن و با حفظ صفر ابتدایی مقایسه می‌شود و محدود به نوع یا وضعیت خاصی از مشتری نیست.

کنترلر از قرارداد عمومی `CustomerLookup.existsByIdentifier` در cif-api استفاده می‌کند. پیاده‌سازی سرویس، fundId را از `CurrentTenantIdentifierResolver<Long>` می‌گیرد. Repository با کوئری صریح `c.identifier = :identifier AND c.fundId = :fundId` بررسی می‌کند. fundId پارامتر درخواست نیست؛ همان identifier در صندوق دیگر، EXIST برنمی‌گرداند.

## وابستگی BaseEntity شرکت

`TenantAwareEntity` از `com.misc.infra.framework4.entity.BaseEntity` در وابستگی `com.misc.infra:framework-4:4.1.15` ارث می‌برد؛ BaseEntity جایگزین در پروژه وجود ندارد. Hibernate Envers با نسخه مدیریت‌شدهٔ Spring Boot برای annotation موجود در JAR اضافه شده است. وابستگی‌های جانبی framework با exclusion کنار گذاشته شده‌اند؛ نیازهای JPA، auditing و Jackson را خود پروژه تأمین می‌کند.

در محیط شرکت Maven باید این artifact را از Repository داخلی دریافت کند. برای محیط فاقد دسترسی، JAR واقعی را پیش از build در Maven محلی نصب کنید. این JAR POM اصلی خود را در `META-INF/maven` دارد و install-file آن را تشخیص می‌دهد:

```shell
mvn org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file -Dfile="/path/to/framework-4-4.1.15.jar"
mvn clean install
```

JAR در Git ذخیره نشده است؛ در محیط تازه باید وابستگی مجدداً فراهم شود. تنظیمات Repository خصوصی و credentialها متعلق به settings.xml هستند.

در TenantAwareEntity با @AttributeOverrides کامنت ستون‌های ارث‌بری‌شده حفظ شده‌اند. CREATED_BY و MODIFIED_BY تا پیاده‌سازی هویت کاربر nullable باقی می‌مانند؛ فیلدهای تاریخ نیز با نگاشت قبلی پروژه سازگارند. OPT_LOCK از نوع long در JAR است و در نگاشت پروژه الزامی با پیش‌فرض دیتابیس ۰ است. تغییرات نگاشت، کد یا bytecode فایل JAR را تغییر نمی‌دهند. تنظیمات logging متعلق به برنامه در logback-spring.xml قرار دارند تا فایل logging همراه JAR، وابستگی‌های غیرضروری تحمیل نکند.

## تراکنش مشترک بین ماژول‌ها و اسکیماها

اتصال پیش‌فرض برنامه به همان Oracle/PDB با `shafagh_app_user` است؛ `DB_USERNAME` و `DB_PASSWORD` قابل تنظیم‌اند و مقدار پیش‌فرض هر دو shafagh_app_user است. جدول‌ها و sequenceها در SchemaName.BASE/CIF/DP/LN باقی می‌مانند. حساب عملیاتی باید CREATE SESSION، دسترسی DML به جدول‌ها و SELECT به sequenceهای مورد استفاده داشته باشد. کاربر و grantها توسط شما آماده شده‌اند؛ اسکریپت ایجاد آنها به پروژه اضافه نشده است.

`shafagh.persistence.module-datasources=false` حالت مشترک است: نام‌های base/cif/dpst/loan برای datasource، EntityManagerFactory و transactionManager همگی alias همان Bean مشترک‌اند؛ `appTransactionManager` نیز همان مدیر است. بنابراین متدهای @Transactional ماژول‌ها با propagation پیش‌فرض REQUIRED به تراکنش بیرونی می‌پیوندند.

فرایندهای مشخص کسب‌وکاری می‌توانند در یک سرویس Spring با `@Transactional(transactionManager="appTransactionManager", rollbackFor=Exception.class)` تعریف شوند و سرویس‌های ماژول‌ها را مستقیم صدا بزنند. این سرویس باید از Bean دیگری فراخوانی شود تا proxy تراکنش اعمال شود. راه مشترک دیگر، تزریق AtomicProcessExecutor از ماژول orchestration است:

```java
var result = atomicProcessExecutor.execute(() -> {
    var customer = customerService.create(command);
    return accountService.open(customer.id(), accountCommand);
});
```

این مثال الگوی فراخوانی است؛ create/open باید قراردادهای واقعی API ماژول مربوط باشند. execute فقط در حالت مشترک موجود است؛ checked exception و RuntimeException کل تراکنش را rollback می‌کنند. استثنا را نبلعید و مراحل فرایند را REQUIRES_NEW نکنید. rollback شامل مقدار مصرف‌شده sequence نمی‌شود و فاصله در شناسه‌ها طبیعی است. فراخوانی REST به برنامه مستقل و اثرات خارج دیتابیس داخل تراکنش JDBC قرار نمی‌گیرند.

DDL حالت عملیاتی `validate` است؛ کاربر DML جدول نمی‌سازد یا حذف نمی‌کند. برای آماده‌سازی اسکیمای توسعه با Hibernate و کاربران مالک، اجرای جداگانه با `shafagh.persistence.module-datasources=true` و `DB_DDL_MODE=create` ممکن است. این حالت مستقل برای اجرای فرایندهای اتمیک استفاده نشود. DROP/CREATE grantهای اشیا را حذف می‌کند و پس از آن باید دسترسی کاربر عملیاتی دوباره برقرار شود. هیچ reset سفارشی یا اعطای CREATE ANY TABLE به کاربر عملیاتی اضافه نشده است.
