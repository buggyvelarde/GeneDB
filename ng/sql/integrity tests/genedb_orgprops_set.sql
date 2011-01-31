#This test checks if each organism that is to be published on GeneDB has the relevant properties set
#in the phylonodeprop table. There are two essential properties: app_www_homePage_content and app_www_homePage_links.
#This test lists organisms that do not have one or both of these properties.

select distinct organism.common_name as organism,
       organism.organism_id,
       phylonode.phylonode_id
from organism
join phylonode on phylonode.label=organism.common_name
join phylonodeprop on phylonode.phylonode_id = phylonodeprop.phylonode_id
where 2 != (select count(phylonodeprop_id) from phylonodeprop p
                  where p.type_id in (select cvterm_id
                                      from cvterm
                                      where name in ('app_www_homePage_content','app_www_homePage_links'))
                  and p.phylonode_id=phylonode.phylonode_id
                  and p.value != '')
and exists (select * from organismprop
            where type_id = (select cvterm_id
                             from cvterm
                             where name='genedb_public')
            and organism_id=organism.organism_id
            and value='yes');



