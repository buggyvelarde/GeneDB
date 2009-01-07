Tidy up product terms by combining terms that differ only in case, spacing or punctuation.
We have to be careful with punctuation: for example, I/II does not mean the same as III.
On the whole we prefer terms that start with a lower-case letter, provided the second
character is a lower-case letter in both versions.

The script fix.sh is the main entry point. It requires the environment variables
PGHOST, PGPORT, PGDATABASE and PGUSER to be set appropriately.
