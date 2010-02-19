create or replace view gene_rna_exon as
select gene.feature_id as gene_id
     , gene.uniquename as gene
     , rna.uniquename  as rna
     , rna_type.name   as rna_type
     , exon.feature_id as exon_id
     , exon.uniquename as exon
     , exon_loc.strand
     , exon_loc.fmin
     , exon_loc.fmax
from feature exon
join featureloc exon_loc using (feature_id)
join cvterm exon_type on exon.type_id = exon_type.cvterm_id
join feature_relationship exon_rna on exon_rna.subject_id = exon.feature_id
join feature rna on exon_rna.object_id = rna.feature_id
join cvterm rna_type on rna.type_id = rna_type.cvterm_id
join feature_relationship rna_gene on rna_gene.subject_id = rna.feature_id
join feature gene on rna_gene.object_id = gene.feature_id
join cvterm gene_type on gene.type_id = gene_type.cvterm_id
where gene_type.name = 'gene'
  and exon_type.name = 'exon'
;

-- Too slow:
--
-- select gre1.gene, gre2.gene
-- from gene_rna_exon gre1
-- join gene_rna_exon gre2 using (exon_id)
-- where gre1.gene_id < gre2.gene_id
-- limit 1
-- ;

-- Stab at more efficient query for overlaps:
-- we're looking for two different transcripts that share an exon
select exon.feature_id as exon_id
     , exon.uniquename as exon_name
     , rna1.feature_id as rna1_id
     , rna1.uniquename as rna1_name
     , rna2.feature_id as rna1_id
     , rna2.uniquename as rna1_name
from feature exon
join cvterm exon_type on exon.type_id = exon_type.cvterm_id
join feature_relationship exon_rna1 on exon_rna1.subject_id = exon.feature_id
join feature rna1 on exon_rna1.object_id = rna1.feature_id
join feature_relationship exon_rna2 on exon_rna2.subject_id = exon.feature_id
join feature rna2 on exon_rna2.object_id = rna2.feature_id
   , cvterm rna_type
where rna1.type_id = rna_type.cvterm_id
  and rna2.type_id = rna_type.cvterm_id
  and rna_type.name = 'mRNA'
  and exon_type.name = 'exon'
  and rna1.feature_id < rna2.feature_id
limit 1
;

-- Search for coextensive exons
create view coextensive_exons as
select f1.uniquename as exon1
     , f2.uniquename as exon2
     , fl1.fmin
     , fl1.fmax
     , fl1.strand
     , chr.uniquename  as chromosome
     , org.common_name as organism
from featureloc fl1
join featureloc fl2 using (srcfeature_id, fmin, fmax, strand)
join feature chr on chr.feature_id = fl1.srcfeature_id
join organism org using (organism_id)
join feature f1 on fl1.feature_id = f1.feature_id
join feature f2 on fl2.feature_id = f2.feature_id
join cvterm ft1 on f1.type_id = ft1.cvterm_id
join cvterm ft2 on f2.type_id = ft2.cvterm_id
where fl1.featureloc_id < fl2.featureloc_id
  and ft1.name = 'exon'
  and ft2.name = 'exon'
;

-- Find genes that have two different transcripts
select gene.feature_id as gene_id
     , gene.uniquename as gene
     , rna1.uniquename  as rna1
     , rna1_type.name   as rna1_type
     , rna2.uniquename  as rna2
     , rna2_type.name   as rna2_type
from feature gene
join cvterm gene_type on gene.type_id = gene_type.cvterm_id
join feature_relationship rna1_gene on rna1_gene.object_id = gene.feature_id
join feature_relationship rna2_gene on rna2_gene.object_id = gene.feature_id
join feature rna1 on rna1_gene.subject_id = rna1.feature_id
join cvterm rna1_type on rna1.type_id = rna1_type.cvterm_id
join feature rna2 on rna2_gene.subject_id = rna2.feature_id
join cvterm rna2_type on rna2.type_id = rna2_type.cvterm_id
where gene_type.name = 'gene'
  and rna1.feature_id < rna2.feature_id
;
