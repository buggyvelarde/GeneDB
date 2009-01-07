\t
\a
select cvterm.name
from cvterm
join cv on cvterm.cv_id = cv.cv_id
where cv.name = 'genedb_products';