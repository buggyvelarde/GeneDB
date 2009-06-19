delete from organismProp
using organism o, cvterm cvt, cv
where o.organism_id = organismProp.organism_id
and organismProp.type_id = cvt.cvterm_id
and cvt.cv_id = cv.cv_id
and cvt.name = 'populated'
and cv.name = 'genedb_misc'
and o.organism_id not in
(select organism_id from feature);