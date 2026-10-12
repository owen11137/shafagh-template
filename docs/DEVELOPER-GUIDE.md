# راهنمای استفاده از تمپلیت پروژه‌ی چندماژولی شفق

ویژه‌ی برنامه‌نویسان تیم · Java 21 · Spring Boot · Maven · Oracle · JPA/Hibernate

## ۱. معماری در یک نگاه

پروژه یک **مونولیت چندماژولی** است: یک پروسه، یک دیتابیس Oracle متمرکز، و چند ماژول مستقل که فقط از راه‌های مجاز با هم حرف می‌زنند.

| ماژول | پکیج | اسکیمای Oracle | کلاس اسکیما |
|---|---|---|---|
| اطلاعات پایه و مشترک | `com.misc.shafagh.base` | `SHFQ_BASE` | `SchemaName.BASE` |
| اشخاص | `com.misc.shafagh.cif` | `SHFQ_CIF` | `SchemaName.CIF` |
| سپرده | `com.misc.shafagh.dpst` | `SHFQ_DPST` | `SchemaName.DPST` |
| تسهیلات | `com.misc.shafagh.loan` | `SHFQ_LOAN` | `SchemaName.LOAN` |

جهت وابستگی ثابت است: `base ← cif ← dpst ← loan`. یعنی base به هیچ ماژولی وابسته نیست، cif فقط به base، و به همین ترتیب. وابستگی برعکس یا حلقه ممنوع است.

## ۲. ساختار پوشه‌ها

```
shafagh-parent/
├── pom.xml
├── shafagh-common/                 (زیرساخت مشترک مستقل از دامنه‌ها)
├── shafagh-<ماژول>/                 (pom تجمیعی، فقط ظرف)
│   ├── shafagh-<ماژول>-api/         (اینترفیس سرویس + DTO)
│   └── shafagh-<ماژول>-impl/        (entity, repository, service)
├── shafagh-orchestration/           (سناریوهای چندماژولی)
├── shafagh-app/                     (کلاس اصلی، تنظیمات، تست یکپارچه)
├── scripts/                         (اسکریپت‌های Oracle: 01 اسکیما/کاربر، 02 جدول، 03 grant)
└── docs/DEVELOPER-GUIDE.md
```

داخل هر ماژول:

- `...<m>.api`: `XxxService` (اینترفیس) و `api.dto` (record و enum عمومی)
- `com.misc.shafagh.common.SchemaName`: ثابت‌های نام اسکیما برای همهٔ دامنه‌ها
- `...<m>.impl.entity` / `impl.repository` / `impl.service`

کلاس‌های زیرساخت مشترک در ماژول مستقل `shafagh-common` قرار می‌گیرند:

```
com.misc.shafagh.common
├── persistence
│   ├── BaseEntity
│   └── TenantAwareEntity
└── product
    └── ProductStatus
```

ماژول‌های `impl` برای استفاده از این کلاس‌ها به `shafagh-common` وابسته می‌شوند؛
این ماژول به هیچ دامنه‌ای وابسته نیست. Entity و Repository دامنه همچنان در
`impl` ماژول صاحبشان می‌مانند. enum مشترک `ProductStatus` در پکیج
`com.misc.shafagh.common.product` قرار دارد و خودش وابستگی JPA ندارد.

utilityهای عمومی آینده را در پکیج متناسب با مسئولیتشان، مثل `common.time` یا
`common.validation`، اضافه کنید. منطق کسب‌وکار مثل محاسبهٔ اقساط در دامنهٔ تسهیلات
می‌ماند و از API آن ارائه می‌شود. در ساختار فعلی، مصرف‌کنندهٔ `common` وابستگی‌های
persistence را نیز دریافت می‌کند؛ ماژول‌های API به این زیرساخت وابسته نمی‌شوند.

تست `ModuleBoundaryTest` در `shafagh-app` وابستگی معکوس از `common` به دامنه‌ها،
وابستگی API به persistence و استفادهٔ مستقیم از impl دامنهٔ دیگر را بررسی می‌کند.

`BaseEntity` در همین زیرساخت، زمان/کاربر ایجاد و ویرایش و نسخهٔ خوش‌بینانهٔ
`OPT_LOCK` را تعریف می‌کند. `TenantAwareEntity` از آن ارث می‌برد و شناسهٔ صندوق
`FUND_ID` را با `@TenantId` به مدل اضافه می‌کند. auditing در `shafagh-app` فعال
است و زمان‌ها خودکار پر می‌شوند. چون تمپلیت هنوز احراز هویت ندارد، فیلدهای
`CREATED_BY` و `MODIFIED_BY` خالی می‌مانند؛ هنگام افزودن احراز هویت یک
`AuditorAware<String>` از هویت واقعی کاربر معرفی کنید.

