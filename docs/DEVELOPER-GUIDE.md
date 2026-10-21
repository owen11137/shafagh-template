# راهنمای استفاده از تمپلیت پروژه‌ی چندماژولی شفق

ویژه‌ی برنامه‌نویسان تیم · Java 21 · Spring Boot · Maven · Oracle · JPA/Hibernate

## ۱. معماری در یک نگاه

پروژه یک **مونولیت چندماژولی** است: یک پروسه، یک دیتابیس Oracle متمرکز، و چند ماژول مستقل که فقط از راه‌های مجاز با هم حرف می‌زنند.

| ماژول | پکیج | اسکیمای Oracle | کلاس اسکیما |
|---|---|---|---|
| اطلاعات پایه و مشترک | `com.misc.shafagh.base` | `SHFQ_BASE` | `SchemaName.BASE` |
| اشخاص | `com.misc.shafagh.cif` | `SHFQ_CIF` | `SchemaName.CIF` |
| سپرده | `com.misc.shafagh.dpst` | `SHFQ_DPST` | `SchemaName.DP` |
| تسهیلات | `com.misc.shafagh.loan` | `SHFQ_LOAN` | `SchemaName.LN` |

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
@Table(schema = SchemaName.DP, name = "SHFQ_DPST_DEPOSIT")
public class DepositEntity { ... }

@SequenceGenerator(name = "...", schema = SchemaName.DP, sequenceName = "SHFQ_DPST_DEPOSIT_SEQ", allocationSize = 1)
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
در common جدول مستقلی ندارند. ماژول loan شامل Entityهای محصول، وام و شخص مرتبط با وام است؛ جدول‌های آن‌ها
با اتصال مستقل LOAN ساخته می‌شوند.

در تنظیم فعلی توسعه، با `spring.jpa.hibernate.ddl-auto=create`، Hibernate
جدول‌ها و Sequenceهای نگاشت‌شده را با کاربر مالک هر اسکیما بازسازی می‌کند. خطای DDL باعث شکست
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


## Swagger مشترک همه ماژول‌ها

وابستگی `springdoc-openapi-starter-webmvc-ui` نسخهٔ 3.1.1 سازگار با Spring Boot 4
فقط در `shafagh-app` قرار دارد. Swagger UI در `/swagger-ui.html` (یا
`/swagger-ui/index.html`) و سند OpenAPI در `/v3/api-docs` ارائه می‌شود؛ پورت
پیش‌فرض برنامه 8084 است. این صفحه همهٔ Controllerهای REST در
`com.misc.shafagh` را به صورت خودکار در یک سند نشان می‌دهد و امکان ارسال درخواست
از طریق Try it out را دارد. Controller جدید در هر ماژول نیز خودکار اضافه می‌شود.

BASE، CIF و LOAN Controller دارند؛ DPST تا زمانی که Controller REST
نداشته باشد، endpoint در Swagger ندارد. قراردادهای سرویس جاوا و Entityها
به‌تنهایی endpoint نیستند. تنظیمات مشترک در `OpenApiConfiguration` و بخش
`springdoc` در `application.yml` قرار دارند. تست HTTP مشترک، تولید سند، حضور
مسیرهای BASE/CIF، schema DTO و ارائه فایل‌های Swagger UI را بررسی می‌کند.

## نسخه‌های وابستگی‌ها

در بررسی انتشارهای پایدار Maven Central در 2026-10-12، وابستگی‌ها به این نسخه‌ها
هماهنگ شدند:

| وابستگی | نسخه |
| --- | --- |
| Spring Boot | 4.1.1 |
| springdoc | 3.1.1 |
| Swagger Models Jakarta | 2.2.55؛ مطابق نسخهٔ مورد استفاده springdoc |
| ArchUnit | 1.5.1 |
| Lombok | 1.18.48 |
| MapStruct | 1.6.3 |
| Lombok MapStruct Binding | 0.2.0 |

پین قدیمی Jackson Databind حذف شد؛ نسخه‌های Jackson، Hibernate، Spring Data،
HTTP Client، درایور Oracle و پلاگین‌های مدیریت‌شده از BOM/parent Spring Boot
گرفته می‌شوند تا مجموعهٔ سازگار حفظ شود. در این بیلد Jackson 2 برابر 2.21.5،
Jackson 3 برابر 3.1.5 و Hibernate برابر 7.4.5.Final است. وجود هر دو نسل Jackson
به دلیل نیازهای متفاوت کتابخانه‌هاست؛ بسته‌های جاوای آن‌ها جدا هستند.
JCIP Annotations در نسخهٔ موجود 1.0 باقی مانده است.
Java همچنان 21 است. `clean install` و هر ۱۵ تست شامل مستندات Swagger، HTTP،
پایداری Entityها و اتصال‌های مستقل ماژول‌ها موفق بودند؛ تست Oracle واقعی
در این بررسی اجرا نشده است. برای همگام‌سازی IntelliJ پس از pull، پروژهٔ Maven
را از POM ریشه Reload کنید.

### توضیحات و دسته‌بندی Swagger

