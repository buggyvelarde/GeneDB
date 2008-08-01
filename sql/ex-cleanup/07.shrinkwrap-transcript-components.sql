begin;

create temporary table shrinkwrap_min as
select transcript.feature_id
     , transcriptloc.fmin as transcript_min
     , min(subloc.fmin) as min_sub_min
from feature transcript
join featureloc transcriptloc using (feature_id)
join feature_relationship sub_transcript
    on sub_transcript.object_id = transcript.feature_id
join feature sub on sub_transcript.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
    , 604 /*pseudogenic_transcript*/
)
  and sub.type_id <> 191 /*polypeptide*/
group by transcript.feature_id, transcriptloc.fmin
having transcriptloc.fmin <> min(subloc.fmin)
;

update featureloc
set fmin = min_sub_min
from shrinkwrap_min
where shrinkwrap_min.feature_id = featureloc.feature_id
  and shrinkwrap_min.transcript_min = featureloc.fmin
;

commit;



begin;

create temporary table shrinkwrap_max as
select transcript.feature_id
     , transcriptloc.fmax as transcript_max
     , max(subloc.fmax) as max_sub_max
from feature transcript
join featureloc transcriptloc using (feature_id)
join feature_relationship sub_transcript
    on sub_transcript.object_id = transcript.feature_id
join feature sub on sub_transcript.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
where transcript.type_id in (
      321 /*mRNA*/
    , 339 /*rRNA*/
    , 340 /*tRNA*/
    , 361 /*snRNA*/
    , 604 /*pseudogenic_transcript*/
)
  and sub.type_id <> 191 /*polypeptide*/
group by transcript.feature_id, transcriptloc.fmax
having transcriptloc.fmax <> max(subloc.fmax)
;

update featureloc
set fmax = max_sub_max
from shrinkwrap_max
where shrinkwrap_max.feature_id = featureloc.feature_id
  and shrinkwrap_max.transcript_max = featureloc.fmax
;

commit;
