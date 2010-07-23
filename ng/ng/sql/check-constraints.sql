/* List all CHECK constraints */

select pg_namespace.nspname  as schema
     , pg_class.relname      as table_name
     , pg_constraint.conname as constraint_name
     , pg_constraint.consrc  as source
from pg_constraint
join pg_namespace on pg_constraint.connamespace = pg_namespace.oid
join pg_class on pg_constraint.conrelid = pg_class.oid
where pg_constraint.contype = 'c'
;
