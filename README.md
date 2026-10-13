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
