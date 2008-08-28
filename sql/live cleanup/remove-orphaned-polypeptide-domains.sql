delete from featureloc
using feature
where featureloc.feature_id = feature.feature_id
and featureloc.srcfeature_id is null
and feature.type_id in (
    1158 /*membrane_structure*/
  , 1160 /*cytoplasm_location*/
  , 1161 /*non_cytoplasm_location*/
  , 1164 /*transmembrane*/
  , 504  /*polypeptide_domain*/
);
