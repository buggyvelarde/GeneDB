-- This SQL extends the lengths of the Tbrucei mrnas to include the 5' UTR features which are
-- currently jutting out at either end. This sql can be modified for other organisms and 3' utrs

-- nds

begin; 

create temporary table short_mrnas as
select mrna.feature_id as mrna
     , mrna.uniquename as name
     , mrnaloc.fmin as mrna_start
     , mrnaloc.fmax as mrna_end
     , mrnaloc.strand as mrna_strand
     , min(utrloc.fmin) as new_start
     , max(utrloc.fmax) as new_end
from feature mrna   
join feature_relationship utr_mrna on utr_mrna.object_id = mrna.feature_id
join feature utr on utr.feature_id=utr_mrna.subject_id
join featureloc utrloc on utr.feature_id=utrloc.feature_id
join featureloc mrnaloc on mrna.feature_id=mrnaloc.feature_id
and mrna.type_id in (select cvterm_id 
                     from cvterm
                     where name in ('mRNA', 'transcript'))
and utr.type_id in (select cvterm_id
                    from cvterm
                    where name in ('five_prime_UTR')) --,'three_prime_UTR'))
and mrnaloc.srcfeature_id = utrloc.srcfeature_id
and mrna.organism_id= (select organism_id
                       from organism
                       where common_name='Tbruceibrucei927')
group by mrna.feature_id, mrna.uniquename, mrnaloc.fmin, mrnaloc.fmax, mrnaloc.strand
having (min(utrloc.fmin) < mrnaloc.fmin and mrnaloc.strand='1') or (max(utrloc.fmax) > mrnaloc.fmax and mrnaloc.strand='-1');


-- For all the mrnas on the positive strand, set the fmin to be the new fmin
update featureloc
set fmin = ( select new_start 
             from short_mrnas
             where mrna = featureloc.feature_id)
where feature_id in ( select mrna 
                      from short_mrnas
                      where mrna_strand = '1');             

-- For all the mrnas on the negative strand, set the fmax to be the new fmax
update featureloc
set fmax = ( select new_end 
             from short_mrnas
             where mrna = featureloc.feature_id)
where feature_id in ( select mrna 
                      from short_mrnas
                      where mrna_strand = '-1'); 
                      
-- Are all the gene lengths being adjusted?

commit;

                      
                      
                      
                      