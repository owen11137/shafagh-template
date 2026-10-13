# shafagh-template
shafagh-template

## صفحه‌بندی REST و Swagger

فهرست‌های `GET /base/city`، `/base/fund`، `/base/branch`، `/dpst/products` و `/loan/products` و جست‌وجوهای `POST /customer/real/filter` و `/customer/legal/filter` پارامترهای query مشترک دارند:

- `page`: شماره صفحه از صفر؛ پیش‌فرض ۰.
- `size`: تعداد رکورد؛ پیش‌فرض ۲۰، حداکثر ۲۰۰۰.
- `sort`: نام فیلد و جهت؛ پیش‌فرض `id,asc`، قابل تکرار برای چند فیلد.

مثال: `/dpst/products?page=0&size=10&sort=title,asc&sort=id,desc`.

پاسخ فهرست‌ها شیء صفحه با `content`, `number`, `size`, `totalElements`, `totalPages` است؛ فهرست‌های BASE نیز اکنون همین ساختار را دارند و آرایه ساده نیستند. شهرهای دارای `parentId=0` استان‌اند و از فهرست شهرها و شمارش صفحات حذف می‌شوند. توضیحات عملیات و DTOهای محصولات سپرده و تسهیلات و ورودی‌های صفحه‌بندی در Swagger عمومی و گروه‌های ماژول‌ها موجودند: `/swagger-ui/index.html`.
