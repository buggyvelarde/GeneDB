/**
 *
 */
package org.genedb.db.loading;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the feature table (FT section) of an EMBL file.
 *
 * @author rh11
 *
 */
class FeatureTable extends EmblFile.Section {
    private static final Logger logger = Logger.getLogger(FeatureTable.class);

    private String filePath;

    FeatureTable(EmblFile emblFile) {
        this.filePath = emblFile.getFilePath();
    }

    class Feature {
        String type;
        int lineNumber;
        EmblLocation location;
        List<Qualifier> qualifiers = new ArrayList<Qualifier>();

        public String getFilePath() {
            return filePath;
        }

        /**
         * Get the values of the named qualifier. If the qualifier does not appear at all,
         * an empty list is returned. If it appears multiple times, the values are in order
         * of appearance.
         * @param keys the name(s) of the qualifier(s)
         * @return a list of values
         */
        public List<String> getQualifierValues(String... keys) {
            List<String> ret = new ArrayList<String>();
            for (Qualifier qualifier: qualifiers) {
                for (String key: keys) {
                    if (qualifier.name.equals(key)
                            && !isQualifierIgnored(type, key)) {
                        qualifier.used = true;
                        ret.add(qualifier.value);
                    }
                }
            }
            return ret;
        }

        /**
         * Does the feature have the specified qualifier?
         *
         * @param key the name of the qualifier
         * @return <code>true</code> if the feature has the specified qualifier,
         *         or <code>false</code> if not
         */
        public boolean hasQualifier(String key) {
            return !getQualifierValues(key).isEmpty();
        }

        /**
         * Get the (unique) value of the specified qualifier.
         *
         * @param key the name of the qualifier
         * @return the value of the qualifier, or <code>null</code> if the qualifier
         *              is not present on the feature
         * @throws DataError if the qualifer appears more than once
         */
        public String getQualifierValue(String key) throws DataError {
            List<String> values = getQualifierValues(key);
            if (values.isEmpty()) {
                return null;
            }
            if (values.size() > 1) {
                // If the qualifier is simply repeated, with the same value, that's okay.
                String uniqueValue = null;
                for (String value: values) {
                    if (uniqueValue == null) {
                        uniqueValue = value;
                    } else if (!uniqueValue.equals(value)) {
                        throw new DataError(String.format("%s:The qualifier '%s' appears more than once in feature '%s' at line %d (with different values)",
                            this.getFilePath(),key, type, lineNumber));
                    }
                }
                logger.warn(String.format("The qualifier /%s=\"%s\" is repeated in feature '%s' at line %d", key, uniqueValue, type, lineNumber));
                return uniqueValue;
            }
            return values.get(0);
        }

        public Iterable<String> getUnusedQualifiers() {
            Set<String> unusedQualifiers = new HashSet<String>();
            for (Qualifier qualifier: qualifiers) {
                if (!qualifier.used) {
                    unusedQualifiers.add(qualifier.toString());
                }
            }
            return unusedQualifiers;
        }

