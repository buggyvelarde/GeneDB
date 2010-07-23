select protein.uniquename
     , product1.name as product1
     , product2.name as product2
from feature protein
join feature_cvterm fc1 on protein.feature_id = fc1.feature_id
join cvterm product1 on fc1.cvterm_id = product1.cvterm_id
join feature_cvterm fc2 on protein.feature_id = fc2.feature_id
join cvterm product2 on fc2.cvterm_id = product2.cvterm_id
where protein.type_id = 191 /*polypeptide*/
and protein.organism_id = 27 /*Pfalciparum*/
and product1.cv_id = 25 /*genedb_products*/
and product2.cv_id = 25 /*genedb_products*/
and product1.cvterm_id < product2.cvterm_id
;