تنظیم `shafagh.fund-id` از متغیر `FUND_ID` خوانده می‌شود و برای هر پروسه یک صندوق
ثابت به Hibernate معرفی می‌کند. مقدار پیش‌فرض قالب ۱ است؛ در استقرار واقعی شناسهٔ
صندوق را صریحاً تعیین کنید. این محدودسازی فقط برای کلاس‌های دارای `@TenantId`
اعمال می‌شود؛ Entityهای بدون این annotation همچنان دادهٔ مشترک هستند.

## ۳. قوانین طلایی

1. **فقط به api ماژول دیگر وابسته شو، هرگز به impl.**
2. **فقط به اسکیمای ماژول خودت دسترسی داشته باش.** هر Entity باید `@Table(schema = SchemaName.<MODULE>, name = "...")` داشته باشد.
3. **رابطه‌ی JPA بین ماژول‌ها ممنوع است** (`@ManyToOne` و مانند آن به Entity ماژول دیگر). فقط شناسه (`Long personId`) نگه دار.
4. **join یا query مستقیم روی جدول اسکیمای دیگر ممنوع است**، حتی با native query. اطلاعات را از سرویس همان ماژول بگیر.
5. **api خالص بماند**: بدون Spring و بدون JPA. فقط اینترفیس، record و enum.
6. **عملیاتی که بیش از یک ماژول را تغییر می‌دهد** فقط در `shafagh-orchestration` نوشته می‌شود.

قوانین ۱ و ۵ و استقلال زیرساخت مشترک را ArchUnit کنترل می‌کند و نقضشان بیلد را
می‌شکند. مالکیت schema، روابط بین Entityها و SQL مستقیم را در code review بررسی کنید.

### ۳-۱. ArchUnit چطور کار می‌کند

ArchUnit یک کتابخانه‌ی تست است که **ساختار کد** را بررسی می‌کند، نه رفتار آن را. در پروژه یک تست JUnit معمولی است (`ModuleBoundaryTest`) که در `mvn verify` اجرا می‌شود.

1. **کلاس‌ها را می‌خواند.** برنامه را اجرا نمی‌کند. فایل‌های `.class` (بایت‌کد) پکیج `com.misc.shafagh` را می‌خواند و مدلی می‌سازد: هر کلاس در کدام پکیج است، به کدام کلاس‌ها وابسته است (import، فیلد، پارامتر، فراخوانی، ارث‌بری) و چه annotation‌هایی دارد.
2. **قانون را مثل یک جمله می‌نویسی.** مثال: «هیچ کلاسی در `dpst.impl` نباید به impl ماژول‌های دیگر وابسته باشد»:

```java
noClasses().that().resideInAPackage("com.misc.shafagh.dpst.impl..")
        .should().dependOnClassesThat().resideInAnyPackage(
                "com.misc.shafagh.cif.impl..", "com.misc.shafagh.loan.impl..", "com.misc.shafagh.base.impl..")
        .check(imported);
```

3. **اگر حتی یک کلاس قانون را نقض کند، تست قرمز می‌شود** و پیام خطا دقیقاً می‌گوید کدام کلاس به چه چیزی وابسته شده است.

قوانین فعلی تست: api به impl، persistence مشترک یا Spring/JPA/Hibernate وابسته
نیست؛ impl یک دامنه به impl دامنهٔ دیگر وابسته نیست؛ common به دامنه‌ها و تنظیمات
برنامه وابسته نیست.

چون ArchUnit بایت‌کد را می‌بیند، **رشته‌ها (String) را نمی‌فهمد**: native query و JPQL که جدول ماژول دیگر را بزند تشخیص داده نمی‌شود و باید در code review دیده شود.

اگر وابستگی به impl دامنهٔ دیگر را به POM اضافه کنید و کد آن را مستقیم استفاده
کند، تست `domainsUseOtherDomainsOnlyThroughApis` در `mvn verify` شکست می‌خورد.

### ۳-۲. چرا ماژول‌ها به impl همدیگر دسترسی ندارند

مثلاً `shafagh-loan-impl` نمی‌تواند به `shafagh-dpst-impl` دسترسی داشته باشد. دو لایه مانع می‌شود:

1. **Maven (زمان کامپایل):** در pom ماژول `shafagh-loan-impl` فقط `shafagh-loan-api`، `shafagh-dpst-api`، `shafagh-cif-api` و `shafagh-base-api` هست. `shafagh-dpst-impl` در کلاس‌پث کامپایل آن نیست و import کردن `DepositServiceImpl` یا `DepositRepository` خطای کامپایل می‌دهد.
2. **ArchUnit (تست):** اگر کسی وابستگی `dpst-impl` را به pom اضافه کند و از کلاس‌های داخلی آن استفاده کند، قانون `domainsUseOtherDomainsOnlyThroughApis` تست را قرمز می‌کند.

ارتباط درست فقط از طریق اینترفیس api است:

```java
class LoanServiceImpl implements LoanService {
    private final DepositService depositService;   // اینترفیس در dpst-api
    ...
    DepositDto deposit = depositService.getById(request.depositId());
}
```

