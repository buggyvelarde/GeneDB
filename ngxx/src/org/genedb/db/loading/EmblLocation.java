package org.genedb.db.loading;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an EMBL location.
 *
 * @author rh11
 *
 */
public abstract class EmblLocation {
    private static final Logger logger = Logger.getLogger(EmblLocation.class);

    /**
     * A regular expression that matches an EMBL &lt;symbol&gt;, as defined in
     * Appendix II of the feature table definition.
     *
     * (Update: In the Apr 2009 version 8.1 of the feature table definition,
     * the BNF appendix has been removed. The permitted characters are still
     * listed in section 3.1.)
     */
    private static final String symbol = "[A-Za-z0-9_\\-'*]*[A-Za-z][A-Za-z0-9_\\-'*]*";

    /**
     * A pattern that matches an external location. Does not validate the local part.
     */
    private static final Pattern externalPattern = Pattern.compile(String.format("(?:(%s)::)?(%s)\\.(\\d+):(.+)", symbol, symbol));

    public static EmblLocation parse(String locationString) throws ParsingException {
        if (locationString.startsWith("complement")) {
            return Complement.parse(locationString.substring(11, locationString.length() - 1));
        }
        else if (locationString.startsWith("join(")) {
            if (!locationString.endsWith(")")) {
                throw new SyntaxError(
                    String.format("Failed to parse join location '%s': no closing parenthesis at end", locationString));
            }
            return Join.parse(locationString.substring(5, locationString.length() - 1).split(","));
        }
        else if (locationString.startsWith("order(")) {
            if (!locationString.endsWith(")")) {
                throw new SyntaxError(
                    String.format("Failed to parse order location '%s': no closing parenthesis at end", locationString));
            }
            return Order.parse(locationString.substring(6, locationString.length() - 1).split(","));
        }
        else if (locationString.matches("<?\\d+\\.\\.>?\\d+|\\d+\\^\\d+|[<>]?\\d+")) {
            return Simple.parse(locationString);
        }
        else if (locationString.matches("gap\\((?:unk100|\\d+)\\)")) {
            return Gap.parse(locationString);
        }
        else if (externalPattern.matcher(locationString).matches()) {
            return External.parse(locationString);
        }
        else {
            throw new SyntaxError("Cannot parse location string '" + locationString + "'");
        }
    }
    public abstract int getStrand();
    public abstract int getFmin();
    public abstract int getFmax();

    public boolean isExternal() {
        return false;
    }

    public List<EmblLocation> getParts() {
        return Collections.singletonList(this);
    }

    static class Complement extends EmblLocation {
        public static Complement parse(String locationString) throws ParsingException {
            return new Complement(EmblLocation.parse(locationString));
        }
        EmblLocation location;
        public Complement(EmblLocation location) {
            this.location = location;
        }

        @Override
        public int getStrand() {
            return -location.getStrand();
        }
        @Override
        public int getFmin() {
            return location.getFmin();
        }
        @Override
        public int getFmax() {
            return location.getFmax();
        }
        @Override
        public String toString() {
            return String.format("complement(%s)", location.toString());
        }
        @Override
        public List<EmblLocation> getParts() {
            // Reverse the order of the parts, and complement each one
            List<EmblLocation> parts = location.getParts();
            EmblLocation[] ret = new EmblLocation[parts.size()];

            for (int i=0; i < parts.size(); i++) {
                EmblLocation part = parts.get(i);
                EmblLocation complementedPart = part instanceof Complement ? ((Complement)part).location : new Complement(part);
                ret[parts.size() - i - 1] = complementedPart;
            }
            return Arrays.asList(ret);
        }
        @Override
        public boolean isExternal() {
            return location.isExternal();
        }
    }

