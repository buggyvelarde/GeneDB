insert into db (name, description, urlprefix) values (
        'RF',
        'The RefSeq database',
        'http://www.ncbi.nlm.nih.gov/sites/gquery?term=');

insert into db (name, description, urlprefix) values (
        'GB',
        'The GenBank database',
        'http://www.ncbi.nlm.nih.gov/sites/gquery?term=');

update db set urlprefix='http://www.expasy.org/cgi-bin/sprot-search-de?'
            , description='The SwissProt database'
    where name='SP';
