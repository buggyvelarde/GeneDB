/*
  Calculate the average length of those S. mansoni genes or pseudogenes
  that lie wholly on a single contig.
*/

select avg(geneloc_contig.fmax - geneloc_contig.fmin) as "Mean length of (pseudo)genes that lie on a single contig"
from feature gene
join featureloc geneloc_scaff on geneloc_scaff.feature_id = gene.feature_id
join featureloc geneloc_contig on geneloc_contig.feature_id = gene.feature_id
join feature scaff on geneloc_scaff.srcfeature_id = scaff.feature_id
join feature contig on geneloc_contig.srcfeature_id = contig.feature_id
where gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and gene.organism_id = 13 /* S. mansoni */
and geneloc_scaff.rank = 0
and geneloc_scaff.locgroup = 0
and geneloc_contig.rank = 1
and geneloc_contig.locgroup = 1
and geneloc_contig.fmax - geneloc_contig.fmin = geneloc_scaff.fmax - geneloc_scaff.fmin
;


/*
  Calculate the mean number of exons per transcript in S. mansoni.
*/
select avg(exon_count) as "Mean number of exons per transcript"
from (
        select transcript.uniquename
             , count(exon) as exon_count
        from feature transcript
        join feature_relationship exon_transcript on exon_transcript.object_id = transcript.feature_id
        join feature exon on exon_transcript.subject_id = exon.feature_id
        where transcript.organism_id = 13 /* S. mansoni */
        and transcript.type_id in (
                321 /*mRNA*/
              , 339 /*rRNA*/
              , 340 /*tRNA*/
              , 361 /*snRNA*/
              , 743 /*ncRNA*/
              , 604 /*pseudogenic_transcript*/
        )
        and exon.type_id in (
                234 /*exon*/
              , 595 /*pseudogenic_exon*/
        )
        group by transcript.uniquename
) exon_counts
;


/*
  Count the S. mansoni genes that don't lie wholly on a contig.
 */
select count(*) as "Number of genes that cross a gap between contigs"
from feature gene
where gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and gene.organism_id = 13 /* S. mansoni */
and not exists (
        select * from featureloc
        where featureloc.feature_id = gene.feature_id
        and featureloc.rank = 1
        and featureloc.locgroup = 1
)
;



/*
  Count the S. mansoni genes that do lie wholly on a contig
 */
select count(*) as "Number of genes that lie wholly on a contig"
from feature gene
where gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and gene.organism_id = 13 /* S. mansoni */
and exists (
        select * from featureloc
        where featureloc.feature_id = gene.feature_id
        and featureloc.rank = 1
        and featureloc.locgroup = 1
)
;

/*
  Calculate the average length of all S. mansoni genes or pseudogenes.
*/
select avg(geneloc_scaff.fmax - geneloc_scaff.fmin) as "Mean length of all (pseudo)genes"
from feature gene
join featureloc geneloc_scaff on geneloc_scaff.feature_id = gene.feature_id
join feature scaff on geneloc_scaff.srcfeature_id = scaff.feature_id
where gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
)
and gene.organism_id = 13 /* S. mansoni */
and geneloc_scaff.rank = 0
and geneloc_scaff.locgroup = 0
;
