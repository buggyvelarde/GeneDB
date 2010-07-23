-- This sql extends the length of pseudogenic_transcripts to encompass the utrs that are currently jutting out at either end
-- This sql can be modified to deal with other organisms and 3' utrs
-- nds

begin; 

create temporary table short_transcripts as
select pt.feature_id as pseudogenic_transcript
     , pt.uniquename as name
     , ptloc.fmin as pt_start
     , ptloc.fmax as pt_end
     , ptloc.strand as pt_strand
     , min(utrloc.fmin) as new_start
     , max(utrloc.fmax) as new_end
from feature pt   
join feature_relationship utr_pt on utr_pt.object_id = pt.feature_id
join feature utr on utr.feature_id=utr_pt.subject_id
join featureloc utrloc on utr.feature_id=utrloc.feature_id
join featureloc ptloc on pt.feature_id=ptloc.feature_id
and pt.type_id = (select cvterm_id 
                     from cvterm
                     where name='pseudogenic_transcript' )
and utr.type_id in (select cvterm_id
                    from cvterm
                    where name in ('five_prime_UTR')) --,'three_prime_UTR'))
and ptloc.srcfeature_id = utrloc.srcfeature_id
and pt.organism_id= (select organism_id
                       from organism
                       where common_name='Tbruceibrucei927')
group by pt.feature_id, pt.uniquename, ptloc.fmin, ptloc.fmax, ptloc.strand
having (min(utrloc.fmin) < ptloc.fmin and ptloc.strand='1') or (max(utrloc.fmax) > ptloc.fmax and ptloc.strand='-1');


-- For all the pseudogenic_transcripts on the positive strand, set the fmin to be the new fmin
update featureloc
set fmin = ( select new_start 
             from short_transcripts
             where pseudogenic_transcript = featureloc.feature_id)
where feature_id in ( select pseudogenic_transcript 
                      from short_transcripts
                      where pt_strand = '1');             

-- For all those on the negative strand, set the fmax to be the new fmax
update featureloc
set fmax = ( select new_end 
             from short_transcripts
             where pseudogenic_transcript = featureloc.feature_id)
where feature_id in ( select pseudogenic_transcript 
                      from short_transcripts
                      where pt_strand = '-1');     
                      
-- Are all the pseudo_gene lengths being adjusted?

commit;

                      
                      
                      
                      