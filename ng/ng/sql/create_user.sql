/*
Create a user in the malaria_workshop database,
for Artemis use.
*/

\set username jl8@sanger.ac.uk


\set username_quoted '"' :username '"'
create user :username_quoted;

\t
\o /tmp/grants.sql

select 'grant all on public."'||tablename||'" to :username_quoted;'
from pg_tables
where schemaname = 'public'
;
select 'grant all on public."'|| sequence_name || '" to :username_quoted;'
from information_schema.sequences
where sequence_schema = 'public'
;
\o
\t
\i /tmp/grants.sql

\password :username