فهرست انتخاب بالای Swagger شامل همه ماژول‌ها، BASE، CIF، DPST و LOAN است.
گروه BASE مسیرهای `/base/**` و گروه CIF مسیرهای `/customer/**` را نشان می‌دهد؛
مسیر استاندارد APIهای آینده DPST و LOAN به‌ترتیب `/dpst/**` و `/loan/**` است.
گروه DPST فعلاً endpoint ندارد و گروه LOAN مسیرهای محصول تسهیلات را نمایش می‌دهد. سندهای گروهی در
`/v3/api-docs/00-all`، `/v3/api-docs/01-base`، `/v3/api-docs/02-cif`،
`/v3/api-docs/03-dpst` و `/v3/api-docs/04-loan` ارائه می‌شوند.

عملیات داخل گروه‌ها با Tag شهرها، صندوق‌ها، شعبه‌ها، اشخاص حقیقی، اشخاص حقوقی
و جست‌وجوی شناسه دسته‌بندی شده‌اند. توضیح Tag، مسئولیت کلاس Controller را مشخص
می‌کند. هر عملیات عنوان و شرح فارسی، شرح ورودی و پاسخ دارد. مدل‌های DTO نیز
در بخش Schemas توضیح کلاس و فیلدها را دارند؛ الزامات شرطی ثبت و ویرایش در شرح
عملیات و مدل بیان شده‌اند. دسته‌بندی و معرفی ماژول‌ها در `OpenApiConfiguration`
و توضیحات عملیات و مدل‌ها در `OpenApiDocumentation` نگهداری می‌شوند. این ساختار
قراردادهای API ماژول‌ها را به کتابخانه Swagger وابسته نمی‌کند.

هنگام اضافه کردن مسیر یا DTO جدید، توضیح مربوط را در customizer توسعه دهید.
تست HTTP مستندات، شرح تمام ۲۶ عملیات فعلی، توضیح DTOها، جدا بودن مسیرهای گروه‌ها
و یکسان بودن توضیحات سند اصلی و سند گروه BASE را بررسی می‌کند.


## مدل‌های تسهیلات LOAN

`LoanEntity`، `LoanPersonEntity` و `ProductLoanEntity` در
`shafagh-loan-impl` و پکیج `com.misc.shafagh.loan.impl.entity` قرار دارند.
هر سه از `TenantAwareEntity` در common و به‌طور غیرمستقیم از `BaseEntity`
ارث می‌برند؛ fundId، تاریخ‌های auditing و version از کلاس‌های مشترک گرفته می‌شوند.
جدول‌ها و Sequenceها صریحاً `SchemaName.LN` دارند و اتصال مستقل LOAN آن‌ها را
هنگام اجرا ایجاد می‌کند. شخص وام به LoanEntity در همان ماژول مرتبط است؛ مشتری
با `customerId` به CIF اشاره می‌کند و وابستگی به Entity یا Repository ماژول CIF
ندارد. هنگام افزودن سرویس ثبت، وجود مشتری را از طریق API ماژول CIF بررسی کنید.

`ProductStatus` از common استفاده می‌شود. enumهای LoanType، LoanStatus،
LoanPersonType و CollateralType مخصوص مدل داخلی تسهیلات‌اند. وضعیت داخلی وام
ارائه‌شده در فایل ارسالی، قرارداد موجود `loan.api.dto.LoanStatus` را تغییر
نمی‌دهد؛ تبدیل بین قرارداد API و وضعیت داخلی باید در سرویس/mapper مشخص شود.
مقادیر enumهای ارسالی و ترتیب ذخیره ORDINAL حفظ شده‌اند؛ ترتیب enum را برای
دیتابیس دارای داده بدون مهاجرت تغییر ندهید. LoanPersonType فعلاً فقط OWNER دارد.

`ModulePersistenceConfigurationTest` ساخت هر سه جدول و Sequenceهای آن‌ها در
اسکیما LOAN، ارتباط شخص و وام، نوع و شماره وثیقه، متن فارسی محصول، مبلغ دقیق،
ثبت و ویرایش وضعیت، version/auditing و جداسازی tenant را روی اتصال مستقل H2
بررسی می‌کند. این کلاس‌ها به‌تنهایی endpoint REST ایجاد نمی‌کنند؛ گروه Swagger
LOAN اکنون CRUD محصول تسهیلات را نمایش می‌دهد. اجرای واقعی Oracle در این بررسی
انجام نشده است.

### اقساط تسهیلات

مدل‌های `InstallmentEntity`، `InstallmentDetailEntity`، `InstallmentType` و
`InstallmentStatus` در پکیج `loan.impl.entity.installment` قرار دارند.
جدول‌های `INSTALLMENT` و `INSTALLMENT_DETAIL` و Sequenceهای
`SEQ_INSTALLMENT_ID` و `SEQ_INSTALLMENT_DETAIL_ID` از `SchemaName.LN`
استفاده می‌کنند. scan موجود LOAN زیرپکیج installment را نیز پوشش می‌دهد.
هر دو Entity از `TenantAwareEntity` مشترک ارث می‌برند و به LoanEntity در همان
ماژول ارتباط ManyToOne با بارگذاری LAZY دارند. جزئیات پرداخت مطابق فایل ارسالی
با وام و شماره قسط مشخص می‌شود؛ ارتباط جدیدی به InstallmentEntity اضافه نشده است.

