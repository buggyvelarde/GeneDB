select organism.common_name as organism
     , chr.uniquename as chromosome
     , gap.uniquename as gap_uniquename
     , gaploc.fmin as gap_min
     , gaploc.fmax as gap_max
     , other.uniquename as other_uniquename
     , otherloc.fmin as other_min
     , otherloc.fmax as other_max
     , otherloc.strand as other_strand
from feature gap
join featureloc gaploc using (feature_id)
join feature chr on gaploc.srcfeature_id = chr.feature_id
join organism on chr.organism_id = organism.organism_id
join featureloc otherloc on otherloc.srcfeature_id = chr.feature_id
join feature other on other.feature_id = otherloc.feature_id
where gaploc.rank = 0
and   otherloc.rank = 0
and   gap.type_id = 818 /*gap*/
and   otherloc.fmin < gaploc.fmax
and   gaploc.fmin < otherloc.fmax
and   other.feature_id <> gap.feature_id
order by organism.common_name, chr.uniquename, gaploc.fmin, otherloc.fmin
;

/* List all gaps */
select organism.common_name as organism
    , chr.uniquename as chromosome
    , gap.uniquename as gap_uniquename
    , gaploc.fmin as gap_min
    , gaploc.fmax as gap_max
    , gaploc.fmax - gaploc.fmin as gap_size
    from feature gap
    join featureloc gaploc using (feature_id)
    join feature chr on gaploc.srcfeature_id = chr.feature_id
    join organism on chr.organism_id = organism.organism_id
where gaploc.rank = 0
and   gap.type_id = 818 /*gap*/
order by organism.common_name, chr.uniquename, gaploc.fmin
;
