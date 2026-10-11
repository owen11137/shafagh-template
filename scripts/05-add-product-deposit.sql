-- ارتقای یک دیتابیس موجود که چهار اسکیمای برنامه از قبل در آن ساخته شده‌اند.
-- این فایل را فقط یک‌بار، به‌جای اجرای دوباره‌ی 02-create-tables.sql، اجرا کنید.
-- برای نصب تازه از فایل‌های 01 تا 03 استفاده کنید و بعد از آن این فایل را اجرا نکنید.
-- با DBA یا کاربر دارای اجازه‌ی ساخت جدول/sequence در SHFQ_DPST و اعطای دسترسی اجرا شود.
-- APP_USER را با نام کاربر اتصال برنامه (DB_USERNAME) عوض کنید.

CREATE SEQUENCE SHFQ_DPST.PRODUCT_DEPOSIT_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE SHFQ_DPST.PRODUCT_DEPOSIT (
    ID           NUMBER(19)      NOT NULL PRIMARY KEY,
    FUND_ID      NUMBER(19)      NOT NULL,
    TITLE        VARCHAR2(255),
    CODE         NVARCHAR2(255)  NOT NULL UNIQUE,
    DESCRIPTION  NVARCHAR2(255),
    -- ORDINAL: ACTIVE = 0, INACTIVE = 1
    STATUS       NUMBER(3),
    -- ORDINAL: DEPOSIT1 = 0, DEPOSIT2 = 1
    DEPOSIT_TYPE NUMBER(3)       NOT NULL,
    FROM_DATE    TIMESTAMP(6),
    TO_DATE      TIMESTAMP(6),
    CREATED_BY   VARCHAR2(30),
    CREATED_ON   TIMESTAMP(6),
    MODIFIED_BY  VARCHAR2(30),
    MODIFIED_ON  TIMESTAMP(6),
    OPT_LOCK     NUMBER(19)      DEFAULT 0
);

GRANT SELECT, INSERT, UPDATE ON SHFQ_DPST.PRODUCT_DEPOSIT        TO APP_USER;
GRANT SELECT                 ON SHFQ_DPST.PRODUCT_DEPOSIT_ID_SEQ TO APP_USER;
