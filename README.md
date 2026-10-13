# shafagh-template
shafagh-template

## صفحه‌بندی REST و Swagger

فهرست‌های `GET /base/city`، `/base/fund`، `/base/branch`، `/dpst/products` و `/loan/products` و جست‌وجوهای `POST /customer/real/filter` و `/customer/legal/filter` پارامترهای query مشترک دارند:

- `page`: شماره صفحه از صفر؛ پیش‌فرض ۰.
- `size`: تعداد رکورد؛ پیش‌فرض ۲۰، حداکثر ۲۰۰۰.
- `sort`: نام فیلد و جهت؛ پیش‌فرض `id,asc`، قابل تکرار برای چند فیلد.

مثال: `/dpst/products?page=0&size=10&sort=title,asc&sort=id,desc`.

پاسخ فهرست‌ها شیء صفحه با `content`, `number`, `size`, `totalElements`, `totalPages` است؛ فهرست‌های BASE نیز اکنون همین ساختار را دارند و آرایه ساده نیستند. شهرهای دارای `parentId=0` استان‌اند و از فهرست شهرها و شمارش صفحات حذف می‌شوند. توضیحات عملیات و DTOهای محصولات سپرده و تسهیلات و ورودی‌های صفحه‌بندی در Swagger عمومی و گروه‌های ماژول‌ها موجودند: `/swagger-ui/index.html`.

## هویت موقت کاربر و اتصال آینده به JWT

قرارداد `CurrentUserContext` و snapshot تغییرناپذیر `UserContext` در `shafagh-common` قرار دارند. هر کلاس مدیریت‌شده توسط Spring می‌تواند قرارداد را با constructor injection دریافت کند:

```java
private final CurrentUserContext currentUserContext;

public void execute() {
    UserContext user = currentUserContext.current();
    long fundId = user.fundId();
    long userId = user.userId();
    long branchId = user.branchId();
}
```

برای یک عملیات، `current()` را در زمان اجرای همان عملیات بخوانید؛ اطلاعات کاربر را در constructor، فیلد static یا singleton ذخیره نکنید. متدهای میانبر `fundId()`, `userId()`, `branchId()` هم در قرارداد موجودند. کلاس‌های بدون Spring می‌توانند قرارداد یا snapshot را به‌عنوان آرگومان دریافت کنند.

فعلاً Bean موقت در `CurrentUserContextConfiguration` مقادیر ثابت کل برنامه را از `shafagh.fund-id`, `shafagh.user-id`, `shafagh.branch-id` می‌گیرد؛ متغیرهای محیطی `FUND_ID`, `USER_ID`, `BRANCH_ID` و پیش‌فرض هرکدام ۱ هستند. این مقادیر شناسه‌اند، به‌معنی وجود رکورد متناظر در دیتابیس یا احراز هویت نیستند. مقدار هر سه باید مثبت باشد. از هدر یا بدنه درخواست خوانده نمی‌شوند.

سرویس‌های محصولات، افتتاح سپرده و ثبت مشتری، resolver مربوط به Hibernate Tenant و JPA auditing همین قرارداد را مصرف می‌کنند. `createdBy` و `modifiedBy` شناسه کاربر را به صورت رشته ذخیره می‌کنند؛ فیلتر tenant از `fundId` می‌آید. `branchId` در دسترس است ولی قاعده جدیدی برای محدودیت شعبه یا ذخیره آن به Entityها اضافه نشده است.

بعداً یک Bean پیاده‌ساز `CurrentUserContext` تعریف کنید که اطلاعات کاربر احراز هویت‌شده را از Spring SecurityContext بخواند و `UserContext` بسازد. Bean موقت با `@ConditionalOnMissingBean` خودکار کنار می‌رود و کدهای مصرف‌کننده تغییر نمی‌کنند. در پیاده‌سازی واقعی، برای نبود احراز هویت یا claimهای ضروری خطا برگردانید؛ به شناسه‌های موقت fallback نکنید. برای jobها/async باید هویت صریحاً منتقل شود؛ قرارداد فعلی ادعای انتقال خودکار SecurityContext به thread دیگر ندارد. Hibernate مالکیت tenant را هنگام باز شدن session تعیین می‌کند، بنابراین fund را وسط یک session/transaction عوض نکنید.