نگاشت `installmentType` از JoinColumn نامناسب برای enum به Column اصلاح شد.
مقادیر و ترتیب enumها و نام فیلد `transactionNO` حفظ شدند. تست اتصال مستقل LOAN
ساخت هر پنج جدول Entity این ماژول، ذخیره مبلغ و پرداخت، نوع و وضعیت قسط، ارتباط
با وام، auditing، tenant و تغییر version در ویرایش قسط را روی H2 بررسی می‌کند.

## ساختار مشترک ماژول‌های دامنه و DTOها

در پیاده‌سازی هر چهار ماژول BASE، CIF، DPST و LOAN این پکیج‌ها وجود دارند:

```text
impl/
  entity/
  repository/
  service/
    port/
    impl/
  config/
  controller/
  exception/
  dto/
    request/
    response/
    mapper/
```

پوشه‌های خالی در محیط محلی موجودند؛ Git فقط پوشه‌های دارای فایل را نگهداری
می‌کند. این ساختار برای ماژول‌های دامنه است؛ common زیرساخت مشترک،
orchestration هماهنگ‌کنندهٔ سرویس‌ها، API قرارداد عمومی و POMهای والد تجمیع‌کننده
هستند و نباید Entity و Repository دامنه در آن‌ها قرار گیرد.

برای ۱۶ Entity که این لایه‌ها را نداشتند، request، response و mapper اضافه یا
تکمیل شده است؛ چهار مدل مشتری، آدرس، تماس و امضای CIF از mapperها و DTOهای موجود
استفاده می‌کنند:

| ماژول | مدل‌های تکمیل‌شده |
| --- | --- |
| BASE | City، Fund، FundAddress، FundContact، Branch، BranchAddress، BranchContact |
| CIF | CustomerInquiry |
| DPST | Deposit، DepositPerson، ProductDeposit |
| LOAN | Loan، LoanPerson، ProductLoan، Installment، InstallmentDetail |

نام مدل‌های جدید ورودی `{Entity}ReqDTO` و خروجی `{Entity}ResponseDto` است.
زیرپکیج‌های branch/fund و installment در DTO و mapper نیز حفظ شده‌اند.
mapperهای BASE موجود با متدهای جدید توسعه یافته‌اند و قراردادهای DTO عمومی
`base-api` برای فراخوانی بین ماژول‌ها برقرارند. DTOهای جدید در impl برای ورودی
و خروجی داخلی و REST آینده‌اند؛ اگر قراردادی بین ماژول‌ها لازم شد، مدل عمومی
مستقل را در API منتشر کنید.

متدهای جدید mapper:

- `toEntity(request)`: ساخت Entity جدید، بدون پذیرش ID و اطلاعات سیستمی.
- `updateFromRequest(request, entity)`: ویرایش کامل فیلدهای ورودی روی Entity موجود.
- `toResponse(entity)` و `toResponses(entities)`: تبدیل رکورد یا فهرست به DTO پاسخ.

requestهای جدید id و فیلدهای auditing/version ندارند؛ برای مدل‌های TenantAware
نیز fundId در request وجود ندارد و صندوق فعال را Hibernate تعیین می‌کند.
fundId در BranchReqDTO شناسهٔ ارتباط شعبه با صندوق است و نقش tenant ندارد.
پاسخ مدل‌های TenantAware، fundId و اطلاعات auditing/version را دارد.
ارتباط‌های ManyToOne/OneToOne با شناسه در DTO مشخص می‌شوند تا گراف Entity و
چرخهٔ ارتباطی به خروجی JSON منتقل نشود. مجموعهٔ فرزندان Fund/Branch در این
DTOهای جدید حمل نمی‌شود و mapper ویرایش، مجموعهٔ مدیریت‌شده را جایگزین نمی‌کند.
برای ویرایش اعضای مجموعه از سرویس مخصوص فرزندان استفاده کنید.

`EntityReferenceResolver` هر ماژول با EntityManagerFactory همان ماژول، شناسه را
به `getReference` تبدیل می‌کند؛ از ساخت Entity جعلی با setId خودداری شده است.
این تبدیل فقط مرجع مدیریت‌شده می‌سازد؛ سرویس باید وجود رکورد و مجاز بودن ارتباط
را بررسی کند و mapping را در تراکنش مناسب انجام دهد. نگاشت‌های جدید با
`unmappedTargetPolicy=ERROR` کامپایل می‌شوند تا فیلد مقصد فراموش‌شده باعث شکست
بیلد شود. MapStruct 1.6.3 از POM ریشه مدیریت می‌شود و پردازشگرهای MapStruct،
Lombok و binding در DPST/LOAN نیز تنظیم شده‌اند.

