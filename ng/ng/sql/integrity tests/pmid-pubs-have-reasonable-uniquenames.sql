#Pubmed ids have reasonable unique names which should look like PMID:number 
#Here too, we ignore the pub where pub_id=1 since it is a special case as are PMID:workshop and PMID:unpublished

select *
from pub
where 
   pub_id!=1 
and
   uniquename not like 'PMID%'
or
   (uniquename like 'PMID:%'
   and uniquename !~ E'^PMID:[0-9]+$'
   and uniquename not in ('PMID:workshop', 'PMID:unpublished'));
