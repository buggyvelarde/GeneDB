select
from feature transcript
join feature_relationship transcript_exon on transcript.feature_id = feature_relationship.object_id
join feature exon on transcript_exon.subject_id = exon.feature_id
where transcript_exon.rank is null;