    /**
     * A joining location: either join(...) or order(...).
     * @author rh11
     *
     */
    abstract static class Joining extends EmblLocation {
        protected abstract String operator();
        private int fmin = Integer.MAX_VALUE, fmax = Integer.MIN_VALUE;
        protected void add(EmblLocation location) throws DataError {
            if (! (location instanceof Gap) && !location.isExternal()) {
                int locationFmin = location.getFmin();
                int locationFmax = location.getFmax();
                int locationStrand = location.getStrand();

                if (locationFmin < fmin) {
                    if (locationStrand != -1 && !locations.isEmpty()) {
                        throw new DataError("Locations are joined in the wrong order");
                    }
                    fmin = locationFmin;
                }
                if (locationFmax > fmax) {
                    if (locationStrand == -1 && !locations.isEmpty()) {
                        throw new DataError("Locations are joined in the wrong order");
                    }
                    fmax = locationFmax;
                }
            }

            locations.add(location);
        }
        List<EmblLocation> locations = new ArrayList<EmblLocation>();

        @Override
        public int getStrand() {
            int strand = 0;
            for (EmblLocation location: locations) {
                if (strand == 0) {
                    strand = location.getStrand();
                } else if (strand != location.getStrand()) {
                    // This could occasionally be okay, if we have trans-splicing
                    // between opposite strands (perhaps of different chromosomes).
                    throw new RuntimeException("This EMBL location joins features from different strands. " +
                        "That's probably a mistake; if not, the code needs to be extended to cope.");
                }
            }
            return strand;
        }
        @Override
        public int getFmin() {
            if (locations.isEmpty()) {
                throw new RuntimeException("A join that doesn't join anything?");
            }
            return fmin;
        }
        @Override
        public int getFmax() {
            if (locations.isEmpty()) {
                throw new RuntimeException("A join that doesn't join anything?");
            }
            return fmax;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (EmblLocation location: locations) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(location.toString());
            }
            return String.format("%s(%s)", operator(), sb);
        }
        @Override
        public List<EmblLocation> getParts() {
            List<EmblLocation> ret = new ArrayList<EmblLocation>();
            for (EmblLocation location: locations) {
                ret.addAll(location.getParts());
            }
            return ret;
        }
        @Override
        public boolean isExternal() {
            for (EmblLocation location: locations) {
                if (location.isExternal()) {
                    return true;
                }
            }
            return false;
        }
    }

    static class Join extends Joining {
        @Override
        protected String operator() {
            return "join";
        }
        public static Join parse(String[] locationStrings) throws ParsingException {
            Join join = new Join();
            for (String locationString: locationStrings) {
                join.add(EmblLocation.parse(locationString));
            }
            return join;
        }
    }

    static class Order extends Joining {
        @Override
        protected String operator() {
            return "order";
        }
        public static Order parse(String[] locationStrings) throws ParsingException {
            Order order = new Order();
            for (String locationString: locationStrings) {
                order.add(EmblLocation.parse(locationString));
            }
            return order;
        }
    }

    static class External extends EmblLocation {
        String database;
        String accession;
        int version;
        Simple simple;

        public static External parse(String locationString) throws ParsingException {
            Matcher matcher = externalPattern.matcher(locationString);
            if (!matcher.matches()) {
                throw new RuntimeException(String.format("Failed to parse location string '%s'", locationString));
            }
            External external = new External();
            external.database = matcher.group(1);
            external.accession = matcher.group(2);
            external.version = Integer.parseInt(matcher.group(3));
            external.simple  = Simple.parse(matcher.group(4));

            return external;
        }
        @Override
        public int getStrand() {
            return simple.getStrand();
        }
        @Override
        public int getFmin() {
            return simple.getFmin();
        }
        @Override
        public int getFmax() {
            return simple.getFmax();
        }
        @Override
        public String toString() {
            if (database != null) {
                return String.format("%s::%s.%d:%s", database, accession, version, simple.toString());
            } else {
                return String.format("%s.%d:%s", accession, version, simple.toString());
            }
        }

        @Override
        public boolean isExternal() {
            return true;
        }

    }

    static class Simple extends EmblLocation {
        int fmin, fmax;
        boolean isFminPartial = false, isFmaxPartial = false;
        public Simple(int fmin, int fmax) {
            this.fmin = fmin;
            this.fmax = fmax;
        }

        public Simple(int fmin, boolean isFminPartial, int fmax, boolean isFmaxPartial) {
            this.fmin = fmin;
            this.isFminPartial = isFminPartial;
            this.fmax = fmax;
            this.isFmaxPartial = isFmaxPartial;
        }

        public int getLength() {
            return fmax - fmin;
        }

        /*
         * We lose some partiality information when parsing a simple location,
         * because the < and > symbols are ignored. Notice that single-base locations
         * of the form <23 (say) are not even representable in Chado.
         */
        private static final Pattern rangePattern = Pattern.compile("(<)?(\\d+)\\.\\.(>)?(\\d+)");
        private static final Pattern interbasePattern = Pattern.compile("(\\d+)\\^(\\d+)");
        private static final Pattern singleBasePattern = Pattern.compile("([<>])?(\\d+)");
        public static Simple parse(String locationString) throws ParsingException {
            Matcher rangeMatcher = rangePattern.matcher(locationString);
            Matcher interbaseMatcher = interbasePattern.matcher(locationString);
            Matcher singleBaseMatcher = singleBasePattern.matcher(locationString);
            if (rangeMatcher.matches()) {
                boolean isFminPartial = rangeMatcher.group(1) != null;
                int fmin = Integer.parseInt(rangeMatcher.group(2));
                boolean isFmaxPartial = rangeMatcher.group(3) != null;
                int fmax = Integer.parseInt(rangeMatcher.group(4));
                if (fmin > fmax) {
                    throw new DataError("Range end is before range start. (We don't support wrap-around features on circular chromosomes.)");
                }
                return new Simple(fmin-1, isFminPartial, fmax, isFmaxPartial);
            } else if (interbaseMatcher.matches()) {
                int before = Integer.parseInt(interbaseMatcher.group(1));
                int after  = Integer.parseInt(interbaseMatcher.group(2));
                if (after - before != 1) {
                    throw new SyntaxError(String.format("Failed to parse location '%s'", locationString));
                }
                return new Simple(before, before);
            } else if (singleBaseMatcher.matches()) {
                boolean isPartial = singleBaseMatcher.group(1) != null;
                int base = Integer.parseInt(singleBaseMatcher.group(2));
                if (isPartial) {
                    logger.warn(String.format("Location string '%s' has a form that cannot be represented in Chado" +
                            "(a single base of indeterminate location)", locationString));
                }
                return new Simple(base-1, base);
            } else {
                // This is an unusual error, since under most circumstances we won't even
                // try to parse a string as a SimpleLocation unless it looks like one. However
                // there is at least one exception to this: the local part of an External location
                // is parsed as a
                throw new SyntaxError(String.format(
                    "Failed to parse simple location '%s'", locationString));
            }
        }
        @Override
        public int getStrand() {
            return 1;
        }
        @Override
        public int getFmin() {
            return fmin;
        }
        @Override
        public int getFmax() {
            return fmax;
        }
        @Override
        public String toString() {
            if (fmin == fmax) {
                return String.format("%d^%d", fmin, fmin + 1);
            } else {
                return String.format("%s%d..%s%d", isFminPartial ? "<" : "", fmin+1, isFmaxPartial ? ">" : "", fmax);
            }
        }
    }

    private static final Pattern gapPattern = Pattern.compile("gap\\((?:(\\d+)|unk100)\\)");
    abstract static class Gap extends EmblLocation {
        public static Gap parse(String locationString) throws ParsingException {
            Matcher matcher = gapPattern.matcher(locationString);
            if (!matcher.matches()) {
                throw new SyntaxError("Failed to parse gap: "+locationString);
            }
            String gapSizeString = matcher.group(1);
            if (gapSizeString == null) {
                return new UnknownGap();
            } else {
                return new KnownGap(Integer.parseInt(gapSizeString));
            }
        }
        public abstract int getLength();
        @Override
        public int getStrand() {
            return 0;
        }
        @Override
        public int getFmin() {
            throw new RuntimeException("A gap doesn't know where it is. You have to work it out from the context.");
        }
        @Override
        public int getFmax() {
            // So that a subclass need only override getFmin()
            return getFmin() + getLength();
        }
    }
    static class KnownGap extends Gap {
        private int size;
        public KnownGap(int size) {
            this.size = size;
        }
        @Override
        public int getLength() {
            return size;
        }
        @Override
        public String toString() {
            return String.format("gap(%d)", size);
        }
    }
    static class UnknownGap extends Gap {
        @Override
        public String toString() {
            return "gap(unk100)";
        }
        @Override
        public int getLength() {
            return 100;
        }
    }
}