در زمان اجرا Spring خودش `DepositServiceImpl` را به‌جای اینترفیس تزریق می‌کند، ولی `LoanServiceImpl` فقط متدهای تعریف‌شده در `DepositService` را می‌بیند. اگر به چیزی نیاز داشتی که در اینترفیس نیست، آن را رسماً به `DepositService` اضافه کن.

استثنا: هیچ‌کدام از این‌ها جلوی reflection (`Class.forName(...)`) را نمی‌گیرند. در عمل نادر است و در code review دیده می‌شود. کلاس‌های `*ServiceImpl` در تمپلیت package-private هستند (بدون `public`)، پس از بیرون پکیج خودشان هم قابل دسترسی نیستند.

## ۴. اسکیمای هر ماژول

نام اسکیماها در کلاس مشترک `SchemaName` تعریف می‌شود. مقدار جدول و sequence باید
از همان ثابت خوانده شود. شهر مرجع در `SchemaName.BASE` و مدل‌های مشتری در
`SchemaName.CIF` قرار می‌گیرند:

```java
import com.misc.shafagh.common.SchemaName;

@Entity
@Table(schema = SchemaName.DPST, name = "SHFQ_DPST_DEPOSIT")
public class DepositEntity { ... }

@SequenceGenerator(name = "...", schema = SchemaName.DPST, sequenceName = "SHFQ_DPST_DEPOSIT_SEQ", allocationSize = 1)
```

- نام اسکیماها در همه‌ی محیط‌ها یکی است.
- sequenceها هم باید `schema` بگیرند.
- اگر native query لازم شد، نام اسکیما را از همین ثابت بردار و رشته‌ی تکراری ننویس.
- برنامه با **یک کاربر اتصال** به همه‌ی اسکیماها وصل می‌شود (برای تراکنش واحد). پس خود Oracle جلوی دسترسی به اسکیمای ماژول دیگر را نمی‌گیرد و رعایت قانون بر عهده‌ی ArchUnit و code review است.

### ۴-۱. کاربر اتصال (APP_USER)، grantها و حذف داده

`APP_USER` یک شخص یا ماژول نیست؛ **کاربر اتصال کل برنامه** به Oracle است (همان `DB_USERNAME`). مسیر واقعی SQL این است:

```
کد ماژول دیگر ──(فقط اینترفیس api)──> DepositService ──> DepositRepository (Hibernate)
                                                              │
                                              SQL با اتصال APP_USER اجرا می‌شود
```

وقتی سرویس یک ماژول `save()` می‌زند، Hibernate INSERT یا UPDATE را با اتصال برنامه اجرا می‌کند و Oracle فقط `APP_USER` را می‌بیند. به همین دلیل `APP_USER` روی جدول‌های هر چهار اسکیما حق INSERT/UPDATE دارد. قانون «ماژول‌ها فقط از طریق سرویس به هم دسترسی دارند» در **سطح کد (Java)** اجرا می‌شود، نه در Oracle.

**اگر جایی از برنامه حذف (`DELETE`) داریم:**

1. **متد حذف را در api همان ماژولِ صاحب جدول تعریف کن** و در impl پیاده کن (مثلاً `DepositService.delete(id)`). ماژول‌های دیگر فقط همین متد را صدا می‌زنند، نه Repository را.
2. **`GRANT DELETE` را فقط روی جدولی بده که واقعاً حذف دارد**، جداگانه و در PR مشخص‌شده. `03-grants.sql` عمداً `DELETE` ندارد، پس اگر کسی اشتباهاً در ماژول دیگر `deleteById` بزند Oracle خطای دسترسی می‌دهد.
3. **چون بین اسکیماها FK نداریم، یکپارچگی را خودمان نگه می‌داریم:** حذفی که فقط یک ماژول را درگیر می‌کند را خودِ سرویس آن ماژول چک می‌کند. حذفی که به ماژول‌های دیگر بستگی دارد (مثلاً «شخصی که سپرده یا تسهیلات دارد حذف نشود») در orchestration نوشته می‌شود: اول از `DepositService` و `LoanService` می‌پرسد، بعد در همان تراکنش `PersonService.delete` را صدا می‌زند.
4. **برای داده‌ی مالی (شخص، سپرده، تسهیلات) soft delete ترجیح دارد:** وضعیت را عوض کن (مثل `CLOSED` یا `INACTIVE`، یک `UPDATE`) تا سابقه و حسابرسی از بین نرود. حذف فیزیکی را برای داده‌ی موقت یا غیرمالی نگه دار و با معماری فنی هماهنگ کن.

`DELETE` در `scripts/04-grants-test-only.sql` فقط برای پاک‌کردن داده‌ی تست روی دیتابیس تست است و روی محیط واقعی نباید داده شود.

## ۵. راه‌اندازی

```
mvn clean verify
```

این دستور بیلد، تست معماری و تست rollback (با H2 در حالت Oracle) را اجرا می‌کند. برای اجرا روی Oracle:

