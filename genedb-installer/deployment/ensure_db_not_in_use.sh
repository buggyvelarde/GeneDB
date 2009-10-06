#!/bin/bash -xv


# Switch snapshot and staging dbs
ssh localhost bash <<'BASH'
  killkillkill="$(mktemp /tmp/killkillkill.XXXXXXXX)"
  psql --tuples-only -h pgsrv2 template1 << PSQL
  \o $killkillkill
  select 'kill ' || procpid from pg_stat_activity where datname = 'nightly';
  \o
  \! . $killkillkill
  \! sleep 3
PSQL
  rm $killkillkill
BASH
