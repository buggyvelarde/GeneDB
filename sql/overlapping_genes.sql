create view overlapping_genes as
select organism.common_name as organism_name
     , gene1_loc.strand as strand
     , gene1.uniquename as g1_uniquename, gene1_loc.fmin as g1_min, gene1_loc.fmax as g1_max
     , gene2.uniquename as g2_uniquename, gene2_loc.fmin as g2_min, gene2_loc.fmax as g2_max
from feature gene1, feature gene2, organism, cvterm gene_type, cv
   , featureloc gene1_loc, featureloc gene2_loc
where cv.name = 'sequence' and gene_type.name = 'gene' and gene_type.cv_id = cv.cv_id
  and gene1.organism_id = organism.organism_id
  and gene2.organism_id = organism.organism_id
  and gene1.type_id = gene_type.cvterm_id and gene1_loc.feature_id = gene1.feature_id
  and gene2.type_id = gene_type.cvterm_id and gene2_loc.feature_id = gene2.feature_id
  and gene1_loc.fmin <= gene2_loc.fmax
  and gene2_loc.fmin <= gene1_loc.fmax
  and gene1_loc.strand = gene2_loc.strand               -- the same strand ...
  and gene1_loc.srcfeature_id = gene2_loc.srcfeature_id -- of the same chromosome
  and gene1.feature_id < gene2.feature_id
;
-- 
-- 
-- select '(' || subject.cvterm_id || ') ' || subject.name as subject
--      , reltype.name as verb
--      , '(' || object.cvterm_id || ') ' || object.name as object
-- from cvterm_relationship, cvterm reltype, cvterm subject, cvterm object
-- where cvterm_relationship.object_id  = object.cvterm_id
--   and cvterm_relationship.type_id    = reltype.cvterm_id
--   and cvterm_relationship.subject_id = subject.cvterm_id
--   and 792 in (subject.cvterm_id, object.cvterm_id)
-- ;
-- 
-- select feature_relationship.subject_id, subject_type.name, rel_type.name
-- from feature_relationship, cvterm rel_type, cvterm subject_type
-- where feature_relationship.type_id = rel_type.cvterm_id
--   and subject.type_id = subject_type.cvterm_id
--   and feature_relationship.object_id = 965;
-- 
-- 
-- select feature_type.name, count(*)
-- from featureprop colour, feature, cvterm feature_type
-- where colour.feature_id = feature.feature_id
--   and feature.type_id = feature_type.cvterm_id
--   and colour.type_id = 26768
--   and colour.value is null
-- group by feature_type.name
-- ;
-- 
-- 
