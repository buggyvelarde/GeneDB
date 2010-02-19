-- This SQL extends the lengths of the Tbrucei genes to include the 5' UTR features which are
-- currently jutting out at either end. This can be performed for any other organism by
-- changing the common_name below
-- nds

begin; 

create temporary table short_genes as
select gene.feature_id as gene
     , gene.uniquename as name
     , geneloc.fmin as gene_start
     , geneloc.fmax as gene_end
     , geneloc.strand as gene_strand
     , min(utrloc.fmin) as new_start
     , max(utrloc.fmax) as new_end
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
and gene.organism_id= (select organism_id
                       from organism
                       where common_name='Tbruceibrucei927')
group by gene.feature_id, gene.uniquename, geneloc.fmin, geneloc.fmax, geneloc.strand
having (min(utrloc.fmin) < geneloc.fmin and geneloc.strand='1') or (max(utrloc.fmax) > geneloc.fmax and geneloc.strand='-1');


-- For all the genes on the positive strand, set the fmin to be the new fmin
update featureloc
set fmin = ( select new_start 
             from short_genes
             where gene = featureloc.feature_id)
where feature_id in ( select gene 
                      from short_genes
                      where gene_strand = '1');             

-- For all the genes on the negative strand, set the fmax to be the new fmax
update featureloc
set fmax = ( select new_end 
             from short_genes
             where gene = featureloc.feature_id)
where feature_id in ( select gene 
                      from short_genes
                      where gene_strand = '-1'); 
                      
-- Are all the MRNA lengths being adjusted?

commit;

                      
                      
                      
                      