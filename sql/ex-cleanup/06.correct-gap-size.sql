begin;

update feature
    set uniquename = uniquename || ':corrected'
from featureloc
where featureloc.feature_id = feature.feature_id
and   feature.type_id = 818 /*gap*/
and   (featureloc.fmax - featureloc.fmin) % 10 = 9
;

update featureloc
    set fmin = fmin-1
from feature
where featureloc.feature_id = feature.feature_id
and   feature.type_id = 818 /*gap*/
and   (featureloc.fmax - featureloc.fmin) % 10 = 9
;

commit;
