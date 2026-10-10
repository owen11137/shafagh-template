# راهنمای استفاده از تمپلیت پروژه‌ی چندماژولی شفق

ویژه‌ی برنامه‌نویسان تیم · Java 21 · Spring Boot · Maven · Oracle · JPA/Hibernate

## ۱. معماری در یک نگاه

پروژه یک **مونولیت چندماژولی** است: یک پروسه، یک دیتابیس Oracle متمرکز، و چند ماژول مستقل که فقط از راه‌های مجاز با هم حرف می‌زنند.

| ماژول | پکیج | اسکیمای Oracle | کلاس اسکیما |
|---|---|---|---|
| اطلاعات پایه و مشترک | `com.misc.shafagh.base` | `SHFQ_BASE` | `BaseSchema` |
| اشخاص | `com.misc.shafagh.cif` | `SHFQ_CIF` | `CifSchema` |
| سپرده | `com.misc.shafagh.dpst` | `SHFQ_DPST` | `DpstSchema` |
| تسهیلات | `com.misc.shafagh.loan` | `SHFQ_LOAN` | `LoanSchema` |

جهت وابستگی ثابت است: `base ← cif ← dpst ← loan`. یعنی base به هیچ ماژولی وابسته نیست، cif فقط به base، و به همین ترتیب. وابستگی برعکس یا حلقه ممنوع است.

## ۲. ساختار پوشه‌ها

```
shafagh-parent/
├── pom.xml
├── shafagh-<ماژول>/                 (pom تجمیعی، فقط ظرف)
│   ├── shafagh-<ماژول>-api/         (اینترفیس سرویس + DTO)
│   └── shafagh-<ماژول>-impl/        (<Module>Schema, entity, repository, service)
├── shafagh-orchestration/           (سناریوهای چندماژولی)
├── shafagh-architecture-tests/      (قوانین ArchUnit)
├── shafagh-app/                     (کلاس اصلی، تنظیمات، تست یکپارچه)
├── scripts/                         (اسکریپت‌های Oracle: 01 اسکیما/کاربر، 02 جدول، 03 grant)
└── docs/DEVELOPER-GUIDE.md
```

داخل هر ماژول:

- `...<m>.api`: `XxxService` (اینترفیس) و `api.dto` (record و enum عمومی)
- `...<m>.impl`: کلاس `<Module>Schema` (نام اسکیما)
- `...<m>.impl.entity` / `impl.repository` / `impl.service`

## ۳. قوانین طلایی

1. **فقط به api ماژول دیگر وابسته شو، هرگز به impl.**
2. **فقط به اسکیمای ماژول خودت دسترسی داشته باش.** هر Entity باید `@Table(schema = <Module>Schema.NAME, name = "...")` داشته باشد.
3. **رابطه‌ی JPA بین ماژول‌ها ممنوع است** (`@ManyToOne` و مانند آن به Entity ماژول دیگر). فقط شناسه (`Long personId`) نگه دار.
4. **join یا query مستقیم روی جدول اسکیمای دیگر ممنوع است**، حتی با native query. اطلاعات را از سرویس همان ماژول بگیر.
5. **api خالص بماند**: بدون Spring و بدون JPA. فقط اینترفیس، record و enum.
6. **عملیاتی که بیش از یک ماژول را تغییر می‌دهد** فقط در `shafagh-orchestration` نوشته می‌شود.

قوانین ۱، ۲ (روی Entityها)، ۵ و جهت وابستگی را ArchUnit کنترل می‌کند و نقضشان بیلد را می‌شکند. قوانین ۳ و ۴ (به‌خصوص native query و JPQL) را **باید در code review** بررسی کنید.

### ۳-۱. ArchUnit چطور کار می‌کند

ArchUnit یک کتابخانه‌ی تست است که **ساختار کد** را بررسی می‌کند، نه رفتار آن را. در پروژه یک تست JUnit معمولی است (`ModuleArchitectureTest`) که در `mvn verify` اجرا می‌شود.

1. **کلاس‌ها را می‌خواند.** برنامه را اجرا نمی‌کند. فایل‌های `.class` (بایت‌کد) پکیج `com.misc.shafagh` را می‌خواند و مدلی می‌سازد: هر کلاس در کدام پکیج است، به کدام کلاس‌ها وابسته است (import، فیلد، پارامتر، فراخوانی، ارث‌بری) و چه annotation‌هایی دارد.
2. **قانون را مثل یک جمله می‌نویسی.** مثال: «هیچ کلاسی در `dpst.impl` نباید به impl ماژول‌های دیگر وابسته باشد»:

