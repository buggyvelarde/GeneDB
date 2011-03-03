DELETE FROM organism WHERE organism_id IN (
    SELECT distinct(organism_id) 
        FROM organismprop 
        JOIN cvterm ON organismprop.type_id = cvterm.cvterm_id AND cvterm.name IN ('webservices_public', 'genedb_public') 
        WHERE value = 'no'
)

