#This checks if all the genes have featurelocs. Can enhance this test later by checking that the gene is featurelocd on the right type
#of feature (contig, supercontig or chromosome)

select organism.common_name as organism
     , gene.feature_id
     , gene.uniquename
from feature gene
join organism on gene.organism_id=organism.organism_id
where type_id = (
                  select cvterm_id 
                  from cvterm join cv using (cv_id)
                  where cv.name ='sequence'
                  and cvterm.name='gene')

and not exists (
                  select * 
                  from featureloc
                  where feature_id=gene.feature_id)
order by organism.common_name;