        public Collection<String> getUnusedQualifierNames() {
            Set<String> unusedQualifiers = new HashSet<String>();
            for (Qualifier qualifier: qualifiers) {
                if (!qualifier.used) {
                    unusedQualifiers.add(qualifier.name);
                }
            }
            return unusedQualifiers;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Qualifier qualifier: qualifiers) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(qualifier);
            }
            return String.format("%s at %s: %s", type, location, sb);
        }

        /**
         * Try to ascertain the uniqueName of the
         * transcript this feature represents or corresponds to.
         * This makes sense for CDS and UTR features, at least.
         *
         * @return the value of the <code>/systematic_id</code>
         *              or <code>temporary_systematic_id</code> qualifier.
         * @throws DataError unless the feature has precisely one of the qualifiers
         *              <code>/systematic_id</code> and <code>temporary_systematic_id</code>.
         */
        public String getUniqueName() throws DataError {
            return getUniqueName(true);
        }

        /**
         * Try to ascertain the uniqueName of the
         * transcript this feature represents or corresponds to.
         * This makes sense for CDS and UTR features, at least.
         * <p>
         * This uses the value of the qualifier <code>/systematic_id</code>
         * or <code>/temporary_systematic_id</code> if available. If neither
         * of those is found, the qualifier <code>/FEAT_NAME</code> is used;
         * if that is not found either, we look for the qualifier <code>/locus_tag</code>.
         *
         * @param failIfNotFound whether to throw a DataError if no suitable
         *              name can be found
         * @return the unique name of the feature.
         *              If <code>failIfNotFound</code> is false and no suitable
         *              name is found, we return <code>null</code>
         * @throws DataError unless the feature has precisely one of the qualifiers
         *              <code>/systematic_id</code>, <code>/locus_tag</code>
         *              and <code>temporary_systematic_id</code>.
         *              Only if <code>failIfNotFound</code> is true.
         */
        public String getUniqueName(boolean failIfNotFound) throws DataError {
            String temporarySystematicId = this.getQualifierValue("temporary_systematic_id");
            String systematicId = this.getQualifierValue("systematic_id");
            String featName = this.getQualifierValue("FEAT_NAME");
            String locusTag = this.getQualifierValue("locus_tag");

            if (temporarySystematicId != null && systematicId != null) {
                throw new DataError(
                    String.format("%s feature has both /systematic_id and /temporary_systematic_id", this.type));
            }
            else if (temporarySystematicId != null) {
                temporarySystematicId = temporarySystematicId.replaceAll("\\s","").trim(); //When ids wrap lines in the EMBL file a space is introduced
                return temporarySystematicId;
            } else if (systematicId != null) {
                systematicId = systematicId.replaceAll("\\s","").trim(); //When ids wrap lines in the EMBL file a space is introduced
                return systematicId;
            } else if (featName != null) {
                featName = featName.replaceAll("\\s","").trim(); //When ids wrap lines in the EMBL file a space is introduced
                logger.warn(
                    String.format("%s feature has neither /systematic_id nor /temporary_systematic_id; " +
                                  "using /FEAT_NAME=\"%s\"", this.type, featName));
                return featName;
            } else if (locusTag != null) {
                locusTag = locusTag.replaceAll("\\s","").trim(); //When ids wrap lines in the EMBL file a space is introduced
                logger.warn(
                    String.format("%s feature has no /systematic_id or /FEAT_NAME; using /locus_tag=\"%s\"",
                        this.type, locusTag));
                return locusTag;
            } else {
                if (failIfNotFound) {
                    throw new DataError(
                        String.format("%s feature has none of /systematic_id, /temporary_systematic_id, /FEAT_NAME or /locus_tag", this.type));
                }
                return null;
            }
        }
    }

    class CDSFeature extends Feature {
        /**
         * Try to ascertain the gene name from the qualifiers present.
         *
         * @return the gene name, or <code>null</code> if we couldn't find one
         * @throws DataError if the qualifiers present an ambiguity
         */
        public String getGeneName() throws DataError {
            String primaryName = getQualifierValue("primary_name");
            List<String> geneQualifiers = getQualifierValues("gene");
            String featName = getQualifierValue("FEAT_NAME");

            if (primaryName != null) {
                return primaryName;
            } else if (featName != null) {
                return featName;
            } else if (geneQualifiers.size() == 1) {
                String geneQualifier = geneQualifiers.get(0);

                // S. mansoni has some qualifiers of the form /gene="RPN1; ORFNames=CaO19.4956;"
                int semicolonIndex = geneQualifier.indexOf(';');
                if (semicolonIndex < 0) {
                    return geneQualifier;
                } else {
                    return geneQualifier.substring(0, semicolonIndex);
                }
            } else {
                return null;
            }
        }

        /**
         * If this CDS feature represents an alternative splice-form, return the
         * uniqueName of its gene. If it represents a singly-spliced gene, return
         * <code>null</code>.
         *
         * @return the <code>uniqueName</code> of the associated gene, or <code>null</code>
         *          if this is a singly-spliced gene
         */
        public String getSharedId() throws DataError {
            String uniqueName = getUniqueName();
            String sharedId   = getQualifierValue("shared_id");

            if (sharedId != null) {
                return sharedId;
            }

            if (getQualifierValues("other_transcript").isEmpty()) {
                // We have neither /shared_id nor /other_transcript, so
                // assume this is a singly-spliced gene.
                return null;
            }

            // An alternately-spliced transcript does not always have a /shared_id qualifier.
            // Sometimes there are just a selection of /other_transcript qualifiers. In that
            // case, we try to take the stem of the transcript ID.
            Matcher dotMatcher = Pattern.compile("(.*)\\.\\d+").matcher(uniqueName);
            if (! dotMatcher.matches()) {
                throw new DataError (String.format(
                    "Alternately-spliced transcript '%s' has no /shared_id qualifier, and its systematic name doesn't end with .<n>",
                    uniqueName));
            }
            sharedId = dotMatcher.group(1);
            logger.info(String.format("[CDS %s] assuming /shared_id of '%s'", uniqueName, sharedId));
            return sharedId;
        }

        /**
         * Does this CDS feature represent a pseudogenic transcript?
         *
         * @return <code>true</code> if it represents a pseudogenic transcript,
         *          <code>false</code>if it doesn't.
         */
        public boolean isPseudo() {
            return hasQualifier("pseudo");
        }
        
        /**
         * Is this feature obsolete?
         *
         * @return <code>true</code> if it is (i.e. note="true",
         *          <code>false</code>if it isn't.
         */
        public boolean isObsolete() {
            if (getQualifierValues("note").contains(new String("true"))){
                return true;
            }
            return false;
        }
    }
    private class Qualifier {
        String name, value;
        boolean valueIsQuoted;
        private boolean used = false;
        public Qualifier(String name, String value, boolean valueIsQuoted) {
            this.name = name;
            this.value = value;
            this.valueIsQuoted = valueIsQuoted;
        }

        public Qualifier(String name) {
            this(name, null, false);
        }

        @Override
        public String toString() {
            String value = this.value;
            String format;
            if (valueIsQuoted) {
                format = "/%s=\"%s\"";
                value = value.replaceAll("\"", "\"\"");
            } else if (value == null) {
                format = "/%s";
            } else {
                format = "/%s=%s";
            }
            return String.format(format, name, value);
        }
    }

    private List<Feature> features = new ArrayList<Feature>();
    public Iterable<Feature> getFeatures() {
        List<Feature> nonIgnoredFeatures = new ArrayList<Feature>();
        for (Feature feature: features) {
            if (isFeatureIgnored(feature)) {
                logger.info(String.format("Ignoring '%s' feature at line %d", feature.type, feature.lineNumber));
            } else {
                nonIgnoredFeatures.add(feature);
            }
        }
        return nonIgnoredFeatures;
    }

    private Feature currentFeature = null;
    private StringBuilder currentLocation = null;
    @Override
    public void addData(int lineNumber, String data) throws ParsingException {
        String featureType = data.substring(0, 16).trim();
        String featureData = data.substring(16);
        if ("".equals(featureType)) {
            // continuation of current feature
            if (currentLocation != null) {
                parseLocationLine(featureData);
            } else {
                parseQualifierLine(featureData);
            }
        } else {
            if (currentLocation != null) {
                throw new SyntaxError("Feature found while location incomplete");
            }
            if (currentQualifier != null) {
                throw new SyntaxError("Feature found while qualifier incomplete");
            }

            finished();
            currentFeature = featureType.equals("CDS") ? new CDSFeature() : new Feature();
            currentFeature.lineNumber = lineNumber;
            currentFeature.type = featureType;
            if (featureData.endsWith(",")) {
                // Location is split over multiple lines
                currentLocation = new StringBuilder(featureData);
            } else {
                currentFeature.location = EmblLocation.parse(featureData);
            }
        }
    }

    /*
     * Add the current feature to the list of features.
     * Called at the end of each feature:
     * we call it from addData when another feature is encountered,
     * and the EmblFile parser calls it at the end of the feature table.
     */
    @Override
    public void finished() {
        if (currentFeature != null) {
            features.add(currentFeature);
        }
    }

    private void parseLocationLine(String line) throws ParsingException {
        currentLocation.append(line);
        if (! line.endsWith(",")) {
            currentFeature.location = EmblLocation.parse(currentLocation.toString());
            currentLocation = null;
        }
    }

    private static final String symbolPattern = "[\\w'*-+]*[A-Za-z][\\w'*-+]*";
    static final Pattern qualifierPattern = Pattern.compile("/(" + symbolPattern + ")(?:=(.*))?");
    static final Pattern quotedStringPattern = Pattern.compile("\"((?:[^\"]|\"\")*)\"");

    private String currentQualifier = null;
    private StringBuilder currentString = null;
    private void parseQualifierLine(String data) throws ParsingException {
        if (currentString != null) {
            // There's a quoted string on a previous line that hasn't been closed
            currentString.append(' ');
            if (quotesMatch(data)) {
                // The string continues on the next line
                currentString.append(data.replaceAll("\"\"", "\""));
            }
            else {
                // This is the last line of the string
                if (! data.endsWith("\"")) {
                    throw new SyntaxError("Failed to parse string data: unbalanced quotes");
                    
                }
                currentString.append(data.substring(0, data.length() - 1).replaceAll("\"\"", "\""));
                currentFeature.qualifiers.add(new Qualifier(currentQualifier, currentString.toString(), true));

                currentQualifier = null;
                currentString = null;
            }
        } else {
            // We are not in the middle of a quoted string, so expect a qualifier
            Matcher qualifierMatcher = qualifierPattern.matcher(data);
            if (!qualifierMatcher.matches()) {
                throw new SyntaxError(String.format("Expected a qualifier, found '%s'", data));
            }
            String qualifierName = qualifierMatcher.group(1);
            String qualifierData = qualifierMatcher.group(2);

            if (qualifierData == null) {
                // e.g. /pseudo
                currentFeature.qualifiers.add(new Qualifier(qualifierName));
            }
            else {
                Matcher quotedStringMatcher = quotedStringPattern.matcher(qualifierData);
                if (quotedStringMatcher.matches()) {
                    // Quoted string all on this line, like /foo="bar"
                    currentFeature.qualifiers.add(new Qualifier(qualifierName, quotedStringMatcher.group(1).replaceAll("\"\"", "\""), true));
                } else if (qualifierData.startsWith("\"")) {
                    // Quoted string that continues on the next line, e.g. /foo="bar "" baz ...\n
                    if (quotesMatch(qualifierData)) {
                        throw new SyntaxError("Failed to parse string data: unbalanced quotes");
                    }
                    currentQualifier = qualifierName;
                    currentString = new StringBuilder(qualifierData.substring(1).replaceAll("\"\"", "\""));
                } else {
                    // Not a quoted string. Treat the qualifier value as a simple identifier.
                    currentFeature.qualifiers.add(new Qualifier(qualifierName, qualifierData, false));
                }
            }
        }
    }
    /**
     * Does the string contain an even number of double-quotes?
     * @param string
     * @return <code>true</code> if string has an even number of double-quotes,
     *          or <code>false</code> if it has an odd number.
     */
    static boolean quotesMatch(String string) {
        boolean even = true;
        for (char c: string.toCharArray()) {
            if (c == '"') {
                even = !even;
            }
        }
        return even;
    }

    private Set<String> ignoredFeatureTypes = new HashSet<String>();
    private Set<String> ignoredQualifiers = new HashSet<String>();
    private Map<String,Set<String>> ignoredQualifiersByFeatureType
        = new HashMap<String,Set<String>>();

    private boolean isFeatureIgnored(Feature feature) {
        return ignoredFeatureTypes.contains(feature.type);
    }

    /**
     * Ignore the named qualifier, i.e. do not return any values
     * for the qualifier from
     * {@link Feature#getQualifierValue(String)}
     * or {@link Feature#getQualifierValues(String...)}. Ignored
     * qualifiers are still returned by {@link Feature#getUnusedQualifiers()}
     * and {@link Feature#getUnusedQualifierNames()}.
     *
     * @param qualifier the name of the qualifier to ignore
     */
    public void ignoreFeature(String featureType) {
        logger.info(String.format("Ignoring features of type '%s'", featureType));
        ignoredFeatureTypes.add(featureType);
    }

    private boolean isQualifierIgnored(String featureType, String qualifier) {
        if (ignoredQualifiers.contains(qualifier)) {
            return true;
        }
        synchronized(ignoredQualifiersByFeatureType) {
            if (ignoredQualifiersByFeatureType.containsKey(featureType)
                    && ignoredQualifiersByFeatureType.get(featureType).contains(qualifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ignore the named qualifier, i.e. do not return any values
     * for the qualifier from
     * {@link Feature#getQualifierValue(String)}
     * or {@link Feature#getQualifierValues(String...)}. Ignored
     * qualifiers are still returned by {@link Feature#getUnusedQualifiers()}
     * and {@link Feature#getUnusedQualifierNames()}.
     *
     * @param qualifier the name of the qualifier to ignore
     */
    public void ignoreQualifier(String qualifier) {
        logger.info(String.format("Ignoring qualifier /%s on all feature types", qualifier));
        ignoredQualifiers.add(qualifier);
    }

    /**
     * Ignore the named qualifier when it appears on a feature of the specified type,
     * i.e. do not return any values for the qualifier from
     * {@link Feature#getQualifierValue(String)}
     * or {@link Feature#getQualifierValues(String...)}. Ignored
     * qualifiers are still returned by {@link Feature#getUnusedQualifiers()}
     * and {@link Feature#getUnusedQualifierNames()}.
     *
     * @param qualifier the name of the qualifier to ignore
     * @param featureType the type of feature on which to ignore the named qualifier
     */
    public void ignoreQualifier(String qualifier, String featureType) {
        logger.info(String.format("Ignoring qualifier /%s on '%s' features", qualifier, featureType));
        synchronized(ignoredQualifiersByFeatureType) {
            if (!ignoredQualifiersByFeatureType.containsKey(featureType)) {
                ignoredQualifiersByFeatureType.put(featureType, new HashSet<String>());
            }
            ignoredQualifiersByFeatureType.get(featureType).add(qualifier);
        }
    }

    /**
     * Reset the list of ignored qualifiers, so that no qualifier
     * is ignored.
     */
    public void resetIgnoredQualifiers() {
        ignoredQualifiers.clear();
        ignoredQualifiersByFeatureType.clear();
    }
}