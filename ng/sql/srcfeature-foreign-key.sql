alter table featureloc drop constraint "featureloc_srcfeature_id_fkey";

alter table featureloc add constraint "featureloc_srcfeature_id_fkey"
    FOREIGN KEY (srcfeature_id)
    REFERENCES feature(feature_id)
    ON DELETE NO ACTION
    DEFERRABLE INITIALLY DEFERRED
;