```
export DB_URL=jdbc:oracle:thin:@//host:1521/service
export DB_USERNAME=...
export DB_PASSWORD=...
mvn -pl shafagh-app spring-boot:run
```

در اجرای عادی، `ddl-auto=update` است؛ Hibernate جدول‌ها و sequenceهای مدل فعلی را
هنگام startup می‌سازد یا ساختارشان را به‌روزرسانی می‌کند. schemaهای مورد استفاده
باید از قبل موجود باشند و کاربر اتصال اجازهٔ ساخت و تغییر آبجکت‌ها در آن‌ها را
داشته باشد. برای مدل فعلی، ساخت دستی جدول‌ها با فایل‌های ۰۲ و ۰۵ پیش‌نیاز اجرا نیست.
اسکریپت‌های دستی زیر برای نصب یا نگهداری ساختار قبلی پروژه در دسترس هستند:

1. `scripts/01-create-schemas-and-user.sql`: ساخت اسکیماها و کاربر اتصال (DBA؛ اگر قبلاً ساخته‌اید اجرا نکنید)
2. `scripts/02-create-tables.sql`: جدول‌ها و sequenceها در اسکیمای هر ماژول
3. `scripts/03-grants.sql`: دسترسی کاربر اتصال بدون `DELETE` (`APP_USER` را با کاربر خودتان عوض کنید)
4. `scripts/04-grants-test-only.sql`: `DELETE` برای پاک‌کردن داده‌ی تست، **فقط روی دیتابیس توسعه/تست**

`scripts/99-drop-all.sql` فقط برای پاک‌کردن دیتابیس توسعه/تست است.

اگر دیتابیس از قبل ساخته شده، برای افزودن محصول سپرده فقط
`scripts/05-add-product-deposit.sql` را یک‌بار اجرا کنید؛ این فایل جدول
`SHFQ_DPST.PRODUCT_DEPOSIT`، sequence و دسترسی‌های لازم را می‌سازد.
پس از نصب تازه با فایل‌های ۰۱ تا ۰۳، فایل ۰۵ را اجرا نکنید.
برای پاک‌کردن دادهٔ تست محصول روی دیتابیس تست، فایل ۰۴ نیز لازم است.

`ProductDepositEntity` و `ProductDepositRepository` در `shafagh-dpst-impl` هستند؛
نوع سپرده (`DepositType`) در `shafagh-dpst-api` قرار دارد. وضعیت محصول و نوع سپرده
مطابق مدل ورودی با `ORDINAL` ذخیره می‌شوند؛ ترتیب مقادیر enum را بدون migration
عوض نکنید. محصول فعلاً تعریف مستقلی است و افتتاح سپرده هنوز به انتخاب محصول
وصل نشده است.

### ۵-۱. تست‌ها (H2 و Oracle واقعی)

تست‌های `shafagh-app` هر دو حالت را پشتیبانی می‌کنند:

- **پیش‌فرض (H2 در حالت Oracle):** `mvn clean verify`. نیازی به Oracle نیست و هر برنامه‌نویس و سرور بیلد می‌تواند اجرا کند.
- **Oracle واقعی:** اول اسکریپت‌های `02` و `03` و (روی دیتابیس تست) `04` را اجرا کنید، بعد:

```
export DB_URL=jdbc:oracle:thin:@//host:1521/service
export DB_USERNAME=...
export DB_PASSWORD=...
mvn clean verify -Dtest.profile=oracle
```

در حالت Oracle، `ddl-auto=validate` است، یعنی اگر Entityها با ستون‌های جدول‌های واقعی نخواند، برنامه بالا نمی‌آید و تست‌ها شکست می‌خورند.
تست‌ها داده‌های خودشان را پاک می‌کنند (کد ملی‌هایی که با `IT-` شروع می‌شوند و ارز `TST`). اگر تستی وسط کار کرش کرد و چیزی ماند، آن‌ها را دستی پاک کنید. روی دیتابیس مشترک تیم اجرا نکنید مگر با هماهنگی.

| تست | چه چیزی را ثابت می‌کند |
|---|---|
| `ModuleBoundaryTest` (ArchUnit، سه تست) | استقلال common، API بدون persistence و منع دسترسی به impl دامنهٔ دیگر |
| `DepositPersistenceTest` | ذخیرهٔ محصول، سپرده و شخص مرتبط؛ شناسهٔ صندوق، auditing و افزایش نسخهٔ خوش‌بینانه |
| `WebServerStartupTest` | راه‌اندازی وب‌سرور و پاسخ به درخواست HTTP |

برای نوشتن تست سناریوی جدید:

- از `@SpringBootTest` برای راه‌اندازی برنامه و از EntityManager یا Repository برای ساخت داده استفاده کن.
- برای بررسی commit/rollback واقعی، تست را بدون `@Transactional` اجرا کن و داده‌های خودت را پاک کن.
- برای تست صرفاً persistence، مانند `DepositPersistenceTest` تراکنش تست را rollback کن.
- برای هر سناریوی چندماژولی تست «شکست در هر مرحله» بنویس.

