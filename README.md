# Shafagh – قالب پروژه‌ی چندماژولی (Modular Monolith)

> **راهنمای کامل برنامه‌نویسان: [docs/DEVELOPER-GUIDE.md](docs/DEVELOPER-GUIDE.md)**

Java 21 · Spring Boot · Maven · Oracle · JPA/Hibernate · پکیج پایه: `com.misc.shafagh`

## ماژول‌ها

| ماژول | پکیج | اسکیمای Oracle |
|---|---|---|
| اطلاعات پایه و مشترک (SHFQ_BASE) | `com.misc.shafagh.base` | `SHFQ_BASE` |
| اشخاص (SHFQ_CIF) | `com.misc.shafagh.cif` | `SHFQ_CIF` |
| سپرده (SHFQ_DPST) | `com.misc.shafagh.dpst` | `SHFQ_DPST` |
| تسهیلات (SHFQ_LOAN) | `com.misc.shafagh.loan` | `SHFQ_LOAN` |

هر ماژول یک پوشه‌ی گروه `shafagh-<m>/` است (pom تجمیعی) که دو Maven module دارد: `shafagh-<m>-api` (اینترفیس‌ها + DTO، بدون JPA/Spring) و `shafagh-<m>-impl` (سرویس، Entity، Repository).
علاوه بر آن‌ها: `shafagh-orchestration` (سناریوهای چندماژولی)، `shafagh-architecture-tests` (ArchUnit) و `shafagh-app` (اجرا).

## قوانین (توسط ArchUnit کنترل می‌شود و نقض آن‌ها بیلد را می‌شکند)
1. ماژول‌ها فقط به `api` همدیگر وابسته‌اند، نه به `impl`.
2. جهت وابستگی: `base ← cif ← dpst ← loan` و بدون حلقه.
3. هر Entity فقط به جدولی در اسکیمای ماژول خودش نگاشت می‌شود (`@Table(schema = <M>Schema.NAME, ...)`) و رابطه‌ی JPA به Entity ماژول دیگر ندارد (فقط شناسه نگه می‌دارید).
4. api ها به Spring/JPA وابسته نیستند.

## تراکنش چندماژولی (یا همه یا هیچ)
مرز تراکنش در `shafagh-orchestration` است (`@Transactional(rollbackFor = Exception.class)`).
سرویس‌های هر ماژول با `REQUIRED` در همان تراکنش شرکت می‌کنند. نمونه: `LoanApplicationService`
و تست `MultiModuleTransactionTest` که rollback را در هر مرحله اثبات می‌کند. در سرویس‌های ماژول‌ها از `REQUIRES_NEW` استفاده نکنید.

## اجرا
```
mvn clean verify                          # بیلد + تست معماری + تست‌های سناریو (روی H2 در حالت Oracle)
mvn clean verify -Dtest.profile=oracle    # همان تست‌ها روی Oracle واقعی (+ تست ساختار اسکیماها)
export DB_URL=... DB_USERNAME=... DB_PASSWORD=...
mvn -pl shafagh-app spring-boot:run
```
اسکریپت‌های Oracle (به ترتیب): `scripts/01-create-schemas-and-user.sql` (DBA)، `scripts/02-create-tables.sql`، `scripts/03-grants.sql` (بدون DELETE) و فقط روی دیتابیس تست `scripts/04-grants-test-only.sql` (DELETE برای پاک‌کردن داده‌ی تست).

## افزودن ماژول جدید
1. پوشه‌ی `shafagh-<m>/` با یک pom تجمیعی (از روی `shafagh-loan/pom.xml` کپی کنید) و داخل آن `shafagh-<m>-api` و `shafagh-<m>-impl` بسازید (در pom هر دو `<relativePath>../../pom.xml</relativePath>` بگذارید). پوشه‌ی گروه را در `modules` و artifactهای api/impl را در `dependencyManagement` پوم ریشه ثبت کنید.
2. پکیج‌ها: `com.misc.shafagh.<m>.api` و `com.misc.shafagh.<m>.impl.{entity,repository,service}`
3. در `impl` کلاس `<M>Schema` با ثابت `NAME` بسازید (اسکیمای Oracle با DBA هماهنگ شود) و در `@Table(schema = ...)` و `@SequenceGenerator(schema = ...)` از آن استفاده کنید.
4. نام ماژول را به `MODULES` در `ModuleArchitectureTest` و اسکیمای آن را به ساخت اسکیماهای H2 تست اضافه کنید.
5. impl را به `shafagh-app` و `shafagh-architecture-tests` اضافه کنید.

## اسکیماها
هر ماژول جدول‌ها و sequence هایش را در اسکیمای خودش دارد و همه‌ی اسکیماها در یک دیتابیس متمرکز هستند.
برنامه با **یک کاربر اتصال** وصل می‌شود (تا تراکنش واحد و rollback چندماژولی ممکن باشد) و به هر اسکیما فقط grant لازم را دارد (`scripts/03-grants.sql`).
چون یک کاربر به همه‌ی اسکیماها دسترسی دارد، خود Oracle جلوی دسترسی ماژول‌ها به اسکیمای همدیگر را نمی‌گیرد؛ این قانون با ArchUnit و code review کنترل می‌شود.

## محدودیت‌های شناخته‌شده
- کنترل جدول‌ها در ArchUnit فقط روی Entityها است؛ native query یا JPQL که جدول ماژول دیگر را بزند را تشخیص نمی‌دهد (در code review دیده شود، یا هر ماژول را با schema/user جدا در Oracle جدا کنید).
- لایه‌ی وب (REST) هنوز اضافه نشده است.