```java
noClasses().that().resideInAPackage("com.misc.shafagh.dpst.impl..")
        .should().dependOnClassesThat().resideInAnyPackage(
                "com.misc.shafagh.cif.impl..", "com.misc.shafagh.loan.impl..", "com.misc.shafagh.base.impl..")
        .check(imported);
```

3. **اگر حتی یک کلاس قانون را نقض کند، تست قرمز می‌شود** و پیام خطا دقیقاً می‌گوید کدام کلاس به چه چیزی وابسته شده است.

قوانین فعلی: api به impl و به Spring/JPA وابسته نیست؛ impl یک ماژول به impl ماژول دیگر وابسته نیست؛ جهت وابستگی `base ← cif ← dpst ← loan` و بدون حلقه؛ orchestration فقط به api وابسته است؛ هر Entity در اسکیمای ماژول خودش است.

چون ArchUnit بایت‌کد را می‌بیند، **رشته‌ها (String) را نمی‌فهمد**: native query و JPQL که جدول ماژول دیگر را بزند تشخیص داده نمی‌شود و باید در code review دیده شود.

برای دیدن عملی: در یک Entity `@Table(schema = "SHFQ_BASE")` بگذار ولی آن را در پکیج `loan.impl.entity` بساز و `mvn verify` بزن. تست `entitiesMapOnlyToTheSchemaOfTheirOwnModule` قرمز می‌شود و پیامش را بخوان.

### ۳-۲. چرا ماژول‌ها به impl همدیگر دسترسی ندارند

مثلاً `shafagh-loan-impl` نمی‌تواند به `shafagh-dpst-impl` دسترسی داشته باشد. دو لایه مانع می‌شود:

1. **Maven (زمان کامپایل):** در pom ماژول `shafagh-loan-impl` فقط `shafagh-loan-api`، `shafagh-dpst-api`، `shafagh-cif-api` و `shafagh-base-api` هست. `shafagh-dpst-impl` در کلاس‌پث کامپایل آن نیست و import کردن `DepositServiceImpl` یا `DepositRepository` خطای کامپایل می‌دهد.
2. **ArchUnit (تست):** اگر کسی وابستگی `dpst-impl` را به pom اضافه کند تا کامپایل شود، قانون `implOfOneModuleNeverUsesImplOfAnotherModule` تست را قرمز می‌کند.

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

هر ماژول اسکیمای Oracle خودش را در یک کلاس ثابت اعلام می‌کند و نام اسکیما فقط همان‌جا نوشته می‌شود:

