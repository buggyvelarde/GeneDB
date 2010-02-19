begin;

create temporary table riley_terms (
    cv_id     integer,
    db_id     integer,
    cvterm_id integer default nextval('cvterm_cvterm_id_seq'::regclass),
    dbxref_id integer default nextval('dbxref_dbxref_id_seq'::regclass),
    name character varying(255) not null,
    description text not null
);

insert into riley_terms (name, description) values
    ('0.0.0', 'Unknown function, no known homologs'),
    ('0.0.1', 'Conserved in Escherichia coli'),
    ('0.0.2', 'Conserved in other organisms'),
    ('1.0.0', 'Cell processes'),
    ('1.1.1', 'Chemotaxis and mobility'),
    ('1.2.1', 'Chromosome replication'),
    ('1.3.1', 'Chaperones'),
    ('1.4.0', 'Protection responses'),
    ('1.4.1', 'Cell killing'),
    ('1.4.2', 'Detoxification'),
    ('1.4.3', 'Drug/analog sensitivity'),
    ('1.4.4', 'Radiation sensitivity'),
    ('1.5.0', 'Transport/binding proteins'),
    ('1.5.1', 'Amino acids and amines'),
    ('1.5.2', 'Cations'),
    ('1.5.3', 'Carbohydrates, organic acids and alcohols'),
    ('1.5.4', 'Anions'),
    ('1.5.5', 'Other Transport/binding proteins'),
    ('1.6.0', 'Adaptation'),
    ('1.6.1', 'Adaptations, atypical conditions'),
    ('1.6.2', 'Osmotic adaptation'),
    ('1.6.3', 'Fe storage'),
    ('1.7.1', 'Cell division'),
    ('1.8.1', 'Sporulation, differentiation and germination'),
    ('2.0.0', 'Macromolecule metabolism'),
    ('2.1.0', 'Macromolecule degradation'),
    ('2.1.1', 'Degradation of DNA'),
    ('2.1.2', 'Degradation of RNA'),
    ('2.1.3', 'Degradation of polysaccharides'),
    ('2.1.4', 'Degradation of proteins, peptides, glyco'),
    ('2.2.0', 'Macromolecule synthesis, modification'),
    ('2.2.1', 'Amino acyl tRNA synthesis; tRNA modification'),
    ('2.2.2', 'Basic proteins - synthesis, modification'),
    ('2.2.3', 'DNA - replication, repair, restriction/modification'),
    ('2.2.4', 'Glycoprotein'),
    ('2.2.5', 'Lipopolysaccharide'),
    ('2.2.6', 'Lipoprotein'),
    ('2.2.7', 'Phospholipids'),
    ('2.2.8', 'Polysaccharides - (cytoplasmic)'),
    ('2.2.9', 'Protein modification'),
    ('2.2.10', 'Proteins - translation and modification'),
    ('2.2.11', 'RNA synthesis, modification, DNA transcript''n'),
    ('2.2.12', 'tRNA'),
    ('3.0.0', 'Metabolism of small molecules'),
    ('3.1.0', 'Amino acid biosynthesis'),
    ('3.1.1', 'Alanine'),
    ('3.1.2', 'Arginine'),
    ('3.1.3', 'Asparagine'),
    ('3.1.4', 'Aspartate'),
    ('3.1.5', 'Chorismate'),
    ('3.1.6', 'Cysteine'),
    ('3.1.7', 'Glutamate'),
    ('3.1.8', 'Glutamine'),
    ('3.1.9', 'Glycine'),
    ('3.1.10', 'Histidine'),
    ('3.1.11', 'Isoleucine'),
    ('3.1.12', 'Leucine'),
    ('3.1.13', 'Lysine'),
    ('3.1.14', 'Methionine'),
    ('3.1.15', 'Phenylalanine'),
    ('3.1.16', 'Proline'),
    ('3.1.17', 'Serine'),
    ('3.1.18', 'Threonine'),
    ('3.1.19', 'Tryptophan'),
    ('3.1.20', 'Tyrosine'),
    ('3.1.21', 'Valine'),
    ('3.2.0', 'Biosynthesis of cofactors, carriers'),
    ('3.2.1', 'Acyl carrier protein (ACP)'),
    ('3.2.2', 'Biotin'),
    ('3.2.3', 'Cobalamin'),
    ('3.2.4', 'Enterochelin'),
    ('3.2.5', 'Folic acid'),
    ('3.2.6', 'Heme, porphyrin'),
    ('3.2.7', 'Lipoate'),
    ('3.2.8', 'Menaquinone, ubiquinone'),
    ('3.2.9', 'Molybdopterin'),
    ('3.2.10', 'Pantothenate'),
    ('3.2.11', 'Pyridine nucleotide'),
    ('3.2.12', 'Pyridoxine'),
    ('3.2.13', 'Riboflavin'),
    ('3.2.14', 'Thiamin'),
    ('3.2.15', 'Thioredoxin, glutaredoxin, glutathione'),
    ('3.2.16', 'biotin carboxyl carrier protein (BCCP)'),
    ('3.2.17', 'Ferredoxin'),
    ('3.3.0', 'Central intermediary metabolism'),
    ('3.3.1', '2''-Deoxyribonucleotide metabolism'),
    ('3.3.2', 'Amino sugars'),
    ('3.3.3', 'Entner-Douderoff'),
    ('3.3.4', 'Gluconeogenesis'),
    ('3.3.5', 'Glyoxylate bypass'),
    ('3.3.6', 'Incorporation metal ions'),
    ('3.3.7', 'Misc. glucose metabolism'),
    ('3.3.8', 'Misc. glycerol metabolism'),
    ('3.3.9', 'Non-oxidative branch, pentose pwy'),
    ('3.3.10', 'Nucleotide hydrolysis'),
    ('3.3.11', 'Nucleotide interconversions'),
    ('3.3.12', 'Oligosaccharides'),
    ('3.3.13', 'Phosphorus compounds'),
    ('3.3.14', 'Polyamine biosynthesis'),
    ('3.3.15', 'Pool, multipurpose conversions of intermed. met''m'),
    ('3.3.16', 'S-adenosyl methionine'),
    ('3.3.17', 'Salvage of nucleosides and nucleotides'),
    ('3.3.18', 'Sugar-nucleotide biosynthesis, conversions'),
    ('3.3.19', 'Sulfur metabolism'),
    ('3.3.20', 'amino acids'),
    ('3.3.21', 'other'),
    ('3.4.0', 'Degradation of small molecules'),
    ('3.4.1', 'Amines'),
    ('3.4.2', 'Amino acids'),
    ('3.4.3', 'Carbon compounds'),
    ('3.4.4', 'Fatty acids'),
    ('3.4.5', 'Other Degradation of small molecules'),
    ('3.5.0', 'Energy metabolism, carbon'),
    ('3.5.1', 'Aerobic respiration'),
    ('3.5.2', 'Anaerobic respiration'),
    ('3.5.3', 'Electron transport'),
    ('3.5.4', 'Fermentation'),
    ('3.5.5', 'Glycolysis'),
    ('3.5.6', 'Oxidative branch, pentose pwy'),
    ('3.5.7', 'Pyruvate dehydrogenase'),
    ('3.5.8', 'TCA cycle'),
    ('3.5.9', 'ATP-proton motive force'),
    ('3.6.0', 'Fatty acid biosynthesis'),
    ('3.6.1', 'Fatty acid and phosphatidic acid biosynth'),
    ('3.7.0', 'Nucleotide biosynthesis'),
    ('3.7.1', 'Purine ribonucleotide biosynthesis'),
    ('3.7.2', 'Pyrimidine ribonucleotide biosynthesis'),
    ('3.8.0', 'related to secondary metabolism'),
    ('3.8.1', 'polylketide synthases (PKSs)'),
    ('3.8.2', 'non-ribosomal peptide synthases (NRPSs)'),
    ('4.0.0', 'Cell envelop'),
    ('4.1.0', 'Periplasmic/exported/lipoproteins'),
    ('4.1.1', 'Inner membrane'),
    ('4.1.2', 'Murein sacculus, peptidoglycan'),
    ('4.1.3', 'Outer membrane constituents'),
    ('4.1.4', 'Surface polysaccharides & antigens'),
    ('4.1.5', 'Surface structures'),
    ('4.1.6', 'G+ membrane'),
    ('4.1.7', 'G+ exported/lipoprotein'),
    ('4.1.8', 'G+ surface anchored'),
    ('4.1.9', 'G+ peptidoglycan, teichoic acid'),
    ('4.2.0', 'Ribosome constituents'),
    ('4.2.1', 'Ribosomal and stable RNAs'),
    ('4.2.2', 'Ribosomal proteins - synthesis, modification'),
    ('4.2.3', 'Ribosomes - maturation and modification'),
    ('5.0.0', 'Extrachromosomal'),
    ('5.1.0', 'Laterally acquirred elements'),
    ('5.1.1', 'Colicin-related functions'),
    ('5.1.2', 'Phage-related functions and prophages'),
    ('5.1.3', 'Plasmid-related functions'),
    ('5.1.4', 'Transposon-related functions'),
    ('5.1.5', 'Pathogenicity Islands/determinants'),
    ('6.0.0', 'Global functions'),
    ('6.1.1', 'Global regulatory functions'),
    ('6.1.2', 'Response regulator'),
    ('6.1.3', 'two-component fusion'),
    ('6.2.0', 'RNA polymerase core enzyme binding'),
    ('6.2.1', 'sigma factor'),
    ('6.2.2', 'anti sigma factor'),
    ('6.2.3', 'anti sigma factor antagonist'),
    ('6.3.1', 'AsnC'),
    ('6.3.2', 'AraC'),
    ('6.3.3', 'GntR'),
    ('6.3.4', 'IclR'),
    ('6.3.5', 'LacI'),
    ('6.3.6', 'LysR'),
    ('6.3.7', 'MarR'),
    ('6.3.8', 'TetR'),
    ('6.3.9', 'ROK'),
    ('6.3.10', 'DeoR'),
    ('6.3.11', 'LuxR (GerR)'),
    ('6.3.12', 'MerR'),
    ('6.3.13', 'ArsR'),
    ('6.3.14', 'PadR'),
    ('6.4.0', 'Protein kinases'),
    ('6.4.1', 'Serine/threonine'),
    ('6.5.0', 'Others'),
    ('6.6.0', 'LPS regulated regulatory functions'),
    ('7.0.0', 'Not classified (included putative assignments)'),
    ('7.1.1', 'DNA sites, no gene product'),
    ('7.2.1', 'Cryptic genes')
