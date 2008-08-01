create or replace view overlapping_exons as
select organism.common_name as organism_name
     , rna.uniquename as rna
     , exon1.uniquename as exon1, exon1_loc.fmin as ex1_min, exon1_loc.fmax as ex1_max
     , exon2.uniquename as exon2, exon2_loc.fmin as ex2_min, exon2_loc.fmax as ex2_max
from feature rna
join organism using (organism_id)
join cvterm rna_type on rna_type.cvterm_id = rna.type_id
join cv using (cv_id)
join feature_relationship rna_exon1 on rna_exon1.object_id = rna.feature_id
join feature exon1 on rna_exon1.subject_id = exon1.feature_id
join cvterm exon1_type on exon1.type_id = exon1_type.cvterm_id
join featureloc exon1_loc on exon1_loc.feature_id = exon1.feature_id
join feature_relationship rna_exon2 on rna_exon2.object_id = rna.feature_id
join feature exon2 on rna_exon2.subject_id = exon2.feature_id
join cvterm exon2_type on exon2.type_id = exon2_type.cvterm_id
join featureloc exon2_loc on exon2_loc.feature_id = exon2.feature_id
where cv.name = 'sequence'
  and rna_type.name = 'mRNA'
  and exon1_type.name = 'exon'
  and exon2_type.name = 'exon'
  -- and exon1.uniquename not like '%:5UTR'
  -- and exon2.uniquename not like '%:5UTR'
  and exon2_type.name = 'exon'
  and exon1.feature_id < exon2.feature_id
  and exon1_loc.fmin < exon2_loc.fmax
  and exon2_loc.fmin < exon1_loc.fmax
;
