/*
 * Copyright (c) 2002 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
package org.genedb.db.loading;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.utils.ChangeVetoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MiningUtils {

    public static boolean balancedBrackets(String string, char open, char close) {
        int depth = 0;

        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == open) {
                depth++;
            }
            if (string.charAt(i) == close) {
                depth--;
                if (depth < 0) {
                    return false;
                }
            }
        }

        return (depth == 0);
    }

    public static String getProperty(String key, Annotation an, String id) {
        if (!an.containsProperty(key)) {
            return null;
        }

        Object value = an.getProperty(key);
        if (value == null) {
            return null;
        }

        if (value instanceof List<?>) {
            Set<Object> s = new HashSet<Object>();
            s.addAll((List<?>) value);
            if (s.size() > 1) {
                if (id != null) {
                    System.err.println("WARN: Returning first value of List for key " + key
                            + " in " + id + " but there are " + s);
                } else {
                    System.err.println("WARN: Returning first value of List for key " + key
                            + " but there are " + s);
                }
            } else {
                if (id != null) {
                    System.err.println("WARN: Returning first (identical) value of List for key "
                            + key + " in " + id);
                } else {
                    System.err.println("WARN: Returning first (identical) value of List for key "
                            + key);
                }
            }

            return ((List<?>) value).get(0).toString();
        }

        return value.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getProperties(String key, Annotation an) {
        if (!an.containsProperty(key)) {
            return Collections.emptyList();
        }
        Object o = an.getProperty(key);

        if (o instanceof List) {
            return (List<String>) o;
        }

        if (o instanceof String) {
            List<String> tmp = new ArrayList<String>();
            tmp.add((String) o);
            return tmp;
        }
        if (o instanceof Boolean) {
            List<String> tmp = new ArrayList<String>();
            tmp.add(((Boolean) o).toString());
            return tmp;
        }
        throw new RuntimeException("Internal error: Couldn't process properties for '" + key
                + "', got '" + o.getClass() + "'");
    }

    @SuppressWarnings("unchecked")
    public static List<String> getProperties(String key, Map map) {
        if (!map.containsKey(key)) {
            return null;
        }
        Object o = map.get(key);

        if (o instanceof List) {
            return (List<String>) o;
        }

        if (o instanceof String) {
            List<String> tmp = new ArrayList<String>();
            tmp.add((String) o);
            return tmp;
        }
        return null;
    }

    public static void setProperty(Annotation an, String key, String value) {
        List<String> current = getProperties(key, an);
        if (current == null || current.size() == 0) {
            try {
                an.setProperty(key, value);
            } catch (IllegalArgumentException e) {
                // Deliberately empty
            } catch (ChangeVetoException e) {
                // Deliberately empty
            }
        } else {
            current.add(value);
        }
    }

    public static void setProperty(Annotation an, String key, List<String> values) {
        List<String> current = getProperties(key, an);
        if (current == null || current.size() == 0) {
            try {
                an.setProperty(key, values);
            } catch (IllegalArgumentException e) {
                // Deliberately empty
            } catch (ChangeVetoException e) {
                // Deliberately empty
            }
        } else {
            current.addAll(values);
        }
    }

    /**
     * Utility method to check what annotation a feature has
     *
     *
     * @param f The feature to check
     * @param requiredSingle Array of keys that MUST appear, with only a single
     *                value
     * @param requiredMultiple Array of keys that MUST appear, with multiple
     *                possible values
     * @param optionalSingle Array of keys that MAY appear, with only a single
     *                value
     * @param optionalMultiple Array of keys that MAY appear, with multiple
     *                possible values
     * @param fatal Should a problem stop the VM
     * @param reportExtra Report any keys not in above lists?
     * @return true if all conditions met, and no keys left over
     */
    @SuppressWarnings("unchecked")
    public static boolean sanityCheckAnnotation(Feature f, String[] requiredSingle,
            String[] requiredMultiple, String[] optionalSingle, String[] optionalMultiple,
            String[] discard, boolean fatal, boolean reportExtra) {

        Map temp = f.getAnnotation().asMap();
        Map<String, Object> map = new HashMap<String, Object>(temp);

        for (String discardKey : discard) {
            map.remove(discardKey);
        }
        map.remove("internal_data");

        if (checkRequiredSingle(f, requiredSingle, fatal, map)) {
            return false;
        }
        // System.err.println("AfterRequiredSingle: "+copy);
        if (checkRequiredMultiple(f, requiredMultiple, fatal, map)) {
            return false;
        }
        // System.err.println("AfterRequiredMultiple: "+copy);
        if (checkOptionalSingle(f, optionalSingle, fatal, map)) {
            return false;
        }
        // System.err.println("AfterOptionalSingle: "+copy);
        if (checkOptionalMultiple(f, optionalMultiple, fatal, map)) {
            return false;
        }
        // System.err.println("AfterOptionalMultiple: "+copy);
        if (reportExtra && !map.isEmpty()) {
            StringBuffer keys = new StringBuffer();
            int i = 0;
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                if (i > 0) {
                    keys.append("|");
                }
                keys.append(it.next());
                i++;
            }
            problem(f, keys.toString(), "Unexpected annotation", fatal, map);
            return false;
        }

        return true;
    }

    /**
     * @param f
     * @param requiredSingle
     * @param fatal
     * @param copy
     * @return
     */
    private static boolean checkRequiredSingle(Feature f, String[] requiredSingle, boolean fatal,
            Map<String, Object> map) {
        boolean result = true;
        for (int i = 0; i < requiredSingle.length; i++) {
            String check = requiredSingle[i];
            List<String> matches = getProperties(check, map);
            if (matches == null) {
                problem(f, check, "No value (required single)", fatal, map);
                result = false;
            }
            if (matches.size() == 0 || matches.size() > 1) {
                problem(f, check, "Wrong number of values (required multiple)", fatal, map);
                result = false;
            }
            map.remove(check);
        }
        return result;
    }

    /**
     * @param f
     * @param requiredSingle
     * @param fatal
     * @param copy
     * @return
     */
    private static boolean checkRequiredMultiple(Feature f, String[] requiredMultiple,
            boolean fatal, Map<String, Object> map) {
        boolean result = true;
        for (int i = 0; i < requiredMultiple.length; i++) {
            String check = requiredMultiple[i];
            List<String> matches = getProperties(check, map);
            if (matches == null) {
                problem(f, check, "No value (required multiple)", fatal, map);
                result = false;
            }
            if (matches.size() == 0) {
                problem(f, check, "Wrong number of values (required multiple)", fatal, map);
                result = false;
            }
            map.remove(check);
        }
        return result;
    }

    /**
     * @param f
     * @param requiredSingle
     * @param fatal
     * @param copy
     * @return
     */
    private static boolean checkOptionalMultiple(Feature f, String[] optionalMultiple,
            boolean fatal, Map<String, Object> map) {
        for (int i = 0; i < optionalMultiple.length; i++) {
            String check = optionalMultiple[i];
            // System.err.println("checkOptionalMultiple for '"+check+"'");
            List<String> matches = getProperties(check, map);
            if (matches != null && matches.size() > 0) {
                map.remove(check);
            }
        }
        return true;
    }

    /**
     * @param f
     * @param requiredSingle
     * @param fatal
     * @param copy
     * @return
     */
    private static boolean checkOptionalSingle(Feature f, String[] optionalSingle, boolean fatal,
            Map<String, Object> map) {
        boolean result = true;
        for (int i = 0; i < optionalSingle.length; i++) {
            String check = optionalSingle[i];
            List<String> matches = getProperties(check, map);
            if (matches != null && matches.size() > 1) {
                problem(f, check, "Wrong number of values (optional single)", fatal, map);
                result = false;
            }
            map.remove(check);
        }
        return result;
    }

    private static void problem(Feature f, String check, String msg, boolean fatal,
            Map<String, Object> map) {
        System.err.println(msg + " for '" + check + "' in '" + f.getType() + "' at '"
                + f.getLocation() + "'");
        System.err.println(map);
        if (fatal) {
            System.exit(-1);
        }
    }

}
