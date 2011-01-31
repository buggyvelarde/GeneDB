-- This SQL makes the Bodo saltan genes, exons and transcripts the same length as the peptides. The mismatch in length was picked up by the integrity tests
-- After talking to andrew, it was concluded that the peptide length should be the right length as this is what he has worked on.
-- We need to keep an eye on this test as this situation really shouldn't arise if Artemis is used properly.

-- nds 6.9.2010

begin;

-- Get all the Bodo genes that are either shorter or longer than their peptide
-- Is there a way to do this in one query? (i.e., all the genes, transcripts and exons)

create temporary table short_features as
select  gene.uniquename as feature
     ,  gene.feature_id as feature_id
     ,  peptideloc.fmin as fmin
     ,  peptideloc.fmax as fmax
     ,  geneloc.featureloc_id as flocid
     ,  geneloc.fmin as genefmin
     ,  geneloc.fmax as genefmax
from feature gene
join feature_relationship transcript_gene on gene.feature_id=transcript_gene.object_id
join feature transcript on transcript.feature_id=transcript_gene.subject_id 
join feature_relationship peptide_transcript on peptide_transcript.object_id=transcript.feature_id
join feature peptide on peptide_transcript.subject_id = peptide.feature_id 
join featureloc peptideloc on peptideloc.feature_id = peptide.feature_id
join featureloc geneloc on geneloc.feature_id=gene.feature_id
where transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and gene.type_id in (select cvterm_id
                    from cvterm
                    where name in ('gene', 'pseudogene'))
and peptide.type_id in (select cvterm_id
                        from cvterm join cv using (cv_id)
                        where cvterm.name='polypeptide' and cv.name='sequence')
and geneloc.srcfeature_id = peptideloc.srcfeature_id
and geneloc.strand = peptideloc.strand
and peptideloc.fmin <> geneloc.fmin
and peptideloc.fmax <> geneloc.fmax
and gene.organism_id = (select organism_id 
                        from organism
                        where common_name='Bsaltans')
order by feature;

-- Run SQL to update lengths (see below)

-- Get all the Bodo transcripts that are not the same length as the peptide
create temporary table short_features as
select  transcript.uniquename as feature
     ,  transcript.feature_id as feature_id
     ,  peptideloc.fmin as fmin
     ,  peptideloc.fmax as fmax
     ,  transcriptloc.featureloc_id as flocid
     ,  transcriptloc.fmin as trfmin
     ,  transcriptloc.fmax as trfmax
from feature transcript
join feature_relationship peptide_transcript on peptide_transcript.object_id=transcript.feature_id
join feature peptide on peptide_transcript.subject_id = peptide.feature_id 
join featureloc peptideloc on peptideloc.feature_id = peptide.feature_id
join featureloc transcriptloc on transcriptloc.feature_id=transcript.feature_id
where transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and peptide.type_id in (select cvterm_id
                        from cvterm join cv using (cv_id)
                        where cvterm.name='polypeptide' and cv.name='sequence')
and transcriptloc.srcfeature_id = peptideloc.srcfeature_id
and transcriptloc.strand = peptideloc.strand
and peptideloc.fmin <> transcriptloc.fmin
and peptideloc.fmax <> transcriptloc.fmax
and transcript.organism_id = (select organism_id 
                        from organism
                        where common_name='Bsaltans')
order by feature;


-- Check if all transcripts just have one exon?

select transcript.uniquename,
       transcript.feature_id,
       count(exon.feature_id) 
from feature transcript
join feature_relationship exon_transcript on exon_transcript.object_id = transcript.feature_id
join feature exon on exon.feature_id = exon_transcript.subject_id
where transcript.organism_id = 210
and transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and exon.type_id = 234
group by transcript.uniquename, transcript.feature_id
having count(exon.feature_id) <>1;

-- Get all the Bodo exons that are not the right length
create temporary table short_features as
select  exon.uniquename as feature
     ,  exon.feature_id as feature_id
     ,  peptideloc.fmin as fmin
     ,  peptideloc.fmax as fmax
     ,  exonloc.featureloc_id as flocid
     ,  exonloc.fmin as exonfmin
     ,  exonloc.fmax as exonfmax
from feature exon
join feature_relationship exon_transcript on exon_transcript.subject_id = exon.feature_id
join feature transcript on exon_transcript.object_id = transcript.feature_id
join feature_relationship peptide_transcript on peptide_transcript.object_id=transcript.feature_id
join feature peptide on peptide_transcript.subject_id = peptide.feature_id 
join featureloc peptideloc on peptideloc.feature_id = peptide.feature_id
join featureloc exonloc on exonloc.feature_id=exon.feature_id
where transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and peptide.type_id in (select cvterm_id
                        from cvterm join cv using (cv_id)
                        where cvterm.name='polypeptide' and cv.name='sequence')
and exon.type_id in (select cvterm_id from cvterm where name='exon')
and exonloc.srcfeature_id = peptideloc.srcfeature_id
and exonloc.strand = peptideloc.strand
and peptideloc.fmin <> exonloc.fmin
and peptideloc.fmax <> exonloc.fmax
and exon.organism_id = (select organism_id 
                        from organism
                        where common_name='Bsaltans')
and transcript.uniquename not in ( -- exclude transcripts with multiple exons
 'BSA01470.1', 
 'BSA01485.1',
 'BSA00741.1',
 'BSA00950.1',
 'BSA01123.1',
 'BSA00228.1',
 'BSA01345.1',
 'BSA00715.1',
 'BSA01146.1',
 'BSA01017.1')
 order by feature;


 
 select  exon.uniquename as feature
     ,  exon.feature_id as feature_id
     ,  peptideloc.fmin as fmin
     ,  peptideloc.fmax as fmax
     ,  exonloc.featureloc_id as flocid
     ,  exonloc.fmin as exonfmin
     ,  exonloc.fmax as exonfmax
from feature exon
join feature_relationship exon_transcript on exon_transcript.subject_id = exon.feature_id
join feature transcript on exon_transcript.object_id = transcript.feature_id
join feature_relationship peptide_transcript on peptide_transcript.object_id=transcript.feature_id
join feature peptide on peptide_transcript.subject_id = peptide.feature_id 
join featureloc peptideloc on peptideloc.feature_id = peptide.feature_id
join featureloc exonloc on exonloc.feature_id=exon.feature_id
where transcript.type_id in ( select cvterm_id 
                              from cvterm
                              where name in ('snRNA', 'tRNA', 'transcript', 'pseudogenic_transcript', 'rRNA', 'mRNA'))
and peptide.type_id in (select cvterm_id
                        from cvterm join cv using (cv_id)
                        where cvterm.name='polypeptide' and cv.name='sequence')
and exon.type_id in (select cvterm_id from cvterm where name='exon')
and exonloc.srcfeature_id = peptideloc.srcfeature_id
and exonloc.strand = peptideloc.strand
and peptideloc.fmin <> exonloc.fmin
and peptideloc.fmax <> exonloc.fmax
and exon.organism_id = (select organism_id 
                        from organism
                        where common_name='Bsaltans')
order by feature;




-- Repeat the update statements below for genes, transcripts and exons

-- Adjust the features  - fmin
	update featureloc 
	set fmin = (select fmin
	            from short_features
	            where short_features.flocid=featureloc_id)
	where featureloc_id in (select flocid
	                     from short_features);
                     
-- Adjust the features  - fmax
	update featureloc 
	set fmax = (select fmax
	            from short_features
	            where short_features.flocid=featureloc_id)
	where featureloc_id in (select flocid
	                     from short_features);
                     
