select *
from pub
where uniquename like 'PMID:%'
and uniquename !~ E'^PMID:[0-9]+$'
;