`EntityDtoMapperTest` برای هر ۱۶ mapper، تزریق Spring، تبدیل تمام فیلدهای ورودی،
شناسهٔ ارتباط، خروجی فهرست، ورودی null و حفظ id/fundId/auditing/version هنگام
ویرایش را بررسی می‌کند. `clean install` و هر ۳۴ تست روی H2 موفق بودند.

## نام‌گذاری اشیای دیتابیس

همهٔ Entityهای فعلی از الگوهای PK_TABLE، FK_CHILD_PARENT، UK_TABLE_COLUMNS و
SEQ_TABLE_ID استفاده می‌کنند. FKهای چندگانه به یک والد، پسوند نقش برگرفته از
ستون‌های فرزند دارند. Checkهای enum و boolean از CHK_TABLE_COLUMN و محدودیت‌های
NOT NULL در Oracle از CHK_TABLE_COLUMN_NOT_NULL استفاده می‌کنند. منطق مجاز بودن
داده با این تغییر عوض نشده است؛ محدودیت جدید تجاری یا ایندکس عملکردی اضافه نشده است.

نام PK و Checkهای خودکار با `ConstraintNamingIntegrator` و نام‌های ضمنی UK/FK
با `DatabaseNamingStrategy` کنترل می‌شوند. `ShafaghOracleDialectResolver` در
تشخیص Oracle، dialect اختصاصی را انتخاب می‌کند. این dialect، NOT NULLها را
صریح نام‌گذاری می‌کند، ایندکس‌های لازم PK/UK را به UDX_TABLE_COLUMNS تغییر نام
می‌دهد و برای ساخت Sequence جدید CACHE 50 NOCYCLE NOORDER را صریح تولید می‌کند.
نام Sequence و Index حداکثر ۵۰ کاراکتر است. allocationSize=1 به معنی CACHE 1
نیست؛ آن تنظیم به الگوریتم تخصیص ID در Hibernate مربوط است. CACHE 50 حداقل
تعیین‌شده برای RAC را نیز تأمین می‌کند؛ انتخاب ORDER در RAC باید با DBA انجام شود.

ایندکس اختیاری فقط با مشورت DBA/معماری داده اضافه می‌شود: IDX_TABLE_COL برای
معمولی، UDX_TABLE_COL برای یکتا، IDX_TABLE_COL1_COL2 برای مرکب، FDI_TABLE_DESC
برای تابعی. Bitmap در OLTP ممنوع است. ساخت ایندکس جدید روی جدول Production باید
ONLINE باشد؛ تغییر نام ایندکس موجود ساخت یا rebuild نیست. Materialized View فقط
در RPT یا با تأیید معماری مجاز است و refresh policy باید مستند شود.

**برای دیتابیس دارای دادهٔ موردنیاز، مهاجرت نام‌ها با حفظ داده لازم است؛
در حالت توسعهٔ بدون داده، تنظیم create جایگزین این مهاجرت است.**
`ddl-auto=update` اشیای قدیمی را rename نمی‌کند و Sequence جدید ممکن است از ۱
شروع شود. در حالت فعلی توسعه، Hibernate جدول‌های نگاشت‌شده را با create
بازسازی می‌کند و نام‌های جدید را مستقیم ایجاد می‌کند؛ اسکریپت مهاجرت لازم نیست.

تست `OracleNamingDdlTest` بدون اتصال Oracle، DDL واقعی Hibernate برای تمام
Entityها را تولید می‌کند و نام PK، FK، UK، NOT NULL، Check enum، ایندکس‌های PK و
گزینه‌های Sequence را بررسی می‌کند. تست چنداتصالی نیز نام واقعی Constraintهای
ایجادشده در هر اسکیما H2 را بررسی می‌کند. این دو نوع بررسی جای آزمون Oracle واقعی
را نمی‌گیرند. فایل `AGENTS.md` قواعد و درخواست کاربر برای سؤال قبل از اصلاح
نام‌گذاری Entityهای ناسازگار آینده را ثبت کرده است.


## ساخت مجدد جدول‌ها در مرحلهٔ توسعه

