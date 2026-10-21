-- ساخت جدول‌ها و sequence ها. با کاربری اجرا شود که اجازه‌ی ساخت آبجکت در اسکیماهای دیگر را دارد
-- (مثلاً DBA یا کاربر دارای CREATE ANY TABLE / CREATE ANY SEQUENCE)، یا هر بخش را با خودِ مالک اسکیما
-- اجرا کنید و پیشوند اسکیما را بردارید.
--
-- نکته: بین جدول‌های اسکیماهای مختلف عمداً FK فیزیکی وجود ندارد؛ ارتباط ماژول‌ها از طریق سرویس‌هاست
-- (تست OracleSchemaTest همین را بررسی می‌کند).

-------------------------------------------------------------------------------
-- SHFQ_BASE: شماره‌گذاری مشترک؛ برای نصب تازه، نه بازنشانی سامانه‌ی موجود
-------------------------------------------------------------------------------
CREATE SEQUENCE SHFQ_BASE.CUSTOMER_CODE_SEQ
    START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999
    NOCYCLE CACHE 100 NOORDER;

CREATE TABLE SHFQ_BASE.BRANCH_CODE_COUNTER (
    FUND_CODE   VARCHAR2(6 CHAR) NOT NULL,
    NEXT_NUMBER NUMBER(6,0)      NOT NULL,
    CONSTRAINT PK_BRANCH_CODE_COUNTER PRIMARY KEY (FUND_CODE),
    CONSTRAINT CK_BRANCH_COUNTER_FUND CHECK (
        LENGTH(FUND_CODE) = 6
        AND TRANSLATE(FUND_CODE, 'x0123456789', 'x') IS NULL
    ),
    -- پس از تخصیص آخرین شماره‌ی مجاز 99999، مقدار بعدی 100000 است.
    CONSTRAINT CK_BRANCH_COUNTER_NEXT CHECK (NEXT_NUMBER BETWEEN 1 AND 100000)
);

CREATE TABLE SHFQ_BASE.ACCOUNT_NUMBER_COUNTER (
    BRANCH_CODE VARCHAR2(11 CHAR) NOT NULL,
    NEXT_NUMBER NUMBER(7,0)       NOT NULL,
    CONSTRAINT PK_ACCOUNT_NUMBER_COUNTER PRIMARY KEY (BRANCH_CODE),
    CONSTRAINT CK_ACCOUNT_COUNTER_BRANCH CHECK (
        LENGTH(BRANCH_CODE) = 11
        AND TRANSLATE(BRANCH_CODE, 'x0123456789', 'x') IS NULL
    ),
    -- پس از تخصیص آخرین شماره‌ی مجاز 999999، مقدار بعدی 1000000 است.
    CONSTRAINT CK_ACCOUNT_COUNTER_NEXT CHECK (NEXT_NUMBER BETWEEN 1 AND 1000000)
);

-- رکورد شمارنده فقط هنگام ثبت صندوق/شعبه با NEXT_NUMBER = 1 درج می‌شود.
-- روی سامانه‌ی دارای داده، مقدارها را از شماره‌های قبلی مهاجرت دهید؛ بازنشانی ممنوع است.
-- جدول‌های واقعی صندوق/شعبه/حساب در این تمپلیت نیستند؛ FK بین اسکیماها ایجاد نمی‌شود.

-------------------------------------------------------------------------------
-- SHFQ_BASE
-------------------------------------------------------------------------------
CREATE TABLE SHFQ_BASE.SHFQ_BASE_CURRENCY (
    CODE  VARCHAR2(3)   NOT NULL PRIMARY KEY,
    NAME  VARCHAR2(100) NOT NULL
);

-------------------------------------------------------------------------------
-- SHFQ_CIF
-------------------------------------------------------------------------------
CREATE SEQUENCE SHFQ_CIF.SHFQ_CIF_PERSON_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE SHFQ_CIF.SHFQ_CIF_PERSON (
    ID            NUMBER(19)    NOT NULL PRIMARY KEY,
    NATIONAL_CODE VARCHAR2(20)  NOT NULL UNIQUE,
    FIRST_NAME    VARCHAR2(100) NOT NULL,
    LAST_NAME     VARCHAR2(100) NOT NULL,
    STATUS        VARCHAR2(20)  NOT NULL
);

-------------------------------------------------------------------------------
-- SHFQ_DPST
-------------------------------------------------------------------------------
CREATE SEQUENCE SHFQ_DPST.SHFQ_DPST_DEPOSIT_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE SHFQ_DPST.SHFQ_DPST_DEPOSIT (
    ID             NUMBER(19)    NOT NULL PRIMARY KEY,
    PERSON_ID      NUMBER(19)    NOT NULL,
    CURRENCY_CODE  VARCHAR2(3)   NOT NULL,
    BALANCE        NUMBER(19,2)  NOT NULL,
    BLOCKED_AMOUNT NUMBER(19,2)  DEFAULT 0 NOT NULL,
    ROW_VERSION    NUMBER(19)    DEFAULT 0 NOT NULL
);

-------------------------------------------------------------------------------
-- SHFQ_LOAN
-------------------------------------------------------------------------------
CREATE SEQUENCE SHFQ_LOAN.SHFQ_LOAN_LOAN_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE SHFQ_LOAN.SHFQ_LOAN_LOAN (
    ID         NUMBER(19)   NOT NULL PRIMARY KEY,
    PERSON_ID  NUMBER(19)   NOT NULL,
    DEPOSIT_ID NUMBER(19)   NOT NULL,
    AMOUNT     NUMBER(19,2) NOT NULL,
    STATUS     VARCHAR2(20) NOT NULL
);