## ۶. افزودن قابلیت به ماژول موجود

مثال: متد جدید در ماژول سپرده.

1. در `shafagh-dpst-api`: متد را به `DepositService` اضافه کن و در صورت نیاز DTO (record) بساز.
2. در `shafagh-dpst-impl`: Entity و Repository را در صورت لزوم تغییر بده و متد را در `DepositServiceImpl` پیاده کن.
3. اگر جدول یا ستون عوض شد، DDL را در `scripts/02-create-tables.sql` (و در صورت نیاز `03-grants.sql`) یا اسکریپت مهاجرت تیم به‌روز کن و ستون `ROW_VERSION` را برای Entityهای دارای `@Version` فراموش نکن. جدول باید در اسکیمای همان ماژول باشد.
4. متدهای نوشتنی `@Transactional` و خواندنی `@Transactional(readOnly = true)` بگیرند.
5. تست بنویس و `mvn clean verify` بزن.

## ۷. صحبت با ماژول دیگر

اینترفیس api ماژول مقصد را از طریق constructor تزریق کن:

```java
class DepositServiceImpl implements DepositService {
    private final PersonService personService;   // api ماژول CIF
    ...
    if (!personService.existsActive(personId)) {
        throw new BusinessException("PERSON_NOT_ACTIVE", "...");
    }
}
```

- به‌جای رابطه‌ی JPA، فقط شناسه را ذخیره کن و اطلاعات را از سرویس بگیر.
- dependency مربوط به `shafagh-<m>-api` مقصد باید در pom ماژول تو باشد.
- اگر ماژول مقصد «بالاتر» از تو در زنجیره‌ی وابستگی است، جهت وابستگی را نقض کرده‌ای. سناریو را به orchestration ببر.

## ۸. عملیات چندماژولی (یا همه یا هیچ)

اگر یک کار باید در چند ماژول انجام شود و در صورت خطا همه rollback شوند، آن را در `shafagh-orchestration` بنویس. نمونه: `LoanApplicationService.openLoanWithCollateral`.

```java
@Service
public class XxxApplicationService {

    @Transactional(rollbackFor = Exception.class)
    public Result doSomething(Command c) {
        personService.existsActive(...);      // ماژول ۱
        depositService.blockAmount(...);      // ماژول ۲
        return loanService.register(...);     // ماژول ۳
    }
}
```

نکات مهم:

- مرز تراکنش فقط در orchestration است. سرویس‌های ماژول‌ها با `REQUIRED` (پیش‌فرض) در همان تراکنش شرکت می‌کنند.
- **در سرویس‌های ماژول‌ها هرگز `REQUIRES_NEW` یا `NOT_SUPPORTED` نگذار.** تراکنش جدا اتمیک‌بودن را از بین می‌برد.
- **exception سرویس ماژول را داخل orchestration با try/catch قورت نده.** تراکنش در همان لحظه rollback-only می‌شود و در انتها `UnexpectedRollbackException` می‌گیری. اگر لازم است catch کنی، دوباره throw کن.
- مبالغ و موجودی‌ها را همیشه با Entityی که `@Version` (قفل خوش‌بینانه) دارد تغییر بده تا درخواست‌های هم‌زمان بیش از موجودی مصرف نکنند (نمونه: `DepositEntity` و `ConcurrencyTest`).
- `rollbackFor = Exception.class` را فراموش نکن: بدون آن exception از نوع checked باعث commit می‌شود (ثابت‌شده در `MultiModuleTransactionTest`).
- orchestration فقط به api ها وابسته است (impl ممنوع) و هیچ ماژولی نباید به آن وابسته شود.
- عملیات تک‌ماژولی نیازی به orchestration ندارد؛ مستقیم سرویس همان ماژول را صدا بزن.
- کارهایی که خارج از دیتابیس اتفاق می‌افتند (ارسال پیام، SMS، فراخوانی سرویس بیرونی) با rollback دیتابیس برنمی‌گردند. آن‌ها را بعد از commit انجام بده.

برای هر سناریوی جدید، مثل `MultiModuleTransactionTest` یک تست rollback برای هر مرحله بنویس که ثابت کند در خطای مرحله‌ی آخر، تغییرات مراحل قبل برگشته است. تست را عمداً بدون `@Transactional` بنویس، وگرنه rollback تست، rollback واقعی را پنهان می‌کند.

## ۹. افزودن ماژول جدید

