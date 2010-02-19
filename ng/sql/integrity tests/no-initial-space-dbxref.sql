#There should not be spaces at the start of dbxref accession numbers

select * from dbxref where accession like ' %';
