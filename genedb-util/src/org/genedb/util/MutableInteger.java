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


package org.genedb.util;


/**
 * A simple int wrapper that allows it's value to be changed.
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */public class MutableInteger {
    private int value;

    public MutableInteger(int i) {
        this.value = i;
    }

    public void increment(int i) {
       value += i;
    }

    public int intValue() {
        return value;
    }
}
