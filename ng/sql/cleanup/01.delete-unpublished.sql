DELETE FROM organism WHERE organism_id IN (
    SELECT distinct(organism.organism_id) 
        FROM 
            organism, 
            organismprop webservices_public_prop, 
            organismprop genedb_public_prop, 
            cvterm webservices_public_cvterm, 
            cvterm genedb_public_cvterm 
        WHERE
            webservices_public_prop.organism_id = organism.organism_id
        AND
            genedb_public_prop.organism_id = organism.organism_id
        AND
            webservices_public_prop.type_id = webservices_public_cvterm.cvterm_id 
                AND webservices_public_cvterm.name = 'webservices_public' 
                AND webservices_public_prop.value = 'no'
        AND 
            genedb_public_prop.type_id = genedb_public_cvterm.cvterm_id 
                AND genedb_public_cvterm.name = 'genedb_public' 
                AND genedb_public_prop.value = 'no'
        
);