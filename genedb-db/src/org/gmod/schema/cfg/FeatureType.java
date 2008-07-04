package org.gmod.schema.cfg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>@FeatureType</code> annotation is used to indicate,
 * for each feature class, which CvTerm this class corresponds to.
 *
 * It's similar to setting the <code>@DiscriminatorColumn</code>,
 * but does not require the cvterm_id to be hard-coded.
 *
 * There are two modes. Either specify a CV and a term name:
 * <code>@FeatureType(cv="sequence", term="gene")<code>
 * or specify a CV and an accession number:
 * <code>@FeatureType(cv="sequence", accession="0001077")<code>
 *
 * The former is more readable, but the latter is more robust
 * in the face of changes to the Sequence Ontology. For example,
 * the term with accession number 0001077 has recently been
 * renamed from 'transmembrane' to 'transmembrane_region'.
 *
 * @author rh11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FeatureType {
    String cv();
    String term()      default "";
    String accession() default "";
}
