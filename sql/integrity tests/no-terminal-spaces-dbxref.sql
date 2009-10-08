#There should be no spaces at the end of dbxref accession numbers
select * from dbxref where accession like '% ';
