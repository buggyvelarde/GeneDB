/* Insert a reference to Arnab's paper on miscellaneous RNAs in P. falciparum. */

create temporary table arnabs_paper as
    select nextval('pub_pub_id_seq'::regclass) as pub_id
         , cvterm.cvterm_id as type_id
    from cvterm join cv using (cv_id)
    where cvterm.name = 'journal' and cv.name = 'genedb_literature'
;

insert into pub (
        pub_id, uniquename, type_id,
        title, volume, issue, pyear, pages, series_name
    ) (
        select pub_id, 'PMID:18096748', type_id
             , 'Genome-wide discovery and verification of novel structured RNAs in Plasmodium falciparum'
             , '18', '2', '2008', '281-92', 'Genome Research'
        from arnabs_paper
);

insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 0, 'Mourier', 'T' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 1, 'Carret', 'C' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 2, 'Kyes', 'S' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 3, 'Christodoulou', 'Z' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 4, 'Gardner', 'PP' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 5, 'Jeffares', 'DC' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 6, 'Pinches', 'R' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 7, 'Barrell', 'B' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 8, 'Berriman', 'M' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 9, 'Griffiths-Jones', 'S' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 10, 'Ivens', 'A' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 11, 'Newbold', 'C' from arnabs_paper);
insert into pubauthor (pub_id, rank, surname, givennames) (
    select pub_id, 12, 'Pain', 'A' from arnabs_paper);

insert into dbxref (db_id, accession) (
    select db_id, '18096748'
    from db
    where name = 'PMID');

insert into pub_dbxref (pub_id, dbxref_id) (
    select currval('pub_pub_id_seq'::regclass), currval('dbxref_dbxref_id_seq'::regclass));
