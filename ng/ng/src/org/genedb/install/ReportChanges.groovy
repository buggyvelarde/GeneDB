package org.genedb.install;

import groovy.sql.Sql
import java.util.regex.Matcher

/**
*
*/
class ReportChanges {

    def geneId = 792;
    def mRnaId = 321;
    def organismId = 27
    def proteinId = 191
    def exonId = 234
    def privateId = 26766
    def productCv = 25
    def ecNumId = 26767
    def debugId = 'not_a_real_id'

    def db

    Map privateMap = [:]

    boolean writeBack = true;


    void process(def latest, def genes, boolean mungeLocations) {

        def count = 0

        latest.eachRow("select * from feature where type_id=${geneId} and is_obsolete=false and organism_id=${organismId}") {
            MiniReportGene mrg = new MiniReportGene();
            mrg.featureId = it.feature_id;
            mrg.uniqueName = it.uniquename;

//            System.err.println it.uniquename
            // Doesn't cope with alternate splicing


            def chromsome =
                latest.firstRow("select f.uniquename from feature f, featureloc fl where fl.srcfeature_id=f.feature_id and fl.feature_id=${it.feature_id}").uniquename
            mrg.chromosome = chromsome
            def row =
                latest.firstRow("select subject_id from feature_relationship where object_id=${it.feature_id}")

            if (row == null) {
                //if (!mrg.uniqueName.contains("RNA")) {
                    //System.err.println("${count++} Problem retrieving mRNA for '${mrg.uniqueName}' '${mrg.featureId}'")
                //}
                return
            }
            def mRnaRow = latest.firstRow("select uniquename, type_id from feature where feature_id=${row.subject_id}");

            int mrnaId = row.subject_id
            if (mRnaRow.type_id != 321) {
                //System.err.println("Problem retrieving '${mRnaRow.uniquename}' isn't an mrna")
                return
            }


            def protein = -1;
            List<Integer> exonIds = new ArrayList()

            latest.eachRow("select subject_id from feature_relationship where object_id=${mrnaId}") {
                def row2 = latest.firstRow("select feature_id, uniquename, type_id from feature where feature_id=${it.subject_id}");
                switch (row2.type_id) {
                case proteinId:
                    if (protein != -1) {
                        System.err.println("Found a protein but already have one");
                        return;
                    }
                    protein = it.subject_id
                    break
                case exonId:
                    if (mrg.uniqueName == debugId) {
                        println "Adding an exon"
                    }
                    exonIds.add(row2.feature_id)
                    break;
                case 275:
                    // intron
                case 292:
                    // 3 utr
                case 291:
                    // 5 utr
                case 1481:
                    // secis
                    // Ignore
                    break;
                default:
                    System.err.println("Got an unexpected feature '${row2.uniquename}' '${row2.type_id}'");
                }
                //println row2.uniquename
            }

            mrg.numExons = exonIds.size()
            if (mrg.uniqueName == debugId) {
                println "In summary, mrg.numExons = ${mrg.numExons} while exonIds.size() = ${exonIds.size()}"
            }

            //if (mrg.chromosome == "Pf3D7_12") {
            //    System.err.println "Hit on chromosome 12 for '${mrg.uniqueName}'"
            //}

            exonIds.each() {
                def locRow =
                    latest.firstRow("select fmin, fmax from featureloc fl where fl.feature_id=${it}")
                int min = locRow.fmin
                int max = locRow.fmax
                if (mrg.chromosome == "Pf3D7_12" && mungeLocations) {
                    if (min > 868570) {
                        //System.err.println "Munging min '${min}' for ${mrg.uniqueName}";
                        min--
                    }
                    if (max > 868570) {
                        //System.err.println "Munging max '${max}' for ${mrg.uniqueName}";
                        max --
                    }
                }
                def range = min..max
                mrg.addExonLoc(range)
                if (mrg.uniqueName == debugId) {
                    println "Adding range from ${min} to ${max} so mrg.getExonLocs = ${mrg.getExonLocs().size()}"
                }
            }
            if (mrg.uniqueName == debugId) {
                println "In summary, mrg.getExonLocs = ${mrg.getExonLocs().size()}"
            }

            latest.eachRow("select value from featureprop where feature_id=${protein} and type_id=${privateId}") {
                if (it.value == null || it.value.length()==0) {
                    return;
                }
                if (it.value.contains("(autocomment")) {
                    return;
                }
//                System.err.println it.value
                privateMap[it.value.toLowerCase()] = privateMap.get(it.value.toLowerCase(),0) + 1
                mrg.privates.add(it.value)
            }

            if (protein == -1) {
                println "Unable to find  a protein for ${mrg.uniqueName}"
            }

            latest.eachRow("select cvt.name from feature_cvterm fc, cvterm cvt where fc.cvterm_id = cvt.cvterm_id and cvt.cv_id=25 and fc.feature_id=${protein}") {
                mrg.addProduct(it.name)
            }

            latest.eachRow("select value from featureprop where feature_id=${protein} and type_id=${ecNumId}") {
                mrg.ecNums.add(it.value)
            }

            latest.eachRow("select cvt.name from feature_cvterm fc, cvterm cvt where fc.cvterm_id = cvt.cvterm_id and cvt.cv_id in (13, 14,15,16) and fc.feature_id=${protein}") {
                mrg.goTerms.add(it.name)
            }

            latest.eachRow("select s.name from feature_synonym fs, synonym s where fs.synonym_id = s.synonym_id and s.type_id=26801 and fs.feature_id=${protein}") {
                //println it.name
                mrg.names.add(it.name)
            }

            genes.put(mrg.uniqueName, mrg)

            //println "${mrg.featureId}\t${mrg.uniqueName}"
        }
    }


