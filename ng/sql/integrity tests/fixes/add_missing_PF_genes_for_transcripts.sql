begin;

create temporary table pf_transcripts as
select organism.common_name,
        organism.organism_id as organism_id,
       feature_id as transcript_id, 
       transcript.type_id as type_id,
       uniquename,
       featureloc.fmin as fmin,
       featureloc.fmax as fmax,
       featureloc.strand as strand,
       featureloc.phase as phase,
       featureloc.srcfeature_id as srcfeature_id
from feature transcript
join organism on transcript.organism_id=organism.organism_id
join featureloc using (feature_id)
where transcript.type_id in (
    select cvterm.cvterm_id
    from cvterm join cv on cvterm.cv_id = cv.cv_id
    where cv.name = 'sequence'
    and cvterm.name='transcript'
)
and not exists (
    select *
    from feature_relationship transcript_gene
    join feature gene on transcript_gene.object_id = gene.feature_id
    where transcript_gene.subject_id = transcript.feature_id
    and gene.type_id = (
                          select cvterm.cvterm_id
                          from cvterm join cv on cvterm.cv_id = cv.cv_id
                          where cv.name = 'sequence'
                          and cvterm.name='gene')

)
and transcript.organism_id=27
and uniquename not in ('PF01TR002.2', 'PF01TR003.2','PF01TR004.2', 'PF02TR003.2', 'PF04TR001.2', 'PF09TR005.2' )
order by organism.common_name
;

/* Add the genes, featurelocs, featurerelationships */

select count(*) from pf_transcripts;

insert into feature (organism_id, uniquename, type_id, is_analysis) 
select organism_id, uniquename, '792', 't' from pf_transcripts;

insert into featureloc (feature_id, fmin, fmax, strand, phase)
select feature.feature_id, pf_transcripts.fmin, pf_transcripts.fmax, pf_transcripts.strand, pf_transcripts.phase
from feature
join pf_transcripts on feature.uniquename=pf_transcripts.uniquename
where feature.type_id=792;

insert into feature_relationship (subject_id, object_id, type_id)
select pf_transcripts.transcript_id, feature.feature_id, '42' from pf_transcripts, feature
where feature.type_id=792 and feature.uniquename=pf_transcripts.uniquename;

/* change the name of the transcripts to have :transcript at the end */

update feature set uniquename=pf_transcripts.uniquename || ':transcript'
from pf_transcripts
where feature.feature_id=pf_transcripts.transcript_id;

/* correction to add the srcfeatures I forgot to add! */
create temporary table flocs as
select audit.featureloc.featureloc_id as flocid, 
       gene.feature_id as geneid, 
       t_floc.srcfeature_id as srcfeatureid 
from audit.featureloc
join feature as gene using (feature_id) 
join feature as transcript on transcript.uniquename = gene.uniquename || ':transcript' 
join featureloc as t_floc on t_floc.feature_id = transcript.feature_id 
where audit.featureloc.username like 'nds%' and audit.featureloc.time > '2009-11-24';


update featureloc set srcfeature_id=flocs.srcfeatureid
from flocs
where featureloc.featureloc_id=flocs.flocid;