1. پوشه‌ی `shafagh-<m>/` بساز. pom تجمیعی را از `shafagh-loan/pom.xml` کپی کن و نام‌ها را عوض کن.
2. داخلش `shafagh-<m>-api` و `shafagh-<m>-impl` را بساز و در pom هر دو `<relativePath>../../pom.xml</relativePath>` بگذار.
3. در pom ریشه: پوشه‌ی گروه را به `modules` و artifactهای api/impl را به `dependencyManagement` اضافه کن.
4. پکیج‌ها: `com.misc.shafagh.<m>.api` و `com.misc.shafagh.<m>.impl.{entity,repository,service}`.
5. ثابت نام اسکیمای جدید را به `SchemaName` در `shafagh-common` اضافه کن و ساخت اسکیمای Oracle را با DBA هماهنگ کن.
6. Entityها را با `@Table(schema = SchemaName.<MODULE>, ...)` و sequenceها را با `schema = SchemaName.<MODULE>` بنویس.
7. impl را به pom مربوط به `shafagh-app` اضافه کن؛ برای زیرساخت مشترک به `shafagh-common` وابسته شو.
8. در `ModuleBoundaryTest` نام دامنه را به `DOMAINS` و پکیجش را به ممنوعیت‌های وابستگی common اضافه کن.
9. نام اسکیمای ماژول جدید را به ساخت اسکیماهای H2 در `shafagh-app/src/test/resources/application.yml` اضافه کن.
10. اگر ماژول جدید بین ماژول‌های موجود قرار می‌گیرد، تیم معماری باید جهت وابستگی را تأیید کند.

## ۱۰. خطاهای رایج بیلد و رفعشان

| پیام یا نشانه | علت | راه حل |
|---|---|---|
| شکست `implOfOneModuleNeverUsesImplOfAnotherModule` | کلاس impl ماژول دیگر را import کرده‌ای | اینترفیس api را تزریق کن |
| شکست `moduleDependencyDirectionIsRespected` | ماژول پایین‌تر به بالاتر وابسته شده | سناریو را به orchestration ببر |
| شکست `entitiesMapOnlyToTheSchemaOfTheirOwnModule` | `@Table` اسکیمای ماژول خودت را ندارد | `schema = SchemaName.<MODULE>` |
| شکست `everyModuleDeclaresItsOwnDistinctSchema` | کلاس `<M>Schema` نیست یا نام اسکیما تکراری است | کلاس را بساز یا نام را اصلاح کن |
| شکست `apiPackagesAreFrameworkFree` | در api از Spring یا JPA استفاده شده | به impl منتقل کن |
| `UnexpectedRollbackException` | exception داخل تراکنش catch شده | دوباره throw کن یا ساختار را عوض کن |
| `ObjectOptimisticLockingFailureException` | دو درخواست هم‌زمان یک ردیف را تغییر داده‌اند | رفتار درست است؛ درخواست دوم را دوباره امتحان کن یا خطا بده |
| خطای validate هنگام بالا آمدن روی Oracle (`Schema-validation`) | Entity با جدول واقعی نمی‌خواند | DDL یا Entity را همسان کن (`scripts/02-create-tables.sql`) |
| `ORA-00942` یا شکست `OracleSchemaTest` | جدول نیست یا grant ندارد | اسکریپت‌های `02` و `03` را اجرا کن |
| `ORA-01031` (insufficient privileges) هنگام پاک‌کردن داده‌ی تست یا `DELETE` | `DELETE` به کاربر اتصال داده نشده | روی دیتابیس تست `04` را اجرا کن؛ در کد اصلی: متد حذف و grant همان جدول را هماهنگ کن |
| خطای «schema not found» در تست H2 | اسکیمای ماژول در URL تست ساخته نشده | به `INIT=...` در `application.yml` تست اضافه کن |

## ۱۱. چک‌لیست قبل از PR

- [ ] `mvn clean verify` سبز است.
- [ ] فقط به api ماژول‌های دیگر وابسته‌ام و join یا query مستقیم روی اسکیمای دیگر ندارم.
- [ ] Entityهای جدید `@Table(schema = SchemaName.<MODULE> ...)` دارند و رابطه‌ی JPA بین‌ماژولی ندارند.
- [ ] sequenceهای جدید هم `schema` دارند.
- [ ] عملیات چندماژولی در orchestration است و تست rollback دارد.
- [ ] DDL جدید به‌روز شده و grant لازم برای کاربر اتصال در نظر گرفته شده است.
- [ ] اگر حذف (`DELETE`) اضافه کرده‌ام: متد در api همان ماژول است، `GRANT DELETE` فقط روی همان جدول است، و برای داده‌ی مالی soft delete را بررسی کرده‌ام.
- [ ] تست‌ها هم روی H2 و هم (برای تغییرات دیتابیسی) روی Oracle با `-Dtest.profile=oracle` سبز هستند.
- [ ] هیچ `REQUIRES_NEW` یا catch خالی در مسیر تراکنش چندماژولی نیست.

## ۱۲. محدودیت‌های شناخته‌شده

