-- Run as a database administrator in the same PDB as DB_URL.
-- The four schema owners must already exist; this script does not create users.
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO SHFQ_BASE;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO SHFQ_CIF;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO SHFQ_DPST;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO SHFQ_LOAN;

-- Use each owner's actual default tablespace, rather than assuming USERS exists.
BEGIN
    FOR owner IN (
        SELECT username, default_tablespace FROM dba_users
        WHERE username IN ('SHFQ_BASE', 'SHFQ_CIF', 'SHFQ_DPST', 'SHFQ_LOAN')
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER USER ' || DBMS_ASSERT.ENQUOTE_NAME(owner.username, FALSE)
            || ' QUOTA UNLIMITED ON ' || DBMS_ASSERT.ENQUOTE_NAME(owner.default_tablespace, FALSE);
    END LOOP;
END;
/