    static void main(args) {
        ReportChanges rp = new ReportChanges()

        def latest = Sql.newInstance(
                'jdbc:postgresql://pathdbsrv1a.internal.sanger.ac.uk:10101/malaria_workshop',
                'pathdb',
                'Pyrate_1',
                'org.postgresql.Driver')
        Map<String,MiniReportGene> latestGenes = new HashMap<String,MiniReportGene>();
        rp.process(latest, latestGenes, true);
        System.err.println("Loaded latest");

        def oldest = Sql.newInstance(
                'jdbc:postgresql://pcs4e.internal.sanger.ac.uk:10102/postgres',
                'pathdb',
                'Pyrate_1',
                'org.postgresql.Driver')
        Map<String,MiniReportGene> oldestGenes = new HashMap<String,MiniReportGene>();
        rp.process(oldest, oldestGenes, false);
        System.err.println("Loaded oldest");

        println("Number of genes: old: ${oldestGenes.size()} new: ${latestGenes.size()}")

        println("New genes found")
        int i = rp.reportMapDiffs(latestGenes, oldestGenes, true)
        println("Completely new genes: ${i}")
        println("Old genes not now present");
        i = rp.reportMapDiffs(oldestGenes, latestGenes, true)
        println("Old genes that have vanished: ${i}")

        rp.outputResults(latestGenes, oldestGenes)
    }


    void outputResults(def latestGenes, def oldestGenes) {

/*        println("Private entries with count")
        def tmpMap = new TreeMap(privateMap)
        tmpMap.each() {
            print it.value.toString().padLeft(7) + " : "
            println it.key
        }*/

        List namesLessList = []
        List namesMoreList = []
        List exonLessList = []
        List exonMoreList = []
        List exonDifferentList = []
        List ecLessList = []
        List ecMoreList = []
        List goLessList = []
        List goMoreList = []
        List productsMoreList = []
        List productsLessList = []

        int newProducts = 0
        int oldProducts = 0

        latestGenes.each() {
            MiniReportGene current = it.value
            MiniReportGene old = oldestGenes.get(it.key)

            if (old != null) {
                compareSizeList(current.names, old.names, current.uniqueName, namesMoreList, namesLessList, true)
                //compareSize(current.numExons, old.numExons, current.uniqueName, exonMoreList, exonLessList)
                compareSizeList(current.ecNums, old.ecNums, current.uniqueName, ecMoreList, ecLessList, false)
                compareSizeList(current.goTerms, old.goTerms, current.uniqueName, goMoreList, goLessList, false)
                compareSizeList(current.products, old.products, current.uniqueName, productsMoreList, productsLessList, true)
                //println "ExonCount Gene: ${current.uniqueName} Now: ${current.getExonLocs().size()} Old: ${old.getExonLocs().size()}}"
                compareExons(current.getExonLocs(), old.getExonLocs(), current.uniqueName, exonDifferentList, exonMoreList,exonLessList)

                oldProducts += old.products.size()
                newProducts += current.products.size()
            }
        }

        //reportLists("primary names", namesMoreList, namesLessList)
        //reportLists("exons", exonMoreList, exonLessList)
        reportLists("EC assignments", ecMoreList, ecLessList)
        reportLists("GO assignments", goMoreList, goLessList)
        reportLists("Products", productsMoreList, productsLessList)

        println "Total meaningful products old: '${oldProducts}' new: '${newProducts}'"

        println("\n\n\nExons: There are '${exonMoreList.size}' genes that have more now, '${exonLessList.size()}' that have less and '${exonDifferentList.size()}' that have a different structure\n");
        exonMoreList.each() {
            println it
        }
        exonLessList.each() {
            println it
        }
        exonDifferentList.each() {
            println it
        }

        println("Done");
    }

    void reportLists(String category, List more, List less) {
        println("\n\n\n${category}: There are '${more.size}' genes that have more now, '${less.size()}' that have less\n");
        more.each() {
            println it
        }
        less.each() {
            println it
        }
    }

    void compareSizeList(def newList, def oldList, String name, List more, List less, boolean report) {
        def newSize = newList.size()
        def oldSize = oldList.size()
        if (newSize > oldSize) {
            String tmp = "${name} ('${newSize}' was '${oldSize}')"
            if (report) {
                tmp += "  '$newList'"
            }
            more.add(tmp)
        } else {
            if (newSize < oldSize) {
                less.add("${name} ('${newSize}' - was '${oldSize}')");
            }
        }
    }

    void compareExons(def newList, def oldList, String name, List different, List more, List less) {
        def newSize = newList.size()
        def oldSize = oldList.size()
        if (newSize > oldSize) {
            String tmp = "${name} ('${newSize}' was '${oldSize}')"
            more.add(tmp)
            return
        }
        if (newSize < oldSize) {
            less.add("${name} ('${newSize}' - was '${oldSize}')")
            return
        }
        // Same num of exons - structural changes?
        if (newList != oldList) {
            different.add("${name} ('${newList}' - was '${oldList}')")
        }
    }

    int reportMapDiffs(Map one, Map two, boolean delete) {
        int ret = 0;
        one.keySet().each() {
            if (!two.containsKey(it)) {
                println "${it}"
                ret++;
                if (delete) {
                    one.remove(one.get(it))
                }
            }
        }
        return ret
    }


}