;

update riley_terms
    set cv_id = cv.cv_id
      , db_id = db.db_id
from cv, db
where cv.name = 'RILEY'
and db.name = 'RILEY'
;

insert into dbxref (dbxref_id, db_id, accession, description)
    (select dbxref_id, db_id, name, description from riley_terms);
insert into cvterm (cvterm_id, cv_id, dbxref_id, name, definition)
    (select cvterm_id, cv_id, dbxref_id, name, description from riley_terms);

commit;


/* Correct spelling */
update dbxref
set description = 'Cell envelope'
where description = 'Cell envelop';


/* Add additional Riley terms */
begin;

create temporary table riley_terms (
    cv_id     integer,
    db_id     integer,
    cvterm_id integer default nextval('cvterm_cvterm_id_seq'::regclass),
    dbxref_id integer default nextval('dbxref_dbxref_id_seq'::regclass),
    name character varying(255) not null,
    description text not null
);

insert into riley_terms (name, description) values
    ('1.4.5', 'DNA-binding'),
    ('3.4.6', 'ATP-proton motive force'),
    ('6.4.2', 'Tyrosine')
;

update riley_terms
    set cv_id = cv.cv_id
      , db_id = db.db_id
from cv, db
where cv.name = 'RILEY'
and db.name = 'RILEY'
;

