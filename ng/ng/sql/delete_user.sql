\set username fooxxx


\set username_dq '"' :username '"'
\set username_sq '''' :username ''''
\set username_dq_sq '''' :username_dq ''''
\o /tmp/revoke.sql
\t
select 'revoke '||privilege_type||' on "'||table_schema||'"."'||table_name||'" from "'||grantee||'";' from information_schema.role_table_grants 
where grantee=:username_sq
;
select 'revoke all on public."' || sequence_name || '" from ' || :username_dq_sq || ';'
from information_schema.sequences
where sequence_schema = 'public'
;
\o
\t
\i /tmp/revoke.sql
drop user :username_dq;
