select db.name as db_name
     , gene.uniquename as gene_uniquename
     , dbxref.accession
from feature polypeptide
join feature_relationship polypeptide_transcript on polypeptide_transcript.subject_id = polypeptide.feature_id
join feature transcript on polypeptide_transcript.object_id = transcript.feature_id
join feature_relationship transcript_gene on transcript_gene.subject_id = transcript.feature_id
join feature gene on transcript_gene.object_id = gene.feature_id
join feature_dbxref on polypeptide.feature_id = feature_dbxref.feature_id
join dbxref on feature_dbxref.dbxref_id = dbxref.dbxref_id
join db using (db_id)
where polypeptide.type_id = 191 /*polypeptide*/
  and transcript.type_id in (
        321 /*mRNA*/
      , 604 /*pseudogenic_transcript*/
  )
  and gene.type_id in (
        792 /*gene*/
      , 423 /*pseudogene*/
  )
  and db.name in ('Pfam', 'InterPro', 'PMID')
;