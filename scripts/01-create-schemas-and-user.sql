-- بخش DBA. اگر اسکیماها و کاربر اتصال را قبلاً ساخته‌اید، این فایل را اجرا نکنید.
-- هر ماژول یک اسکیمای (user) مخصوص خودش دارد و همه‌ی اسکیماها در یک دیتابیس متمرکز هستند.
-- رمزها و tablespace را مطابق استاندارد خودتان بگذارید.

CREATE USER SHFQ_BASE IDENTIFIED BY "CHANGE_ME" QUOTA UNLIMITED ON USERS;
CREATE USER SHFQ_CIF  IDENTIFIED BY "CHANGE_ME" QUOTA UNLIMITED ON USERS;
CREATE USER SHFQ_DPST IDENTIFIED BY "CHANGE_ME" QUOTA UNLIMITED ON USERS;
CREATE USER SHFQ_LOAN IDENTIFIED BY "CHANGE_ME" QUOTA UNLIMITED ON USERS;

-- کاربر اتصال برنامه (همان که در DB_USERNAME می‌دهید)
CREATE USER APP_USER IDENTIFIED BY "CHANGE_ME";
GRANT CREATE SESSION TO APP_USER;
