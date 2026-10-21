-- دسترسی کاربر اتصال برنامه (APP_USER) به جدول‌ها و sequence ها. بعد از 02-create-tables.sql اجرا شود.
-- APP_USER را با نام کاربر اتصال خودتان (DB_USERNAME) عوض کنید.
--
-- نکته‌ها:
-- * برنامه با یک کاربر به همه‌ی اسکیماها وصل می‌شود (برای تراکنش واحد چندماژولی)؛ پس این کاربر
--   روی جدول‌های هر چهار اسکیما حق نوشتن دارد، حتی اگر کد هر ماژول فقط به جدول‌های خودش دست بزند.
-- * DELETE عمداً داده نشده است. هر جدولی که واقعاً حذف فیزیکی دارد، فقط برای همان جدول
--   `GRANT DELETE ...` جداگانه بدهید (و در PR مشخص کنید). برای داده‌ی مالی soft delete (تغییر STATUS) ترجیح دارد.

GRANT SELECT, INSERT, UPDATE ON SHFQ_BASE.BRANCH_CODE_COUNTER    TO APP_USER;
GRANT SELECT, INSERT, UPDATE ON SHFQ_BASE.ACCOUNT_NUMBER_COUNTER TO APP_USER;
GRANT SELECT                 ON SHFQ_BASE.CUSTOMER_CODE_SEQ     TO APP_USER;
-- شمارنده‌ها حتی پس از بسته‌شدن حساب یا غیرفعال‌شدن شعبه نگهداری می‌شوند؛ DELETE داده نمی‌شود.

GRANT SELECT, INSERT, UPDATE ON SHFQ_BASE.SHFQ_BASE_CURRENCY TO APP_USER;

GRANT SELECT, INSERT, UPDATE ON SHFQ_CIF.SHFQ_CIF_PERSON     TO APP_USER;
GRANT SELECT                 ON SHFQ_CIF.SHFQ_CIF_PERSON_SEQ TO APP_USER;

GRANT SELECT, INSERT, UPDATE ON SHFQ_DPST.SHFQ_DPST_DEPOSIT     TO APP_USER;
GRANT SELECT                 ON SHFQ_DPST.SHFQ_DPST_DEPOSIT_SEQ TO APP_USER;

GRANT SELECT, INSERT, UPDATE ON SHFQ_LOAN.SHFQ_LOAN_LOAN     TO APP_USER;
GRANT SELECT                 ON SHFQ_LOAN.SHFQ_LOAN_LOAN_SEQ TO APP_USER;
