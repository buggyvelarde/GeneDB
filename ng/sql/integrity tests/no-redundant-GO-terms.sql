#Checks that there are no redundant GO terms. Some of the results returned could be duplicate GO terms. The solution is
#to run the delete_duplicate_GO_terms.sql script followed by delete_redundant_GO_terms.sql.

select feature.uniquename
     , general.feature_cvterm_id
     , cv.name
        || ' ''' || general_term.name || ''' subsumed by '''
        || specific_term.name || ''', ' || specific_evidence.value || '.'
from cv
   , cvterm evidence_type join cv evidence_type_cv using (cv_id)
   , feature_cvterm specific
left join feature_cvtermprop specific_evidence
    on specific_evidence.feature_cvterm_id = specific.feature_cvterm_id
join cvterm specific_term on specific.cvterm_id = specific_term.cvterm_id
join cvtermpath on cvtermpath.subject_id = specific_term.cvterm_id
join cvterm reltype on cvtermpath.type_id = reltype.cvterm_id
join cv reltypecv on reltype.cv_id = reltypecv.cv_id
join cvterm general_term on cvtermpath.object_id = general_term.cvterm_id
join feature_cvterm general on general.cvterm_id = general_term.cvterm_id
left join feature_cvtermprop general_evidence
    on general_evidence.feature_cvterm_id = general.feature_cvterm_id
join feature on specific.feature_id = feature.feature_id
where specific_term.cv_id = cv.cv_id
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
and specific.cvterm_id <> general.cvterm_id
and general_evidence.value in (
      'inferred from electronic annotation'
    , 'Inferred from Electronic Annotation'
    , 'IEA'
)
;
