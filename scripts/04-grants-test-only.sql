-- فقط برای دیتابیس توسعه/تست! روی محیط واقعی (پروداکشن) اجرا نشود.
-- تست‌های Oracle (mvn verify -Dtest.profile=oracle) داده‌ی خودشان را بعد از اجرا پاک می‌کنند و برای همین
-- به DELETE روی جدول‌ها نیاز دارند. کد اصلی برنامه به این دسترسی‌ها نیازی ندارد.
-- APP_USER را با نام کاربر اتصال خودتان عوض کنید.

GRANT DELETE ON SHFQ_BASE.SHFQ_BASE_CURRENCY TO APP_USER;
GRANT DELETE ON SHFQ_CIF.SHFQ_CIF_PERSON     TO APP_USER;
GRANT DELETE ON SHFQ_DPST.SHFQ_DPST_DEPOSIT  TO APP_USER;
GRANT DELETE ON SHFQ_DPST.PRODUCT_DEPOSIT    TO APP_USER;
GRANT DELETE ON SHFQ_LOAN.SHFQ_LOAN_LOAN     TO APP_USER;
