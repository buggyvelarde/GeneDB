delete from featureloc
using feature
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
join cv on feature_type.cv_id = cv.cv_id
where featureloc.feature_id = feature.feature_id
and featureloc.srcfeature_id is null
and cv.name = 'sequence'
and feature_type.name in (
    'membrane_structure'
  , 'cytoplasm_location'
  , 'non_cytoplasm_location'
  , 'transmembrane'
  , 'polypeptide_domain'
);
