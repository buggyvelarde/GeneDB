-- This sql adds strands information from the genes to their UTRs (in cases where the UTR strand info is null)
-- We do this currently for the 5' UTRs of TBrucei927 as requested by Matt Rogers, but the SQL below can easily be modified to
-- do others
-- nds

begin;

-- Get the gene strands for all the utrs in Tbrucei that have no strand information

create temporary table utr_strands as
select utr.feature_id as utr
     , geneloc.strand as gene_strand
from feature gene   
join feature_relationship mrna_gene on mrna_gene.object_id=gene.feature_id
join feature mrna on mrna_gene.subject_id=mrna.feature_id
join feature_relationship utr_mrna on utr_mrna.object_id = mrna.feature_id
join feature utr on utr.feature_id=utr_mrna.subject_id
join featureloc utrloc on utr.feature_id=utrloc.feature_id
join featureloc geneloc on gene.feature_id=geneloc.feature_id
where gene.type_id = (select cvterm_id 
                      from cvterm 
                      where name='gene')
and mrna.type_id in (select cvterm_id 
                     from cvterm
                     where name in ('mRNA', 'transcript'))
and utr.type_id in (select cvterm_id
                    from cvterm
                    where name in ('five_prime_UTR')) --,'three_prime_UTR'))
and geneloc.srcfeature_id = utrloc.srcfeature_id
and utrloc.strand is null
and gene.organism_id= (select organism_id
                       from organism
                       where common_name='Tbruceibrucei927');

-- Add strand information to featureloc table
update featureloc
set strand = (select gene_strand
              from utr_strands
              where utr=featureloc.feature_id)
where feature_id in (select utr
                     from utr_strands);
                     
commit;
                     
                     
                       
                       
                       --

