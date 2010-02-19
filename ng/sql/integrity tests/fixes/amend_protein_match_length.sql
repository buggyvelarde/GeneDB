-- This SQL adjusts the length of the polypeptide membrane structures. Previously, proteins were made to include the stop codon
-- which meant that the length was always 1 more than it ought to be. This has had an effect on the length of the membrane
-- structures, causing them to be one codon longer than necessary. This SQL adjusts the fmax, seqlen and the uniquename. 
-- 29.1.2010: Clarify if residues should contain the stop codon?? 

begin;

--Gets all the membrane structures that are short as a result of this protein stop codon 
create temporary table short_membranes as 
select super.feature_id as membrane_id
      ,super.uniquename as membrane_name
      ,protein.feature_id as protein_id
      ,protein.seqlen as protein_length
      ,superloc.featureloc_id 
from feature super
join featureloc superloc using (feature_id)
join organism using (organism_id)
join cvterm supertype on supertype.cvterm_id = super.type_id
join feature_relationship sub_super
    on sub_super.object_id = super.feature_id
join cvterm reltype on sub_super.type_id = reltype.cvterm_id
join feature sub on sub_super.subject_id = sub.feature_id
join featureloc subloc on subloc.feature_id = sub.feature_id
join feature protein on protein.feature_id=(select srcfeature_id 
                                            from featureloc 
                                            where featureloc.feature_id=super.feature_id)
where reltype.name = 'part_of'
and superloc.locgroup = subloc.locgroup
and superloc.srcfeature_id = subloc.srcfeature_id
and super.type_id= (select cvterm_id
                    from cvterm
                    where name='membrane_structure')
and substr(protein.residues, length(protein.residues)) in ('*', '+', '#')    
and protein.seqlen=length(protein.residues)
group by super.feature_id, super.uniquename, superloc.featureloc_id, protein.seqlen, superloc.fmax, protein.feature_id
having (superloc.fmax-1) = max(subloc.fmax)
order by super.feature_id;


-- Set all the above featurelocs to have an fmax that is one shorter than the protein length 
update featureloc 
set fmax = (select protein_length-1 
            from short_membranes 
            where featureloc.featureloc_id=short_membranes.featureloc_id)
where featureloc_id in (select featureloc_id 
                        from short_membranes);


-- Set the seqlen of the membrane to the right length
update feature
set seqlen = (select (protein_length-1)
              from short_membranes
              where membrane_id=feature.feature_id)
where feature_id in (select membrane_id 
                     from short_membranes);


-- Change the uniquename of the membrane
update feature
set uniquename = (select substr(membrane_name,0, position('0-' in membrane_name)) || '0-' || (protein_length-1) 
                  from short_membranes 
                  where short_membranes.membrane_id=feature.feature_id)
where feature_id in (select membrane_id
                     from short_membranes);
                     
   

-- Remove stop codon from protein
update feature
set residues = substr(residues,0,length(residues))
where feature_id in (select protein_id 
                     from short_membranes);
                     
-- Set length of protein to be one shorter than before
update feature
set seqlen = seqlen-1
where feature_id in (select protein_id
                     from short_membranes);


                     
commit;















