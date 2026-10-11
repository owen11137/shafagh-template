-- ساخت جدول‌ها و sequence ها. با کاربری اجرا شود که اجازه‌ی ساخت آبجکت در اسکیماهای دیگر را دارد
-- (مثلاً DBA یا کاربر دارای CREATE ANY TABLE / CREATE ANY SEQUENCE)، یا هر بخش را با خودِ مالک اسکیما
-- اجرا کنید و پیشوند اسکیما را بردارید.
--
-- نکته: بین جدول‌های اسکیماهای مختلف عمداً FK فیزیکی وجود ندارد؛ ارتباط ماژول‌ها از طریق سرویس‌هاست
-- (تست OracleSchemaTest همین را بررسی می‌کند).

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
