 
#Checks that all transcript components (exons, utrs and promoters) lie within the 
#boundaries of the transcript


select locsub.feature_id as sub_id
     , sub.uniquename as sub_uniquename
     , locsub.fmin as sub_min
     , locsub.fmax as sub_max
     , loctranscript.feature_id as transcript_id
     , transcript.uniquename as transcript_uniquename
     , loctranscript.fmin as transcript_min
     , loctranscript.fmax as transcript_max
from feature sub
join featureloc locsub using (feature_id)
join feature_relationship sub_transcript on sub_transcript.subject_id = sub.feature_id
join feature transcript on sub_transcript.object_id = transcript.feature_id
join featureloc loctranscript on loctranscript.feature_id = transcript.feature_id
where transcript.type_id in (
        select cvterm_id 
        from cvterm
        where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA')
 
)
and (locsub.fmax > loctranscript.fmax
     or locsub.fmin < loctranscript.fmin)
and locsub.srcfeature_id = loctranscript.srcfeature_id
and sub_transcript.type_id = (select cvterm_id
                              from cvterm
                              where name='part_of')
order by transcript_uniquename
;
