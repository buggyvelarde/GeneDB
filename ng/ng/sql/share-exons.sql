/* Generate a table of shared exons */
create temporary table shared_exons as
select gene.feature_id as gene_feature_id
     , gene.uniquename as gene_uniquename
     , transcript1.uniquename as transcript1
     , exon1.feature_id as exon1_feature_id
     , exon1.uniquename as exon1_uniquename
     , transcript2.uniquename as transcript2
     , exon2.feature_id as exon2_feature_id
     , exon2.uniquename as exon2_uniquename
     , exon1_loc.fmin /* = exon2_loc.fmin */
     , exon1_loc.fmax /* = exon2_loc.fmax */
from feature gene
join cvterm gene_type on gene.type_id = gene_type.cvterm_id
join cv gene_type_cv using (cv_id)
join feature_relationship gene_transcript1 on gene.feature_id = gene_transcript1.object_id
join feature transcript1 on gene_transcript1.subject_id = transcript1.feature_id
join cvterm transcript1_type on transcript1.type_id = transcript1_type.cvterm_id
join cv transcript1_type_cv on transcript1_type.cv_id = transcript1_type_cv.cv_id
join feature_relationship transcript1_exon1 on transcript1_exon1.object_id = transcript1.feature_id
join feature exon1 on transcript1_exon1.subject_id = exon1.feature_id
join cvterm exon1_type on exon1.type_id = exon1_type.cvterm_id
join cv exon1_type_cv on exon1_type.cv_id = exon1_type_cv.cv_id
join featureloc exon1_loc on exon1_loc.feature_id = exon1.feature_id
join feature_relationship gene_transcript2 on gene.feature_id = gene_transcript2.object_id
join feature transcript2 on gene_transcript2.subject_id = transcript2.feature_id
join cvterm transcript2_type on transcript2.type_id = transcript2_type.cvterm_id
join cv transcript2_type_cv on transcript2_type.cv_id = transcript2_type_cv.cv_id
join feature_relationship transcript2_exon2 on transcript2_exon2.object_id = transcript2.feature_id
join feature exon2 on transcript2_exon2.subject_id = exon2.feature_id
join cvterm exon2_type on exon2.type_id = exon2_type.cvterm_id
join cv exon2_type_cv on exon2_type.cv_id = exon2_type_cv.cv_id
join featureloc exon2_loc on exon2_loc.feature_id = exon2.feature_id
where gene_type_cv.name = 'sequence'
and   gene_type.name = 'gene'
and   transcript1_type_cv.name = 'sequence'
and   transcript1_type.name = 'mRNA'
and   transcript2_type_cv.name = 'sequence'
and   transcript2_type.name = 'mRNA'
and   exon1_type_cv.name = 'sequence'
and   exon1_type.name = 'exon'
and   exon2_type_cv.name = 'sequence'
and   exon2_type.name = 'exon'
and   exon1.feature_id < exon2.feature_id
and   exon1_loc.rank = 0
and   exon2_loc.rank = 0
and   exon1_loc.fmin = exon2_loc.fmin
and   exon1_loc.fmax = exon2_loc.fmax
;

/* Choose one of the shared exons to be the canonical representative.
   We pick the one with the smallest feature_id.
 */
create temporary table canonical_exon_representatives as
select gene_feature_id, fmin, fmax, min(exon1_feature_id) as canonical_feature_id
from shared_exons
group by gene_feature_id, fmin, fmax
;

create temporary table exon_replacements as
select distinct shared_exons.exon2_feature_id as actual_feature_id
              , canonical_exon_representatives.canonical_feature_id
from shared_exons
join canonical_exon_representatives using (gene_feature_id, fmin, fmax)
where shared_exons.exon2_feature_id <> canonical_exon_representatives.canonical_feature_id
;

update feature_relationship set
    subject_id = (
        select canonical_feature_id
        from exon_replacements
        where feature_relationship.subject_id = exon_replacements.actual_feature_id)
where subject_id in (
    select actual_feature_id from exon_replacements
);

update feature_relationship set
    object_id = (
        select canonical_feature_id
        from exon_replacements
        where feature_relationship.object_id = exon_replacements.actual_feature_id)
where object_id in (
    select actual_feature_id from exon_replacements
);

delete from feature
where feature_id in (
    select actual_feature_id from exon_replacements
);

