begin;

insert into feature_relationship (
    subject_id, object_id, type_id
) (
    select transcript.feature_id, gene.feature_id, 42 /*part_of*/
    from feature gene
    join feature transcript using (uniquename)
    where gene.type_id = 792 /*gene*/
    and transcript.type_id in (
          321 /*mRNA*/
        , 339 /*rRNA*/
        , 340 /*tRNA*/
        , 361 /*snRNA*/
    )
    and not exists (
        select 8
        from feature_relationship transcript_gene
        where transcript_gene.subject_id = transcript.feature_id
        and   transcript_gene.object_id  = gene.feature_id
    )
)
;

/* Assuming no splicing of noncoding transcripts */

update feature
set uniquename = uniquename || ':rRNA'
where type_id = 339 /*rRNA*/
and uniquename not like '%:%'
;

update feature
set uniquename = uniquename || ':tRNA'
where type_id = 340 /*tRNA*/
and uniquename not like '%:%'
;

update feature
set uniquename = uniquename || ':snRNA'
where type_id = 361 /*snRNA*/
and uniquename not like '%:%'
;

/* Check it looks okay before you commit! */

commit;
