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
