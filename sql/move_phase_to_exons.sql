/*
 * At one stage there was a bug in the loader (really a misunderstanding on my part)
 * with the effect that the phase was applied to transcripts rather than exons. This
 * script corrects that.
 */

begin;

create temporary table transcript_phase as
select transcript.feature_id
     , transcript.uniquename
     , transcriptloc.phase
from feature transcript
join featureloc transcriptloc using (feature_id)
where transcript.type_id in (
      321 /*mRNA*/
    , 604 /*pseudogenic_transcript*/
)
and transcriptloc.rank = 0
and transcriptloc.locgroup = 0
and transcriptloc.phase is not null
;

create temporary table exon_phase as
select exon.feature_id
     , exon.uniquename
     , transcript_phase.phase
from feature exon
join feature_relationship exon_transcript
    on exon_transcript.subject_id = exon.feature_id
join transcript_phase
    on exon_transcript.object_id = transcript_phase.feature_id
where exon.type_id in (
      234 /*exon*/
    , 595 /*pseudogenic_exon*/
)
;

update featureloc
set phase = exon_phase.phase
from exon_phase
where featureloc.feature_id = exon_phase.feature_id
;

/*
update featureloc
    set phase = null
    where feature_id in (select feature_id from transcript_phase)
;
*/

-- update featureloc
--     set phase = null
--     from feature
--     where featureloc.feature_id = feature.feature_id
--     and feature.type_id not in (
--           234 /*exon*/
--         , 595 /*pseudogenic_exon*/
--     )
-- ;

update featureloc set phase = null
where featureloc_id in (
    select featureloc_id
    from featureloc
    join feature using (feature_id)
    where feature.type_id not in (
              234 /*exon*/
            , 595 /*pseudogenic_exon*/
        )
    and phase is not null
);

commit;