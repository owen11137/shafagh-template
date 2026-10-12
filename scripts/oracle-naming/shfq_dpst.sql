-- Run as SHFQ_DPST in the application's PDB BEFORE starting the new version.
-- Stop application writers during migration; Oracle DDL commits implicitly.
-- Renames preserve sequence state, rows and constraint/index definitions.
SET SERVEROUTPUT ON;
DECLARE
    existing_old NUMBER;
    existing_new NUMBER;
    target_name VARCHAR2(128);
    column_names VARCHAR2(1000);
    column_count NUMBER;
    parent_table VARCHAR2(128);
    role_columns VARCHAR2(1000);
    parent_count NUMBER;
    check_text VARCHAR2(4000);
    PROCEDURE rename_sequence(old_name VARCHAR2, new_name VARCHAR2) IS
    BEGIN
        SELECT COUNT(*) INTO existing_old FROM user_sequences WHERE sequence_name=old_name;
        SELECT COUNT(*) INTO existing_new FROM user_sequences WHERE sequence_name=new_name;
        IF existing_old > 0 AND existing_new > 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Both old and new sequences exist; DBA reconciliation required: ' || old_name);
        ELSIF existing_old > 0 THEN
            EXECUTE IMMEDIATE 'RENAME ' || DBMS_ASSERT.SIMPLE_SQL_NAME(old_name) || ' TO ' || DBMS_ASSERT.SIMPLE_SQL_NAME(new_name);
        END IF;
        IF existing_old > 0 OR existing_new > 0 THEN
            EXECUTE IMMEDIATE 'ALTER SEQUENCE ' || DBMS_ASSERT.SIMPLE_SQL_NAME(new_name) || ' NOCYCLE CACHE 50 NOORDER';
        END IF;
    END;
BEGIN
    IF USER <> 'SHFQ_DPST' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Connect as SHFQ_DPST, not an administrator or another schema');
    END IF;
    rename_sequence('DEPOSIT_PERSON_ID_SEQ', 'SEQ_DEPOSIT_PERSON_ID');
    rename_sequence('PRODUCT_DEPOSIT_ID_SEQ', 'SEQ_PRODUCT_DEPOSIT_ID');
    rename_sequence('DEPOSIT_ID_SEQ', 'SEQ_DEPOSIT_ID');

    FOR item IN (SELECT * FROM user_constraints
                 WHERE table_name IN ('DEPOSIT_PERSON', 'PRODUCT_DEPOSIT', 'DEPOSIT') AND constraint_type IN ('P','U','R','C')) LOOP
        SELECT LISTAGG(column_name, '_') WITHIN GROUP (ORDER BY position), COUNT(*)
            INTO column_names, column_count FROM user_cons_columns WHERE constraint_name=item.constraint_name;
        IF item.constraint_type='P' THEN
            target_name := 'PK_' || item.table_name;
        ELSIF item.constraint_type='U' THEN
            target_name := 'UK_' || item.table_name || '_' || column_names;
        ELSIF item.constraint_type='R' THEN
            SELECT parent.table_name INTO parent_table FROM user_constraints child
                JOIN all_constraints parent ON parent.owner=child.r_owner AND parent.constraint_name=child.r_constraint_name
                WHERE child.constraint_name=item.constraint_name;
            SELECT COUNT(*) INTO parent_count FROM user_constraints child
                JOIN all_constraints parent ON parent.owner=child.r_owner AND parent.constraint_name=child.r_constraint_name
                WHERE child.table_name=item.table_name AND child.constraint_type='R' AND parent.table_name=parent_table;
            target_name := 'FK_' || item.table_name || '_' || parent_table;
            IF parent_count > 1 THEN
                SELECT LISTAGG(REGEXP_REPLACE(column_name, '_ID$', ''), '_') WITHIN GROUP (ORDER BY position)
                    INTO role_columns FROM user_cons_columns WHERE constraint_name=item.constraint_name;
                target_name := target_name || '_' || role_columns;
            END IF;
        ELSE
            IF SUBSTR(item.constraint_name, 1, 4) = 'CHK_' THEN
                target_name := item.constraint_name;
            ELSIF column_count <> 1 OR column_names IS NULL OR LENGTH(column_names)>100 THEN
                RAISE_APPLICATION_ERROR(-20003, 'A DBA must name this CHECK rule: ' || item.constraint_name);
            ELSE
                SELECT search_condition_vc INTO check_text FROM user_constraints WHERE constraint_name=item.constraint_name;
                target_name := 'CHK_' || item.table_name || '_' || column_names;
                IF INSTR(UPPER(check_text), 'IS NOT NULL')>0 THEN target_name := target_name || '_NOT_NULL'; END IF;
            END IF;
        END IF;
        IF item.constraint_name <> target_name THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || DBMS_ASSERT.SIMPLE_SQL_NAME(item.table_name)
                || ' RENAME CONSTRAINT ' || DBMS_ASSERT.SIMPLE_SQL_NAME(item.constraint_name)
                || ' TO ' || DBMS_ASSERT.SIMPLE_SQL_NAME(target_name);
        END IF;
        IF item.constraint_type IN ('P','U') AND item.index_name IS NOT NULL THEN
            target_name := 'UDX_' || item.table_name || '_' || column_names;
            IF LENGTH(target_name)>50 THEN RAISE_APPLICATION_ERROR(-20004, 'Index name exceeds 50 characters: ' || target_name); END IF;
            IF item.index_name <> target_name THEN
                EXECUTE IMMEDIATE 'ALTER INDEX ' || DBMS_ASSERT.ENQUOTE_NAME(item.index_owner, FALSE) || '.'
                    || DBMS_ASSERT.ENQUOTE_NAME(item.index_name, FALSE) || ' RENAME TO ' || DBMS_ASSERT.SIMPLE_SQL_NAME(target_name);
            END IF;
        END IF;
    END LOOP;
END;
/