طبق درخواست کاربر، تنظیم اصلی برنامه برای دورهٔ توسعه چنین است:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create
```

با اجرای عادی برنامه، Hibernate به اتصال هر ماژول وصل می‌شود، جدول‌های متناظر
Entityهای آن ماژول را حذف و سپس جدول‌ها و Sequenceهای نگاشت‌شده را با قواعد
نام‌گذاری فعلی می‌سازد. پروفایل reset، کد پاک‌سازی اختصاصی و اسکریپت اجرای reset
حذف شده‌اند. اجرای SQL دستی یا دسترسی agent به Oracle برای اعمال این تنظیم
لازم نیست؛ خود برنامه باید به Oracle و حساب‌های چهار اسکیما دسترسی داشته باشد.

create داده‌های جدول‌های نگاشت‌شده را در هر startup از بین می‌برد و اشیای ساخته‌شده
را پس از توقف برنامه حفظ می‌کند. این تنظیم همهٔ اشیای نامرتبط اسکیما یا Sequenceهای
قدیمیِ خارج از نگاشت فعلی را حذف نمی‌کند. برای مرحلهٔ دارای داده یا محیط عملیاتی،
create را به update یا validate متناسب با راهبرد مهاجرت تغییر دهید.


## CRUD محصول تسهیلات

Repository، سرویس داخلی و کنترلر `ProductLoanEntity` در ماژول loan اضافه شده‌اند.
سرویس از `loanTransactionManager`، DTO و mapper موجود استفاده می‌کند. دریافت،
فهرست، ویرایش و حذف با fundId فعال محدود می‌شوند؛ کاربر نمی‌تواند ID، fundId،
version یا auditing را در request تعیین کند. flush قبل از ساخت response انجام
می‌شود تا اطلاعات ثبت و version واقعی در پاسخ باشند.

| عملیات | مسیر | پاسخ موفق |
| --- | --- | --- |
| ثبت | `POST /loan/products` | 201 و مشخصات محصول |
| فهرست | `GET /loan/products?page=0&size=20&sort=id,asc` | 200 و صفحه نتایج |
| دریافت | `GET /loan/products/{id}` | 200 |
| ویرایش | `PUT /loan/products/{id}` | 200 |
| حذف | `DELETE /loan/products/{id}` | 204 بدون بدنه |

نمونهٔ بدنهٔ ثبت:

```json
{
  "code": "QL-001",
  "title": "محصول قرض‌الحسنه",
  "productType": "ADI",
  "status": "ACTIVE",
  "earlySettlementAllowed": true,
  "minAmount": 1000,
  "maxAmount": 100000,
  "installmentInterval": 1,
  "minInstallmentCount": 1,
  "maxInstallmentCount": 24
}
```

code، productType و earlySettlementAllowed الزامی‌اند؛ code پس از trim حداکثر
۲۰ کاراکتر و در کل اسکیما یکتا است. title و description حداکثر ۳۰۰ کاراکترند.
مقادیر مالی و تعدادها نامنفی، min <= max و toDate >= fromDate هستند. ورودی
نامعتبر 400، رکورد ناموجود یا متعلق به صندوق دیگر 404 و کد تکراری 409 دارد.
خطاهای این کنترلر با ProblemDetail و فیلد detail برمی‌گردند؛ تداخل هم‌زمان
ویرایش نیز 409 دارد. تشخیص تکرار در پایگاه داده، رقابت هم‌زمان یا کد متعلق به
صندوق دیگر را هم پوشش می‌دهد و constraint موجود UK_PRODUCT_LOAN_CODE حفظ شده است.

Swagger در گروه LOAN و Tag «محصولات تسهیلات»، همهٔ مسیرها، request/response و
شرح فیلدهای محصول را نشان می‌دهد. `ProductLoanCrudTest` چهار تست HTTP واقعی
روی اتصال مستقل LOAN دارد: چرخه CRUD با auditing/version، اعتبارسنجی، کد تکراری
و رکورد ناموجود، و جداسازی tenant همراه یکتایی جهانی کد. Oracle واقعی در این
بررسی اجرا نشده است.

## سرویس مشترک تولید کد

سرویس تولید کد در ماژول موجود `shafagh-base` قرار دارد و ساختار api/impl همان
ماژول را رعایت می‌کند:

- `shafagh-base-api`: اینترفیس عمومی
  `com.misc.shafagh.base.api.CodeGeneratorService` و خطای
  `com.misc.shafagh.base.api.CodeGenerationException`؛ بدون وابستگی به Spring، JDBC یا JPA.
- `shafagh-base-impl`: پیاده‌سازی
  `com.misc.shafagh.base.impl.service.impl.CodeGeneratorServiceImpl`، دسترسی Oracle/JDBC
  و مدیریت تراکنش‌ها.

ماژول‌های کسب‌وکار و orchestration فقط قرارداد `shafagh-base-api` را مصرف و
اینترفیس `CodeGeneratorService` را از طریق سازنده تزریق می‌کنند. `shafagh-app`
وابستگی موجود `shafagh-base-impl` را برای بارگذاری پیاده‌سازی Spring دارد.
منطق شماره‌گذاری در ماژول base باقی می‌ماند؛ ثابت موجود
`com.misc.shafagh.common.SchemaName.BASE` با مقدار `SHFQ_BASE` مالکیت آبجکت‌های
شماره‌گذاری را در اسکیمای اطلاعات پایه مشخص می‌کند. قرارداد داخلی `CodeNumberStore`
در `base.impl.service.api` و پیاده‌سازی `OracleCodeNumberStoreImpl` در
`base.impl.service.impl` قرار دارند.

| متد | ساختار خروجی | دامنه‌ی شمارنده | ظرفیت تخصیص |
|---|---|---|---|
| `generateCustomerCode(fundCode)` | کد صندوق ۶ رقمی + شماره ۸ رقمی | یک Oracle sequence سراسری | `99,999,999` در کل سامانه |
| `generateBranchCode(fundCode)` | کد صندوق ۶ رقمی + شماره ۵ رقمی | یک رکورد برای هر صندوق | `99,999` برای هر صندوق |
| `generateAccountNumber(branchCode)` | کد شعبه ۱۱ رقمی + شماره ۶ رقمی + Luhn | یک رکورد برای هر شعبه | `999,999` برای هر شعبه |

کدها رشته‌اند و صفرهای ابتدای اجزا حفظ می‌شوند. طول مشتری ۱۴، شعبه ۱۱ و حساب ۱۸
رقم ASCII است. اعداد فارسی، فاصله و ورودی با طول نامعتبر پذیرفته نمی‌شوند.
رقم Luhn برای تشخیص برخی خطاهای ورود است؛ وجود حساب، مالکیت یا مجوز استفاده را
تأیید نمی‌کند. ترتیب تخصیص شماره نیز به معنی ترتیب نهایی‌شدن تراکنش‌ها نیست.

### آبجکت‌های دیتابیس و مقدار اولیه

فایل `scripts/numbering-counters.sql` فقط sequence جدید مشتری و دو جدول شمارنده
را در اسکیمای موجود `SHFQ_BASE` تعریف می‌کند. این فایل کاربر، اسکیما، grant یا جدول
دامنه ایجاد نمی‌کند و هیچ آبجکت موجودی را حذف، تغییر نام یا بازسازی نمی‌کند.
اسکریپت یک‌بار و فقط پس از بررسی DBA درباره‌ی نبود این آبجکت‌ها و داده‌های قبلی
اجرا می‌شود؛ روی سامانه‌ی دارای شماره‌های قبلی، مقدار شروع باید در مهاجرتی بررسی‌شده
تعیین شود. sequenceهای موجود مدل، از جمله شناسه‌ی مشتری، دست‌نخورده باقی می‌مانند.

حالت runtime از datasource و transaction manager مشترک و `ddl-auto=validate`
استفاده می‌کند. حساب runtime و دسترسی‌ها قبلاً توسط کاربر/DBA فراهم شده‌اند؛ این
تغییر اسکریپت ساخت کاربر یا اعطای دسترسی ندارد. دسترسی موردنیاز شماره‌گذاری باید
توسط DBA روی همین حساب موجود بررسی شود. Hibernate برای این شمارنده‌های JDBC
جدول ایجاد نمی‌کند؛ DDL و مجوزها باید پیش از استفاده از سرویس آماده باشند.

- `SHFQ_BASE.SEQ_CUSTOMER_CUSTOMER_CODE`: مشترک بین صندوق‌ها، شروع `1`، افزایش `1`،
  سقف `99999999`، `NOCYCLE` و `CACHE 50 NOORDER`.
- `SHFQ_BASE.BRANCH_CODE_COUNTER`: کلید اصلی `FUND_CODE` و ستون `NEXT_NUMBER`.
- `SHFQ_BASE.ACCOUNT_NUMBER_COUNTER`: کلید اصلی `BRANCH_CODE` و ستون `NEXT_NUMBER`.

هر دو ستون `NEXT_NUMBER` هنگام درج رکورد با `1` مقداردهی می‌شوند؛ مقدار فعلی
شماره‌ی قابل تخصیص است و سپس به‌صورت اتمیک افزایش می‌یابد. بنابراین اولین شعبه
پسوند `00001` و اولین حساب پسوند ترتیبی `000001` دارد. پس از صدور آخرین شماره،
شمارنده‌ی شعبه `100000` و شمارنده‌ی حساب `1000000` می‌شود؛ ستون و قید دیتابیس
عمداً این مقدار «ظرفیت تکمیل‌شده» را می‌پذیرند، اما تخصیص بعدی باید خطا بدهد.
هیچ شمارنده‌ای دور نمی‌زند یا با تکمیل ظرفیت دوباره از یک شروع نمی‌شود.

تنظیمات قابل تغییر برنامه:

```yaml
app:
  code-sequences:
    customer: SHFQ_BASE.SEQ_CUSTOMER_CUSTOMER_CODE
  code-counters:
    branch: SHFQ_BASE.BRANCH_CODE_COUNTER
    account: SHFQ_BASE.ACCOUNT_NUMBER_COUNTER
  code-allocation:
    timeout-seconds: 5
