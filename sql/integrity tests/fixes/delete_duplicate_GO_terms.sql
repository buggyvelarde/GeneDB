delete from feature_cvterm
where feature_cvterm_id in (
select second.feature_cvterm_id from feature_cvterm second
   , cv
   , cvterm evidence_type join cv evidence_type_cv
        on evidence_type.cv_id = evidence_type_cv.cv_id
   , feature_cvtermprop second_evidence
   , feature_cvterm first
     left join feature_cvtermprop first_evidence
         on first_evidence.feature_cvterm_id = first.feature_cvterm_id
   , cvterm first_term
   , cvterm second_term
   , feature
where second.cvterm_id = second_term.cvterm_id
and second_evidence.feature_cvterm_id = second.feature_cvterm_id
and first_term.cv_id = cv.cv_id
and second_term.cv_id = cv.cv_id
and cv.name in (
      'biological_process'
    , 'molecular_function'
    , 'cellular_component'
)
and first.feature_id = feature.feature_id
and first.feature_id = second.feature_id
and second.cvterm_id = second_term.cvterm_id
and first.cvterm_id = first_term.cvterm_id
and first.pub_id = second.pub_id
and evidence_type.name = 'evidence'
and evidence_type_cv.name = 'genedb_misc'
and first_evidence.type_id = evidence_type.cvterm_id
and second_evidence.type_id = evidence_type.cvterm_id
and first.feature_cvterm_id < second.feature_cvterm_id
and upper(second_evidence.value) = upper(first_evidence.value)
and second_term.name = first_term.name
);

