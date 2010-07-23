#This checks if every transcript has a rank zero exon. Some transcripts (e.g., those that begin with 'RNAzID' and 'PF' in Falciparum) have been excluded from this test 
#as they are not expected to have exons.
#The LMexicana transcripts returned by this test are ignored until the new Mexicana data is loaded. For now, we ignore LMexicana altogether.

select organism.common_name as organism
     , transcript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
from feature transcript
join organism using (organism_id)
where transcript.type_id in (
                             select cvterm.cvterm_id
                             from cvterm join cv on cvterm.cv_id = cv.cv_id
                             where cv.name = 'sequence'
                             and cvterm.name in ('mRNA', 
                                                 'rRNA', 
                                                 'tRNA',
                                                 'snRNA',
                                                 'snoRNA',
                                                 'ncRNA',
                                                 'transcript'))
and transcript.organism_id != (
                              select organism_id 
                              from organism 
                              where common_name='Lmexicana') 
and not exists (
                select *
                from feature_relationship exon_transcript
                join feature exon on exon_transcript.subject_id = exon.feature_id
                where exon_transcript.object_id = transcript.feature_id
                and exon.type_id = (
                         select cvterm.cvterm_id
                         from cvterm join cv on cvterm.cv_id=cv.cv_id
                         where cv.name = 'sequence'
                         and cvterm.name='exon')
                and exon_transcript.rank = 0)
order by organism.common_name
;