- ArchUnit وابستگی کلاس‌ها را بررسی می‌کند؛ SQL رشته‌ای و مالکیت schemaها نیازمند بررسی جداگانه‌اند.
- کاربر اتصال به همه‌ی اسکیماها دسترسی دارد، پس جداسازی در سطح دیتابیس اجباری نشده است (در انتظار تصمیم معماری فنی).
- لایه‌ی REST هنوز در تمپلیت نیست. controllerها در `shafagh-app` (یا یک ماژول وب جدا) نوشته شوند و برای عملیات چندماژولی فقط orchestration را صدا بزنند.
- تست‌ها به‌صورت پیش‌فرض روی H2 اجرا می‌شوند؛ برای اطمینان از DDL و دسترسی‌های واقعی باید با `-Dtest.profile=oracle` هم اجرا شوند.


## تنظیمات و بررسی CIF

کدهای CIF به Lombok، MapStruct، اعتبارسنجی Jakarta، Spring MVC
و Apache HttpClient وابسته‌اند. annotation processorهای MapStruct و Lombok در POM
همین ماژول همراه `lombok-mapstruct-binding` تنظیم شده‌اند.

`CifCustomerServiceTest` ثبت و ویرایش مشتری با آدرس و تماس، ثبت تکراری و ورودی
نامعتبر را روی H2 بررسی می‌کند. شناسهٔ صندوق از `shafagh.fund-id` گرفته می‌شود؛
Entityهای مشتری از `TenantAwareEntity` مشترک ارث می‌برند.

در ساختار فعلی، درخواست‌های HTTP به توکن احراز هویت نیاز ندارند. CORS همچنان
با `shafagh.client-url` تنظیم می‌شود. کلاینت‌های HTTPS از اعتبارسنجی عادی گواهی و
hostname استفاده می‌کنند؛ برای CA داخلی truststore معتبر پیکربندی کنید.
ارتباط واقعی با Oracle و سرویس‌های بیرونی نیازمند بررسی جداگانه در محیط دارای
اتصال و تنظیمات آن سرویس‌هاست.

## شهر، شعبه و صندوق در BASE

Entity، Repository، Mapper، Service و Controller شهر، شعبه و صندوق در
`shafagh-base-impl` قرار دارند. DTO و قرارداد سرویس‌های `CityService`،
`BranchService` و `FundService` در `shafagh-base-api` هستند. جدول‌ها و Sequenceهای
`CITY`، `BRANCH`، `FUND` و آدرس‌ها و تماس‌های شعبه و صندوق از `SchemaName.BASE`
استفاده می‌کنند. `ContactType` در `shafagh-common` بین BASE و CIF مشترک است.

CIF فقط به `shafagh-base-api` وابسته است: شناسه شهر در ستون موجود `CITY_ID`
مشتری و آدرس نگهداری می‌شود؛ اعتبارسنجی شهر و دریافت DTO آن با `CityService`
انجام می‌شود. پاسخ مشتری همچنان فیلد `city` با مشخصات شهر و استان دارد.
BASE نیز مالک صندوق را با `ownerId` در ستون `OWNER_ID` نگهداری و از طریق
`CustomerLookup` در `shafagh-cif-api` اعتبارسنجی می‌کند. هیچ‌کدام از این دو ماژول
به Entity یا Repository ماژول دیگر وابسته نیست. Spring در برنامه ترکیبی
پیاده‌سازی این APIها را تزریق می‌کند؛ ارتباط داخلی فعلی فراخوانی سرویس جاواست.

برای هرکدام از `/base/city`، `/base/fund` و `/base/branch`:

| عملیات | روش و مسیر |
| --- | --- |
| فهرست | `GET /base/{resource}` |
| دریافت | `GET /base/{resource}/{id}` |
| ثبت | `POST /base/{resource}` |
| ویرایش | `PUT /base/{resource}/{id}` |

ثبت شهر به `name` نیاز دارد؛ `parentId=0` استان را مشخص می‌کند و فهرست عمومی
شهرها استان‌ها را حذف می‌کند. صندوق به `code`، `name` و `ownerId` مشتری موجود
در صندوق فعال نیاز دارد. شعبه به `code`، `name` و `fundId` موجود نیاز دارد.
هنگام ثبت، `id` نفرستید. شناسه مرجع ناموجود پاسخ 404 و اطلاعات ناقص پاسخ 400 دارد.

`BaseModuleIntegrationTest` ثبت، ویرایش، ارتباط دو ماژول، فیلتر شهر مشتری،
آدرس‌ها و تماس‌ها و قرارگیری هر هفت جدول در اسکیما BASE را روی H2 بررسی می‌کند.
`WebServerStartupTest` مسیرهای REST و ثبت و ویرایش از طریق HTTP واقعی را بررسی
می‌کند. تست‌های مرز ماژول‌ها نیز وابستگی مستقیم به پیاده‌سازی ماژول دیگر را رد می‌کنند.

### دیتابیس موجود