```

نام آبجکت‌های پیکربندی باید شناسه‌ی معتبر Oracle با پیشوند اختیاری اسکیما باشد؛
ورودی صندوق و شعبه به‌صورت پارامتر SQL ارسال می‌شود. sequence سراسری حساب و
تنظیم قدیمی `app.code-sequences.account` دیگر استفاده نمی‌شوند.

### ثبت صندوق، ثبت شعبه و مرز تراکنش‌ها

سرویس کسب‌وکار پیش از تولید شماره، وجود، وضعیت مجاز و دسترسی به صندوق یا شعبه
را بررسی می‌کند. صندوق مجاز از tenant جاری تعیین می‌شود؛ شناسه یا کد صندوق دلخواه
از کلاینت مبنای دسترسی نیست. تولیدکننده قالب، تخصیص عدد، ظرفیت و رقم کنترل را
مدیریت می‌کند. مراحل زیر الگوی اتصال به سرویس‌های ثبت موجود هستند؛ این تغییر هنوز
فراخوانی خودکار تولیدکننده را به سرویس‌های ثبت صندوق، شعبه، مشتری و سپرده اضافه نکرده است.

1. سرویس ثبت صندوق در همان تراکنش ایجاد صندوق،
   `initializeBranchCounter(fundCode)` را فراخوانی می‌کند.
2. سرویس ثبت شعبه در تراکنش ثبت شعبه، `generateBranchCode(fundCode)` را
   فراخوانی، شعبه را ذخیره و سپس `initializeAccountCounter(branchCode)` را اجرا می‌کند.
3. این متدهای آماده‌سازی و تولید کد شعبه به تراکنش فعال ثبت نیاز دارند؛ از bean
   تزریق‌شده استفاده کنید تا کنترل تراکنش Spring اعمال شود. در خطای ثبت، آماده‌سازی
   شمارنده و افزایش شماره‌ی شعبه هم rollback می‌شوند. کد شعبه پیش از commit منتشر نمی‌شود.
4. تنها پس از commit ثبت شعبه و شمارنده‌ی حسابش، افتتاح حساب مجاز است؛ تراکنش
   مستقل تخصیص شماره حساب نمی‌تواند رکورد شمارنده‌ی هنوز commitنشده را بخواند.

آماده‌سازی فقط `INSERT` با مقدار `1` است. تکرار آماده‌سازی نباید با `MERGE` یا
`UPDATE` شمارنده‌ی قبلی را بازنشانی کند؛ وجود رکورد قبلی خطای قابل رسیدگی است.
شماره‌ی مشتری از sequence دریافت می‌شود و rollback ثبت مشتری آن را برنمی‌گرداند.

### تخصیص حساب و هم‌زمانی

شمارنده‌ی حساب با دستور اتمیک و شرط ظرفیت، در یک تراکنش مستقل کوتاه
`REQUIRES_NEW` افزایش می‌یابد. شماره فقط پس از commit موفق این تخصیص به فراخواننده
برمی‌گردد؛ عملیات کامل افتتاح حساب در این تراکنش قرار نمی‌گیرد. شکست بعدی ثبت حساب
شماره‌ی تخصیص‌یافته را مصرف‌شده باقی می‌گذارد؛ فاصله بین شماره‌های حساب و مشتری مجاز است.

این استقلال باید با bean جداگانه‌ی دارای proxy یا `TransactionTemplate` اجرا شود؛
فراخوانی متد داخلی همان کلاس با `@Transactional` به‌تنهایی تراکنش مستقل نمی‌سازد.
این رزرو فنی، استثنای محدود و صریحاً توافق‌شده با کاربر نسبت به قاعده‌ی عمومیِ
استفاده‌نکردن از `REQUIRES_NEW` در workflow اتمیک است. صرفاً تخصیص عدد مستقل
می‌شود؛ workflow ثبت حساب و تمام تغییرات مالی با propagation معمول `REQUIRED`،
یک datasource و transaction manager مشترک و انتقال خطا همچنان اتمیک باقی می‌مانند.
حالت owner/legacy با managerهای مستقل برای اجرای workflow چندماژولی مناسب نیست.

درخواست‌های یک شعبه فقط برای تغییر رکورد همان شمارنده منتظر می‌مانند. رکوردهای
شعب مختلف جدا هستند. تخصیص را ترجیحاً پیش از تراکنش طولانی ثبت حساب انجام دهید؛
اگر آن را از تراکنش موجود صدا بزنید، اتصال بیرونی ممکن است نگه داشته شود و اتصال
دوم لازم شود. محدودیت هم‌زمانی، ظرفیت connection pool، زمان دریافت اتصال و
timeout تخصیص را متناسب با بار تنظیم و آزمایش کنید. فراخوانی سرویس بیرونی یا
عملیات طولانی را داخل تراکنش تخصیص قرار ندهید.

### مسئولیت‌های یکپارچه‌سازی بانکی

مدل‌ها و سرویس‌های واقعی صندوق، شعبه، مشتری و سپرده در پروژه وجود دارند؛
`FundEntity`، `BranchEntity`، `CustomerEntity` و `DepositEntity` در این تغییر
دست‌نخورده‌اند. تولیدکننده‌ی شماره هنوز به مسیرهای ثبت آن‌ها متصل نشده است.
API تولیدکننده در این مرحله زیرساخت تخصیص شماره است و ثبت خودکار دامنه را فعال
نمی‌کند. در مدل و سرویس فعلی:

- `Fund.CODE` قید یکتایی سراسری ندارد.
- یکتایی `Branch` فقط روی `(FUND_ID, CODE)` است؛ این قید به‌تنهایی یکتایی سراسری
  کد شعبه را که کلید شمارنده‌ی حساب است تضمین نمی‌کند.
- `Customer.CUSTOMER_CODE` قابل تهی‌بودن است و قید یکتایی ندارد.
- مسیرهای ثبت و ویرایش کد ارسالی فراخواننده و تغییر پیشوند را می‌پذیرند و هنوز
  رکوردهای شمارنده را خودکار آماده نمی‌کنند.

این تغییر مدل‌ها، قیود یا hookهای سرویس‌های موجود را اصلاح نمی‌کند. پیش از فعال‌کردن
شماره‌گذاری خودکار باید اتصال‌های ثبت/ویرایش، پیشوندهای تغییرناپذیر و یکتای سراسری،
داده‌های قبلی و قیود لازم در یک تغییر و مهاجرت بررسی‌شده تکمیل شوند.
موارد زیر پیش‌نیاز اتصال واقعی هستند و این تغییر به‌تنهایی اجرای آن‌ها را تضمین نمی‌کند:

- روی کد صندوق و کدهای کامل مشتری، شعبه و حساب قیود `NOT NULL`، طول/قالب و
  `UNIQUE` قرار دهید. از نوع رشته‌ای برای حفظ صفرهای ابتدایی استفاده کنید.
- طبق معماری پروژه FK بین اسکیماهای ماژول‌ها ساخته نمی‌شود؛ وجود و مجازبودن
  صندوق/شعبه و همراهی ایجاد شمارنده با ثبت موجودیت در سرویس مربوط تضمین شود.
- کد صندوق و شعبه که داخل شناسه‌ها قرار می‌گیرد ثابت بماند؛ پس از بسته‌شدن حساب،
  غیرفعال‌شدن شعبه یا حذف اطلاعات کسب‌وکار، فضای شماره‌گذاری و شمارنده‌ها حفظ شوند.
  DBA باید دسترسی حساب runtime را به عملیات لازم محدود کند و حذف یا بازنشانی
  شمارنده‌ها را مجاز نکند. این تغییر مجوزهای حساب موجود را تغییر نمی‌دهد. بازیابی
  بکاپ نیز نباید شمارنده‌ها را از شماره‌های قبلاً منتشرشده عقب ببرد.
- در سامانه‌ی دارای داده، شروع شمارنده یا sequence از `1` مجاز نیست؛ پیش از فعال‌سازی
  سرویس جدید، داده‌های قدیمی و فضای شماره‌گذاری بررسی و مقادیر مناسب مهاجرت شوند.
- سرویس ثبت باید شناسه‌ی یکتای درخواست را همراه نتیجه به‌صورت تراکنشی نگه دارد.
  ارسال دوباره‌ی همان درخواست باید نتیجه‌ی قبلی را بدهد، نه اینکه با شماره‌ای جدید
  مشتری، شعبه یا حساب دیگری ایجاد کند. شماره‌ی یکتا به‌تنهایی idempotency نیست.
- شماره‌ی تخصیص‌یافته به معنی حساب موجود یا افتتاح موفق نیست؛ نتیجه فقط پس از
  commit ثبت حساب معتبر است. خطای ظرفیت، نبود شمارنده، ورودی نامعتبر و خطای موقت
  دیتابیس را جداگانه رسیدگی کنید. بدون کنترل درخواست تکراری، کل ثبت را خودکار تکرار نکنید.

تست‌ها باید اولین و آخرین تخصیص، تکمیل ظرفیت، چند اتصال هم‌زمان به یک شمارنده،
استقلال شمارنده‌ی شعب مختلف، rollback ثبت شعبه و آماده‌سازی، بقای تخصیص حساب پس
از rollback افتتاح حساب و خطای شمارنده‌ی مفقود را پوشش دهند. رفتار واقعی
`RETURNING ... INTO`، مجوزها، تراکنش و انتظار قفل باید روی Oracle نیز آزمایش شود؛
موفقیت تست‌های H2 به‌تنهایی تأیید این رفتارها یا ظرفیت عملکردی Oracle نیست.


برای اجرای تست‌های شماره‌گذاری با JDK 21:

```sh
mvn -pl shafagh-base/shafagh-base-impl -am test
```

تست‌های H2 مرز تراکنش‌های سرویس را با یک adapter آزمایشی بررسی می‌کنند؛
تست‌های mock نیز اتصال پارامترهای JDBC و تبدیل خطا را می‌سنجند. هیچ‌کدام اجرای
SQL اختصاصی Oracle را شبیه‌سازی یا تأیید نمی‌کنند. برای اجرای
`OracleCodeNumberStoreIntegrationTest`، متغیرهای `ORACLE_TEST_URL`،
`ORACLE_TEST_USERNAME` و `ORACLE_TEST_PASSWORD` را برای یک اسکیمای آزمایشی مجزا
تنظیم کنید و همین دستور را اجرا کنید. کاربر تست باید مجوز ایجاد جدول و sequence
داشته باشد؛ تست فقط آبجکت‌های خودش با نام‌های تصادفی را ایجاد و پاک می‌کند.
بدون این متغیرها، تست Oracle اجرا نمی‌شود. DDL جدید شماره‌گذاری و دسترسی واقعی
حساب runtime نیز باید جداگانه روی Oracle آزمایشی بررسی شوند.
