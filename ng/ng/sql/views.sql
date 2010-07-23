create or replace view feature_v as
select organism.common_name as organism_name
     , feature_type.name as feature_type
     , feature.feature_id
     , feature.uniquename
     , featureloc.strand
     , featureloc.fmin
     , featureloc.fmax
from feature
join organism using (organism_id)
join featureloc using (feature_id)
join cvterm feature_type on feature.type_id = feature_type.cvterm_id
;

create view gene as
select feature.feature_id
     , feature.uniquename
     , feature.name
     , organism.common_name as organism_common_name
     , feature.seqlen
     , featureloc.fmin
     , featureloc.fmax
     , colour_prop_type.name
from feature
join cvterm gene_type on feature.type_id = gene_type.cvterm_id
join organism using   (organism_id)
join featureloc using (feature_id)
right outer join featureprop colour_prop using (feature_id)
join cvterm colour_prop_type on colour_prop.type_id = colour_prop_type.cvterm_id
where gene_type.name = 'gene'
  and (colour_prop_type.name is null or colour_prop_type.name = 'colour')
;

create or replace view exon as
select feature.feature_id
     , feature.uniquename
     , feature.name
     , cv.name as ontology
     , rel.name as relationship
     , feature_relationship.object_id
     , feature.seqlen
     , featureloc.fmin
     , featureloc.fmax
from feature, cvterm exon_type, featureloc
   , feature_relationship
   , cvterm rel, cv, cvterm
where feature.type_id = exon_type.cvterm_id
  and exon_type.name = 'exon'
  and featureloc.feature_id = feature.feature_id
  and feature_relationship.subject_id = feature.feature_id
  and feature_relationship.type_id = rel.cvterm_id
  and cv.cv_id = rel.cv_id
;