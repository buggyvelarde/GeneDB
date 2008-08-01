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
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
    , 604 /*pseudogenic_transcript*/
)
 and (locsub.fmax > loctranscript.fmax
   or locsub.fmin < loctranscript.fmin)
 and sub.type_id <> 191 /*polypeptide*/
;
