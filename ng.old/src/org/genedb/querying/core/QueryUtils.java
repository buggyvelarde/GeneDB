/*
 * Copyright (c) 2007 Genome Research Limited.
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

package org.genedb.querying.core;

import org.springframework.beans.BeanWrapperImpl;

public class QueryUtils {

    public static String makeParseableDescription(String name, String[] paramNames, Object[] paramValues) {
        StringBuilder ret = new StringBuilder();
        ret.append(name);
        ret.append('{');
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (i>0) {
                    ret.append(';');
                }
                ret.append(paramNames[i]);
                ret.append('=');
                ret.append(paramValues[i]);
            }
        }
        ret.append('}');
        return ret.toString();
    }
    
    public static String makeParseableDescription(String name, String[] paramNames, Object target) {
        return makeParseableDescription(name, paramNames, getParamValues(target, paramNames));
    }
    
    public static Object[] getParamValues(Object target, String[] paramNames) {
        Object[] ret = new Object[paramNames.length];
        BeanWrapperImpl wrapper = new BeanWrapperImpl(target);
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i];
            ret[i] = wrapper.getPropertyValue(name);
        }
        return ret;
    }

}
