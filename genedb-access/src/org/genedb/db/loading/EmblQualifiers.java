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

/**
 * Constants file representing qualifiers found in EMBL file
 * 
 * @author Adrian Tivey (art)
 */
public class EmblQualifiers {


    public static final String QUAL_CHROMOSOME   = "chromosome";
    public static final String QUAL_SUPERCONTIG  = "supercontig";
    public static final String QUAL_CONTIG       = "contig";
    public static final String QUAL_SO_TYPE      = "so_type";
    public static final String QUAL_PRIVATE      = "private";
    public static final String QUAL_PSEUDO       = "pseudo";
    public static final String QUAL_NOTE         = "note";
    public static final String QUAL_CURATION     = "curation";
    public static final String QUAL_DB_XREF      = "db_xref";
    public static final String QUAL_PRODUCT      = "product";
    public static final String QUAL_GO           = "GO";
    public static final String QUAL_C_CURATION   = "controlled_curation";
    public static final String QUAL_EVIDENCE     = "evidence";
    
    // Naming
    public static final String QUAL_SYS_ID       = "systematic_id";
    public static final String QUAL_TEMP_SYS_ID  = "temporary_systematic_id";
    public static final String QUAL_PRIMARY      = "primary_name";
    public static final String QUAL_SYNONYM      = "synonym";
    public static final String QUAL_OBSOLETE     = "obsolete_name";
    public static final String QUAL_RESERVED     = "reserved_name";
    public static final String QUAL_PREV_SYS_ID  = "prev_systematic_id";
    
    // Deprecated ie not output but will be read
    public static final String QUAL_D_COLOUR     = "colour";
    public static final String QUAL_D_GENE       = "gene";
    public static final String QUAL_D_FASTA_FILE = "fasta_file";
    public static final String QUAL_D_LITERATURE = "literature";
    public static final String QUAL_D_PSU_DB_XREF = "psu_db_xref";
    
    
    
	
}

