insert into featureprop (feature_id, type_id, value, rank) 
select distinct exon.feature_id as feature_id,
     polypeptide_colour_term.cvterm_id as type_id, 
     polypeptide_colour.value as value,
     polypeptide_colour.rank as rank
from feature exon
join feature_relationship exon_transcript on exon.feature_id = exon_transcript.subject_id
join feature transcript on transcript.feature_id = exon_transcript.object_id
join feature_relationship polypeptide_transcript on polypeptide_transcript.object_id = transcript.feature_id
join feature polypeptide on polypeptide_transcript.subject_id = polypeptide.feature_id
join featureprop polypeptide_colour on polypeptide.feature_id = polypeptide_colour.feature_id
join cvterm polypeptide_colour_term on polypeptide_colour.type_id = polypeptide_colour_term.cvterm_id
join cv polypeptide_colour_cv on polypeptide_colour_term.cv_id = polypeptide_colour_cv.cv_id
where exon.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv using (cv_id)
    where cv.name = 'sequence'
    and cvterm.name in ('exon', 'pseudogenic_exon')
)
and transcript.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv using (cv_id)
    where cv.name = 'sequence'
    and cvterm.name in ('pseudogenic_transcript', 'mRNA')
)
and polypeptide.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv using (cv_id)
    where cv.name = 'sequence'
    and cvterm.name = 'polypeptide'
)
and polypeptide_colour_cv.name = 'genedb_misc'
and polypeptide_colour_term.name = 'colour'
and not exists (
    select *
    from featureprop
    where featureprop.feature_id = exon.feature_id
    and type_id in (
        select cvterm_id
        from cvterm join cv using (cv_id)
        where cv.name = 'genedb_misc'
        and cvterm.name = 'colour'
    )
)
;

