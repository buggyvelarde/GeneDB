package org.genedb.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String getMD5(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
        }
        md.update(input.getBytes(), 0, input.length());
        return new BigInteger(1, md.digest()).toString(16);
    }


    public static String getMD5(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
        }
        md.update(bytes, 0, bytes.length);
        return new BigInteger(1, md.digest()).toString(16);
    }

    public static String getPathBasedOnMD5(String in, char seperator) {

        String md5 = getMD5(in);

        return String.format("%s%c%s%c%s",
            md5.substring(0, 2),
            seperator,
            md5.substring(2, 4),
            seperator,
            in);
    }

}