insert into dbxref (dbxref_id, db_id, accession, description)
    (select dbxref_id, db_id, name, description from riley_terms);
insert into cvterm (cvterm_id, cv_id, dbxref_id, name, definition)
    (select cvterm_id, cv_id, dbxref_id, name, description from riley_terms);

commit;


/* Round two: correct another typo and remove 3.4.6 (which is duplicated by 3.5.9) */

update dbxref
set description = 'Laterally acquired elements'
where accession = '5.1.0' and description = 'Laterally acquirred elements';

delete from cvterm where name = '3.4.6' and definition = 'ATP-proton motive force';
delete from dbxref where accession = '3.4.6' and description = 'ATP-proton motive force';


/* Round three: roll in corrections and additions from Matt Holden (5 December 2008) */

begin;

update dbxref
set description = 'polyketide synthases (PKSs)'
where accession = '3.8.1' and description = 'polylketide synthases (PKSs)';

create temporary table riley_terms (
    cv_id     integer,
    db_id     integer,
    cvterm_id integer default nextval('cvterm_cvterm_id_seq'::regclass),
    dbxref_id integer default nextval('dbxref_dbxref_id_seq'::regclass),
    name character varying(255) not null,
    description text not null
);

insert into riley_terms (name, description) values
    ('1.6.4',  'Nodulation related'),
    ('3.2.18', 'Isoprenoid'),
    ('3.3.22', 'Nitrogen metabolism (urease)'),
    ('6.5.1',  'GGDEF/EAL domain regulatory protein')
;

update riley_terms
    set cv_id = cv.cv_id
      , db_id = db.db_id
from cv, db
where cv.name = 'RILEY'
and db.name = 'RILEY'
;

insert into dbxref (dbxref_id, db_id, accession, description)
    (select dbxref_id, db_id, name, description from riley_terms);
insert into cvterm (cvterm_id, cv_id, dbxref_id, name, definition)
    (select cvterm_id, cv_id, dbxref_id, name, description from riley_terms);

commit;
