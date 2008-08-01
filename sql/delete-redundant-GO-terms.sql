delete from feature_cvterm general
using cv
   , cvterm evidence_type join cv evidence_type_cv using (cv_id)
   , feature_cvtermprop general_evidence
   , feature_cvterm specific
left join feature_cvtermprop specific_evidence
    on specific_evidence.feature_cvterm_id = specific.feature_cvterm_id
join cvterm specific_term on specific.cvterm_id = specific_term.cvterm_id
join cvtermpath on cvtermpath.subject_id = specific_term.cvterm_id
join cvterm reltype on cvtermpath.type_id = reltype.cvterm_id
join cv reltypecv on reltype.cv_id = reltypecv.cv_id
join cvterm general_term on cvtermpath.object_id = general_term.cvterm_id
join feature on specific.feature_id = feature.feature_id
where general.cvterm_id = general_term.cvterm_id
and general_evidence.feature_cvterm_id = general.feature_cvterm_id
and specific_term.cv_id = cv.cv_id
and general_term.cv_id = cv.cv_id
and cv.name in (
      'biological_process'
    , 'molecular_function'
    , 'cellular_component'
)
and specific.feature_id = general.feature_id
and reltype.name = 'is_a'
and reltypecv.name = 'relationship'
and evidence_type.name = 'evidence'
and evidence_type_cv.name = 'genedb_misc'
and specific_evidence.type_id = evidence_type.cvterm_id
and general_evidence.type_id = evidence_type.cvterm_id
and specific.feature_cvterm_id <> general.feature_cvterm_id
and general_evidence.value in (
      'inferred from electronic annotation'
    , 'Inferred from Electronic Annotation'
)
;
