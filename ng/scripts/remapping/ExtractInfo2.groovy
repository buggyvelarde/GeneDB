package org.genedb.misc.remapper

import java.io.File
import java.util.Set;
import java.util.List;

// This script reads the specified EMBL files and extracts the feature names and locations
// This data will be used in the next stage to remap the annotation.
// Sample usage:
// (groovy ExtractInfo.groovy L* > mapping.out) >& mapping.out.err
//

public class ExtractInfo {

    private boolean cdsHack
    private boolean transcriptNameHack

    ExtractInfo(boolean cdsHack, boolean transcriptNameHack) {
        this.cdsHack = cdsHack
        this.transcriptNameHack = transcriptNameHack
    }

    private void extractLocationInfo(String annotation) {

        File an = new File(annotation)
        String contigName = an.name

	// The top level feature name is determined by the file name.
	// Lets strip the .embl from the end if it has one
	if(contigName =~ /\S+\.embl/){
	   contigName = contigName.substring(0,contigName.length()-5)
	}


        def g
	    def lastFeatType
        boolean seenID = true
        an.eachLine({

            if (it ==~ /^FT   \w+\W+.*$/) {
                if (!seenID) {
                    System.err.println "**** Couldn't find ID in '$lastFeatType' (currently at '$it' ******"
                    //System.exit(1)
                }
                g = [:]
                seenID = false
                def parts = it.split()
                if (cdsHack && parts[1].equals("CDS")) {
                    g.type = "exon"
                } else {
                    g.type = parts[1]
		    
                }
		lastFeatType = parts[1]
                String location = parts[2]
                //System.err.println(location)
                if (location.contains("complement")) {
                    g.strand = '-'
                    location = location.replace("complement(", "")
                    location = location.substring(0, location.length()-1)
                } else {
                    g.strand = '+'
                }
                if (location.contains("join(")) {
                    //System.err.println "Found splicing - fix manually"
                    location = location.replace("join(", "")
                    location = location.substring(0, location.length()-1)
                    g.fmin = location
                } else {
                    def locs = location.split("\\.\\.")
                    if (locs.length==1) {
                        println("Unable to split '$location'")
                        System.exit(1)
                    }
                    g.fmin = locs[0]
                    g.fmax = locs[1]
                }
            } else {
                if (it ==~ /^SQ/) {
                    return
                }
                if (it ==~ /^FT\W+\/ID=.*$/) {
                    String s = it.substring(26)
                    String id = s.substring(0, s.length()-1)
                    if (cdsHack) {
                        id = id.replace('{', ':')
                        id = id.replace('}', '')
                        g.id = id
                    } else {
                        g.id = id
                    }
                    seenID = true
                    //if (transcriptNameHack) {
                    //    g.id = g.id.replace(":mRNA", ".1");
                    //}
                    if (g.fmax != null && !g.id.contains(",")) {
                       // if (g.type!="repeat_region") {
                            println g.type + '\t' + g.id + '\t' + contigName + '\t'+ g.strand + '\t' + g.fmin + '\t' + g.fmax
                       // }
                    } else {
                        // Splicing or id contains comma
			if (g.fmax != null) {
			   // Have a sensible location, but ids with commas
			   // So put out multiple lines, with the same location
			  String[] ids = g.id.split(',');
                          String newId = g.id.substring(0, g.id.lastIndexOf(':')) + ':'
                          String[] suffixStrings = g.id.substring(g.id.lastIndexOf(':')+1).split(",")
                          for (int i in 0..suffixStrings.length-1) {
                              println g.type + '\t' + newId + suffixStrings[i] + '\t' + contigName + '\t' + g.strand + '\t' + g.fmin + '\t' + g.fmax
			  }
			} else {
			  String[] locs = g.fmin.split(',');
                          for (int i in 0..locs.length-1) {
                              String[] tmp = locs[i].split("\\.\\.")
                              String newId = g.id.substring(0, g.id.lastIndexOf(':')) + ':'
                              String suffix = g.id.substring(g.id.lastIndexOf(':')+1).split(",")[i]
			      println g.type + '\t' + newId + suffix + '\t' + contigName + '\t' + g.strand + '\t' + tmp[0] + '\t' + tmp[1].split(",")[0]
                          }
			}
                    }
                }
            }
        })

    }


    private static void usage() {
      println "ExtractInfo remappedFilename1 ..."
    }


    public static void main(String[] args) {
      if (args.length < 1) {
        usage()
      }

      ExtractInfo app = new ExtractInfo(true, true)
      for (String string : args) {
          app.extractLocationInfo(string)
      }
    }
}