```java
// com.misc.shafagh.dpst.impl.DpstSchema
public final class DpstSchema {
    public static final String NAME = "SHFQ_DPST";
    private DpstSchema() {}
}

@Entity
@Table(schema = DpstSchema.NAME, name = "SHFQ_DPST_DEPOSIT")
public class DepositEntity { ... }

@SequenceGenerator(name = "...", schema = DpstSchema.NAME, sequenceName = "SHFQ_DPST_DEPOSIT_SEQ", allocationSize = 1)
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

اسکریپت‌ها به ترتیب اجرا می‌شوند (`ddl-auto` عمداً `none` است و جدول‌ها را Hibernate نمی‌سازد):

1. `scripts/01-create-schemas-and-user.sql`: ساخت اسکیماها و کاربر اتصال (DBA؛ اگر قبلاً ساخته‌اید اجرا نکنید)
2. `scripts/02-create-tables.sql`: جدول‌ها و sequenceها در اسکیمای هر ماژول
3. `scripts/03-grants.sql`: دسترسی کاربر اتصال بدون `DELETE` (`APP_USER` را با کاربر خودتان عوض کنید)
4. `scripts/04-grants-test-only.sql`: `DELETE` برای پاک‌کردن داده‌ی تست، **فقط روی دیتابیس توسعه/تست**

`scripts/99-drop-all.sql` فقط برای پاک‌کردن دیتابیس توسعه/تست است.

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
| `ModuleArchitectureTest` (ArchUnit) | مرز ماژول‌ها: فقط api، جهت وابستگی، بدون حلقه، Entity فقط در اسکیمای ماژول خودش |
| `ModuleCollaborationTest` | ارتباط ماژول‌ها فقط با سرویس‌های expose شده (سپرده به شخص و ارز، تسهیلات به شخص و سپرده) و اعتبارسنجی‌های هر ماژول |
| `MultiModuleTransactionTest` | «یا همه یا هیچ»: موفقیت کامل؛ شکست در مرحله‌ی ۱، ۲ یا ۳ همه را برمی‌گرداند؛ مالک اشتباه سپرده؛ الگوهای اشتباه (checked exception، قورت‌دادن exception) |
| `ConcurrencyTest` | دو بلوکه‌ی هم‌زمان بیش از موجودی: فقط یکی موفق می‌شود و بازنده کاملاً rollback می‌شود |
| `OracleSchemaTest` (فقط Oracle) | جدول و sequence هر ماژول در اسکیمای خودش است، بین اسکیماها FK نیست، کاربر اتصال به همه‌ی اسکیماها دسترسی دارد |

برای نوشتن تست سناریوی جدید:

- از `AbstractScenarioSupport` ارث ببر. داده‌ی تست را با `newPerson()` و `newDeposit()` بساز تا خودکار پاک شود.
- تست را **بدون `@Transactional`** بنویس تا rollback واقعی دیده شود.
- نتیجه را با `blockedInDb`، `loansInDb`، `rowCount` (JDBC مستقیم) بررسی کن، نه فقط با پاسخ سرویس.
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
5. در `com.misc.shafagh.<m>.impl` کلاس `<M>Schema` با ثابت `NAME` بساز (مثلاً `XyzSchema`). اسکیمای Oracle را با DBA هماهنگ و در DDL اضافه کن.
6. Entityها را با `@Table(schema = XyzSchema.NAME, ...)` و sequenceها را با `schema = XyzSchema.NAME` بنویس.
7. impl را به pom مربوط به `shafagh-app` و `shafagh-architecture-tests` اضافه کن.
8. در `ModuleArchitectureTest`: نام ماژول را به `MODULES` اضافه کن و جایگاهش را در قوانین جهت وابستگی مشخص کن.
9. نام اسکیمای ماژول جدید را به ساخت اسکیماهای H2 در `shafagh-app/src/test/resources/application.yml` اضافه کن.
10. اگر ماژول جدید بین ماژول‌های موجود قرار می‌گیرد، تیم معماری باید جهت وابستگی را تأیید کند.

## ۱۰. خطاهای رایج بیلد و رفعشان

| پیام یا نشانه | علت | راه حل |
|---|---|---|
| شکست `implOfOneModuleNeverUsesImplOfAnotherModule` | کلاس impl ماژول دیگر را import کرده‌ای | اینترفیس api را تزریق کن |
| شکست `moduleDependencyDirectionIsRespected` | ماژول پایین‌تر به بالاتر وابسته شده | سناریو را به orchestration ببر |
| شکست `entitiesMapOnlyToTheSchemaOfTheirOwnModule` | `@Table` اسکیمای ماژول خودت را ندارد | `schema = <Module>Schema.NAME` |
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
- [ ] Entityهای جدید `@Table(schema = <Module>Schema.NAME ...)` دارند و رابطه‌ی JPA بین‌ماژولی ندارند.
- [ ] sequenceهای جدید هم `schema` دارند.
- [ ] عملیات چندماژولی در orchestration است و تست rollback دارد.
- [ ] DDL جدید به‌روز شده و grant لازم برای کاربر اتصال در نظر گرفته شده است.
- [ ] اگر حذف (`DELETE`) اضافه کرده‌ام: متد در api همان ماژول است، `GRANT DELETE` فقط روی همان جدول است، و برای داده‌ی مالی soft delete را بررسی کرده‌ام.
- [ ] تست‌ها هم روی H2 و هم (برای تغییرات دیتابیسی) روی Oracle با `-Dtest.profile=oracle` سبز هستند.
- [ ] هیچ `REQUIRES_NEW` یا catch خالی در مسیر تراکنش چندماژولی نیست.

## ۱۲. محدودیت‌های شناخته‌شده

- ArchUnit فقط Entityها را بررسی می‌کند و native query یا JPQL که اسکیمای ماژول دیگر را بزند تشخیص نمی‌دهد. در code review بررسی شود.
- کاربر اتصال به همه‌ی اسکیماها دسترسی دارد، پس جداسازی در سطح دیتابیس اجباری نشده است (در انتظار تصمیم معماری فنی).
- لایه‌ی REST هنوز در تمپلیت نیست. controllerها در `shafagh-app` (یا یک ماژول وب جدا) نوشته شوند و برای عملیات چندماژولی فقط orchestration را صدا بزنند.
- تست‌ها به‌صورت پیش‌فرض روی H2 اجرا می‌شوند؛ برای اطمینان از DDL و دسترسی‌های واقعی باید با `-Dtest.profile=oracle` هم اجرا شوند.
