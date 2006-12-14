-- Clear organism table
delete from organism where common_name = 'human';
delete from organism where common_name = 'fruitfly';
delete from organism where common_name = 'mouse';
delete from organism where common_name = 'mosquito';
delete from organism where common_name = 'rat';
delete from organism where common_name = 'mustard weed';
delete from organism where common_name = 'worm';
delete from organism where common_name = 'zebrafish';
delete from organism where common_name = 'rice';


-- Load GeneDB organisms and their properties

insert into organism (abbreviation, genus, species, common_name)
       values ('A.fumigatus', 'Aspergillus','fumigatus','afumigatus');

insert into organism (abbreviation, genus, species, common_name)
       values ('B.cenocepacia', 'Burkholderia','cenocepacia','bcenocepacia');

insert into organism (abbreviation, genus, species, common_name)
       values ('B.fragilis', 'Bacteroide','fragilis','bfragilis');

insert into organism (abbreviation, genus, species, common_name)
       values ('B.bronchiseptica', 'Bordetella','bronchiseptica','bbronchiseptica');
insert into organism (abbreviation, genus, species, common_name)
       values ('B.pertussis', 'Bordetella','pertussis','bpertussis');

insert into organism (abbreviation, genus, species, common_name)
       values ('B.pseudomallei', 'Burkholderia', 'pseudomallei','bpseudomallei');

insert into organism (abbreviation, genus, species, common_name)
       values ('C.abortus', 'Chlamydophilia','abortus','cabortus');

insert into organism (abbreviation, genus, species, common_name)
       values ('C.diptheriae', 'Corynesbacterium','diptheriae','cdiptheriae');

insert into organism (abbreviation, genus, species, common_name)
       values ('D.discoideum', 'Dictyostelium','discoideum','ddiscoideum');

insert into organism (abbreviation, genus, species, common_name)
       values ('E.tenella', 'Eimeria','tenella','etenella');

insert into organism (abbreviation, genus, species, common_name)
       values ('E.histolytica', 'Entamoeba','histolytica','ehistolytica');

insert into organism (abbreviation, genus, species, common_name)
       values ('E.cartaovora', 'Erwinai','cartaovora','ecartaovora');

insert into organism (abbreviation, genus, species, common_name)
       values ('G.morsitans', 'Glossina','morsitans','gmorsitans');

insert into organism (abbreviation, genus, species, common_name)
       values ('L.major', 'Leishmania','major','lmajor');
insert into organism (abbreviation, genus, species, common_name)
       values ('L.infantum', 'Leishmania','infantum','linfantum');
insert into organism (abbreviation, genus, species, common_name)
       values ('L.braziliensis', 'Leishmania','braziliensis','lbraziliensis');

insert into organism (abbreviation, genus, species, common_name)
       values ('P.falciparum', 'Plasmodium','falciparum','pfalciparum');
insert into organism (abbreviation, genus, species, common_name)
       values ('P.chabaudi', 'Plasmodium','chabaudi','pchabaudi');
insert into organism (abbreviation, genus, species, common_name)
       values ('P.knowlesi', 'Plasmodium','knowlesi','pknowlesi');
insert into organism (abbreviation, genus, species, common_name)
       values ('P.vivax', 'Plasmodium','vivax','pvivax');
insert into organism (abbreviation, genus, species, common_name)
       values ('P.berghei', 'Plasmodium','berghei','pberghei');

insert into organism (abbreviation, genus, species, common_name)
       values ('R.leguminosarum', 'Rhibosoma','leguminosarum','rleguminosarum');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.cerevisiae', 'Saccharomyces','cerevisiae','yeast');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.typhi', 'Salmonella','typhi','styphi');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.coelicolor', 'Streptomyces','coelicolor','scoelicolor');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.mansoni', 'Schistosoma','mansoni','smansoni');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.pombe', 'Schizosaccharomyces','pombe','spombe');

insert into organism (abbreviation, genus, species, common_name)
       values ('S.aureus', 'Staphylococcus','aureus','saureus');

insert into organism (abbreviation, genus, species, common_name)
       values ('T.annulata', 'Theileria','annulata','tannulata');

insert into organism (abbreviation, genus, species, common_name)
       values ('T.whipplei', 'Tropheryma','whipplei','twhipplei');

insert into organism (abbreviation, genus, species, common_name)
       values ('T.congolense', 'Trypanosoma','congolense','tcongolense');
insert into organism (abbreviation, genus, species, common_name)
       values ('T.cruzi', 'Trypanosoma','cruzi','tcruzi');
insert into organism (abbreviation, genus, species, common_name)
       values ('T.gambiense', 'Trypanosoma','gambiense','tgambiense');
insert into organism (abbreviation, genus, species, common_name)
       values ('T.vivax', 'Trypanosoma','vivax','tvivax');
insert into organism (abbreviation, genus, species, common_name)
       values ('T.brucei', 'Trypanosoma','brucei','tbrucei');

-- Unsorted
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');
--insert into organism (abbreviation, genus, species, common_name)
--       values ('', '','','');



-- Load phylogeny relationships
insert into phylotree (dbxref_id, name, type_id, comment)
		values ('', '', '' , '');


-- Clear dbs
delete from db where name like 'DB:%';

-- Add local dbs
-- Add GeneDB dbs

-- Add local cv and cvterms
