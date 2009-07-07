delete from feature_cvterm
where feature_cvterm_id in (
select general.feature_cvterm_id from feature_cvterm general
   , cv                    
   , cvterm evidence_type join cv evidence_type_cv
        on evidence_type.cv_id = evidence_type_cv.cv_id
   , feature_cvtermprop general_evidence
   , feature_cvterm specific 
left join feature_cvtermprop specific_evidence
    on specific_evidence.feature_cvterm_id = specific.feature_cvterm_id
                                                                       
   , cvterm specific_term 
   , cvterm general_term   
   , feature               
where general.cvterm_id = general_term.cvterm_id
and general_evidence.feature_cvterm_id = general.feature_cvterm_id
and specific_term.cv_id = cv.cv_id
and general_term.cv_id = cv.cv_id
and cv.name in (
      'biological_process'
    , 'molecular_function'
    , 'cellular_component'
)                         
and specific.feature_id = feature.feature_id
and specific.feature_id = general.feature_id
and general.cvterm_id = general_term.cvterm_id
and specific.cvterm_id = specific_term.cvterm_id
and specific.pub_id = general.pub_id
and evidence_type.name = 'evidence'
and evidence_type_cv.name = 'genedb_misc'
and specific_evidence.type_id = evidence_type.cvterm_id
and general_evidence.type_id = evidence_type.cvterm_id
and specific.feature_cvterm_id < general.feature_cvterm_id
and upper(general_evidence.value) = upper(specific_evidence.value)
and general_term.name = specific_term.name
);