`ddl-auto=update` داده‌ها را بین اسکیماها جابه‌جا نمی‌کند. اگر جدول‌های شعبه و
صندوق در `SHFQ_CIF` داده دارند، پیش از استفاده از نسخه جدید باید آن‌ها را به
`SHFQ_BASE` منتقل کنید: ابتدا FUND، سپس BRANCH، و سپس جدول‌های آدرس و تماس؛
شناسه‌ها، `OWNER_ID`، `FUND_ID`، `BRANCH_ID` و `CITY_ID` باید حفظ شوند.
Sequenceهای مقصد را به مقداری بالاتر از بیشترین ID منتقل‌شده تنظیم کنید و
دسترسی کاربر برنامه به جدول‌ها و Sequenceهای BASE را برقرار کنید.
CITY از قبل در BASE بوده است. جدول‌ها و داده‌های قدیمی با این تغییر حذف نمی‌شوند.
تست‌ها روی دیتابیس H2 انجام شده‌اند؛ انتقال داده یا اتصال واقعی Oracle در این
تغییر اجرا نشده است.


## اتصال مستقل هر اسکیما و ساخت جدول‌ها

حالت پیش‌فرض اجرای برنامه `shafagh.persistence.module-datasources=true` است.
چهار DataSource، چهار persistence unit و چهار transaction manager مستقل تعریف
شده‌اند. همهٔ اتصال‌ها به `DB_URL` وصل می‌شوند؛ آدرس هرکدام را نیز می‌توانید با
`BASE_DB_URL`، `CIF_DB_URL`، `DPST_DB_URL` و `LOAN_DB_URL` جدا تنظیم کنید.
نام کاربری و رمز پیش‌فرض هر اتصال برابر نام کوچک اسکیما است:
`shfq_base`، `shfq_cif`، `shfq_dpst` و `shfq_loan`.
برای تغییر آن‌ها از متغیرهای `{MODULE}_DB_USERNAME` و `{MODULE}_DB_PASSWORD`
استفاده کنید؛ MODULE یکی از BASE، CIF، DPST، LOAN است.
نام‌های بزرگ `SchemaName` برای Oracle بدون نقل‌قول با این کاربران مطابقت دارند.

هر persistence unit فقط کلاس‌های `com.misc.shafagh.{module}.impl` را scan می‌کند
و schema پیش‌فرض همان ماژول است. Repositoryها نیز به اتصال و transaction manager
همان ماژول متصل‌اند. Entityهای فعلی schema جدول و Sequence را صریحاً از
`SchemaName` می‌گیرند. Entity جدید را در پیاده‌سازی ماژول مربوط قرار دهید و برای
جدول و Sequence از ثابت همان ماژول استفاده کنید. کلاس‌های `@MappedSuperclass`
در common جدول مستقلی ندارند. ماژول loan فعلاً Entity ندارد؛ اتصال آن آماده است
ولی تا اضافه شدن Entity، جدولی برای آن ساخته نمی‌شود.

با `spring.jpa.hibernate.ddl-auto=update`، Hibernate در شروع برنامه جدول‌ها و
Sequenceهای مفقود را با کاربر مالک هر اسکیما ایجاد می‌کند. خطای DDL باعث شکست
startup می‌شود تا ساخت ناقص جدول‌ها پنهان نماند. هر کاربر Oracle باید از قبل در
همان PDB موجود باشد و مجوزهای CREATE SESSION، CREATE TABLE، CREATE SEQUENCE
و quota روی tablespace خود داشته باشد. فایل `scripts/oracle-schema-permissions.sql`
برای اعطای این مجوزها به چهار کاربر موجود توسط مدیر دیتابیس ارائه شده است؛
برنامه با این تنظیم، کاربر Oracle جدید ایجاد نمی‌کند. مجوز CREATE ANY TABLE یا
دسترسی مستقیم به جدول‌های ماژول دیگر لازم نیست.

سرویس‌های BASE از `@Transactional("baseTransactionManager")` و سرویس‌های CIF
از `@Transactional("cifTransactionManager")` استفاده می‌کنند. برای سرویس جدید
DPST یا LOAN نیز transaction manager همان ماژول را مشخص کنید. برای EntityManager
از `@PersistenceContext(unitName="dpst")` (یا base/cif/loan) استفاده کنید.
اتصال پیش‌فرض، EntityManager پیش‌فرض و transaction manager پیش‌فرض CIF هستند.
این تراکنش‌ها مستقل‌اند؛ عملیات نوشتن در چند ماژول با این تنظیم، یک تراکنش اتمیک
مشترک نیست. ارتباط فعلی ماژول‌ها فقط lookup از API ماژول دیگر است.

تست‌های قبلی با `module-datasources=false` و یک اتصال مشترک H2 اجرا می‌شوند.
`ModulePersistenceConfigurationTest` حالت چنداتصالی واقعی برنامه را با چهار
کاربر H2 جداگانه اجرا می‌کند و جداسازی Entityها، ساخت همه جدول‌ها و Sequenceها،
نام کاربر اتصال، ارتباط BASE/CIF و tenant/auditing سپرده را بررسی می‌کند.
اتصال به Oracle واقعی و اجرای مجوزها باید در شبکهٔ دارای دسترسی به DB انجام شود.
