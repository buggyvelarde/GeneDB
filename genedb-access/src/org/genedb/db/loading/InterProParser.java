package org.genedb.db.loading;

import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InterProParser {

    //    0     1 2       3          4          5      6
    // GeneACC  ? ? NATIVE_PROG NATIVE_ACC NATIVE_DESC ?

    // 7 8 9 10   11      12       13
    // ? ? ? ?  IP_ACC IP_DESC GO_DETAILS

    private static final int COL_ID=0;
    private static final int COL_NATIVE_PROG=3;
    private static final int COL_NATIVE_ACC=4;
    private static final int COL_ACC=11;
    private static final int COL_GO=13;

    private SequenceDao sequenceDao;
    private FeatureUtils featureUtils;
    private static Map<String, String> months = new HashMap<String, String>(12);
    private HibernateTransactionManager hibernateTransactionManager;
    private static HashMap<String, String> dbs;

    static {
        dbs = new HashMap<String, String>();
        dbs.put("HMMPfam", "Pfam");
        dbs.put("ScanProsite", "PROSITE");
        dbs.put("FPrintScan", "PRINTS");
        dbs.put("ProfileScan", "PROSITE");
        dbs.put("ScanRegExp", "PROSITE");
        dbs.put("HMMSmart", "SMART");
        dbs.put("BlastProDom", "ProDom");
        dbs.put("Superfamily", "Superfamily");
        dbs.put("superfamily", "Superfamily");

        months.put("Jan", "01");
        months.put("Feb", "02");
        months.put("Mar", "03");
        months.put("Apr", "04");
        months.put("May", "05");
        months.put("Jun", "06");
        months.put("Jul", "07");
        months.put("Aug", "08");
        months.put("Sep", "09");
        months.put("Oct", "10");
        months.put("Nov", "11");
        months.put("Dec", "12");

    }



    public void parse(String filename) {

        //System.err.println("Filename is  "+filename);
        File fl = new File(filename);
        if (!fl.exists()) {
            System.err.println("WARN: INTERPRO file/directory doesn't exist: "+filename);
            return;
        }

        if ( fl.isDirectory() ) {
            String[] fls = fl.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if ( name.endsWith("~")) {
                            return false;
                        }
                        return true;
                    }
                });
            for (int i=0; i < fls.length; i++) {
                parse(filename+"/"+fls[i]);
            }
            return;
        }


        System.err.println("Reading interpro from "+filename);

        String[][] ret = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader( fl ) );
            CharSVParser parser = new CharSVParser(br, "\t", true, 0, "#" );
            ret = parser.getValues();
        } catch (FileNotFoundException exp) {
            exp.printStackTrace();
        } catch (IOException exp) {
            exp.printStackTrace();
        }


        if ( ret != null ) {
            // Go through the results and pull the rows into the
            // hashmap genes, keyed on gene names, where the values
            // are ArrayLists of String[]
            Map<String, List<InterproRow>> genes = new HashMap<String, List<InterproRow>>();
            List<InterproRow> col = new ArrayList<InterproRow>();
            for ( int i = 0; i < ret.length; i++ ) {
                String geneId = ret[i][COL_ID];
                if ( genes.containsKey(geneId) ) {
                    col = genes.get(geneId);
                } else {
                    col = new ArrayList<InterproRow>();
                    genes.put(geneId, col);
                }
                col.add(new InterproRow(ret[i]));
            }
            ret = null;

            Set<String> strangeProgram = new HashSet<String>();
            SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
            Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);
            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            Transaction transaction = session.beginTransaction();
            parse2(genes, col, strangeProgram,session);
            transaction.rollback();
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(session);

            Iterator<String> it = strangeProgram.iterator();
            if ( it.hasNext()) {
                //System.err.println("WARN: Strange program name found in Interpro file: ");
                while (it.hasNext()) {
                    System.err.print(it.next());
                    System.err.print(' ');
                }
                //System.err.print('\n');
            }


        } // if ( ret != null )

    }

    public void afterPropertiesSet() {
        // Deliberately empty
    }

    private void parse2(Map<String, List<InterproRow>> genes, List<InterproRow> col, Set<String> strangeProgram, Session session) {
        // Go through each key and sort the ArrayLists

        Iterator<String> geneIterator = genes.keySet().iterator();
        Feature polypeptide = null;

        while ( geneIterator.hasNext() ) {
            String id = (String) geneIterator.next();
            polypeptide = sequenceDao.getFeatureByUniqueName(id+":pep", "polypeptide");
            if ( polypeptide == null ) {
                System.err.println("WARN: Database doesn't contain id of :" + id);
                continue;
            }
            Hibernate.initialize(polypeptide);
            Set goIdsLinked = new HashSet();
            col = genes.get(id);
            Collections.sort(col);
            boolean swap = true;

            // col is now sorted by interpro, then program
            HashSet<String> ip = new HashSet<String>();
            for ( int i = 0; i < col.size(); i++) {
                String[] a = col.get(i).row;
                String aAccNum = "NULL";
                if ( a.length > COL_ACC) {
                    aAccNum = a[COL_ACC];
                }
                ip.add(aAccNum);
            }
            int max;
            int min;
            Iterator<String> it = ip.iterator();
            while ( it.hasNext()) {
                max = -1;
                min = col.size();
                String ipNum = (String) it.next();
                for ( int i = 0; i < col.size(); i++ ) {
                    String[] a = col.get(i).row;
                    String aAccNum = "NULL";
                    if ( a.length > COL_ACC) {
                        aAccNum = a[COL_ACC];
                    }
                    if ( aAccNum.equals(ipNum)) {
                        if ( i < min) {
                            min = i;
                        }
                        if ( i > max) {
                            max = i;
                        }
                    }
                }
                // Now know upper and lower bound of this interpro num
                List<String> progs = new ArrayList<String>();
                List<String> progAcc = new ArrayList<String>();
                List<String[]> coordIP = new ArrayList<String[]>();
                List<String> scores = new ArrayList<String>();
                for ( int i = min; i <= max ; i++) {
                    String[] thisRow = col.get(i).row;
                    String prog = thisRow[COL_NATIVE_PROG];
                    String db = (String) dbs.get(prog);
                    if ( db == null) {
                        strangeProgram.add(prog);
                    } else {
                        //System.err.println("program is -> " + prog);
                        progs.add(prog);
                        String pAcc = thisRow[COL_NATIVE_ACC];
                        progAcc.add(pAcc);
                        String score = thisRow[8];
                        String description = thisRow[12];
                        scores.add(score + "=" + description);
                        coordIP.add(new String[] {thisRow[6], thisRow[7]});
                    }

                    if (thisRow.length >= COL_GO+1) {
                        //System.err.println("Adding GO terms for "+gene.getId());
                        addGoTerms(polypeptide, thisRow, goIdsLinked,session);
                    }
                }

                processInterProHit(session, polypeptide, ipNum, progs, progAcc,
                        coordIP, scores);

                // Now go thru' individual hits even if InterPro is null
                processIndividualHit(col, strangeProgram, session, polypeptide,
                        max, min, ipNum, progs);
            }

        } // Got all the interpro numbers

    }

    private void processIndividualHit(List<InterproRow> col, Set<String> strangeProgram,
            Session session, Feature polypeptide, int max, int min,
            String ipNum, List<String> progs) {
        Iterator<String> it2 = progs.iterator();
//		int rank2 = 0;
        while ( it2.hasNext() ) {
            int rank = 0;
            String prog = (String) it2.next();
            List<String> coords = new ArrayList<String>();
            List<String[]> coordinates = new ArrayList<String[]>();
            String dbacc = null;
            String nativeProg = null;
            String score = null;
            String desc = null;
            int count = 0;
            for ( int i = min; i <= max ; i++) {
                StringBuffer tmp = new StringBuffer();
                String[] a = col.get(i).row;
                if ( a[3].equals(prog)) {
                    tmp.append( a[6] + "-" + a[7] );
                    if ( count == 0) {
                        tmp.append( " (Score: ");
                    } else {
                        tmp.append(" (");
                    }
                    tmp.append(a[8] + ")");
                    coords.add(tmp.toString());
                    coordinates.add(new String[] {a[6], a[7]});
                    dbacc = a[COL_NATIVE_ACC];
                    nativeProg = a[COL_NATIVE_PROG];
                    score = a[8];
                    desc = a[12];
                    count++;
                }
            }
            StringBuffer note = new StringBuffer("Residue");
            if ( coords.size() > 1 ) {
                note.append("s");
            }
            note.append(": ");
            for ( int i = 0; i < coords.size(); i++) {
                if ( i > 0 && (i != coords.size() - 1)) {
                    note.append(", ");
                }
                if ( (i == coords.size() - 1) && coords.size() != 1) {
                    note.append(" and ");
                }
                note.append( (String) coords.get(i) );
            }
            String db = (String) dbs.get(prog);
            if ( db == null ) {
                strangeProgram.add(prog);
            } else {
                if(ipNum.equals("NULL")) {
                    // Hack for superfamily as InterPro reports acc as SF12345 rather than 12345
                    Feature domain = null;
                    String uniqueName = polypeptide.getUniqueName() + ":" + dbacc;
                    //System.err.println("creating feature with uniquename -> " + uniqueName);
                    List<Feature> features = sequenceDao.getFeaturesByUniqueName(uniqueName + "%");

                    if (features.size() == 0) {
                        //---domain = featureUtils.createFeature("polypeptide_domain", uniqueName, polypeptide.getOrganism());
                    } else {
                        //System.err.println("feature size is in else " + features.size());

                        uniqueName = uniqueName + ":" + (features.size() - 1);
                        /*int num = 0;
                        String name = null;
                        for (Feature feature : features) {
                            name = feature.getUniqueName();
                            if(name.split(":").length > 4) {

                            } else {

                            }
                            int c = (int)name.charAt(name.length()-1);
                            if(c > num) {
                                num = c;
                            }
                        }
                        name = name + ":" + num++;*/
                        //---domain = featureUtils.createFeature("polypeptide_domain", uniqueName, polypeptide.getOrganism());
                    }
                    //Feature domain = featureUtils.createFeature("polypeptide_domain", uniqueName, polypeptide.getOrganism());
                    //---DbXRef dbxref = featureUtils.findOrCreateDbXRefFromString(db + ":" + dbacc);
                    session.flush();
                    //---domain.setDbXRef(dbxref);

                    //---sequenceDao.persist(domain);

                    //---CvTerm scoreTerm = featureUtils.findOrCreateCvTermFromString("null", "score");
                    //---FeatureProp scoreProp = new FeatureProp(domain,scoreTerm,score,0);
                    //---sequenceDao.persist(scoreProp);

                    //---CvTerm description = featureUtils.findOrCreateCvTermFromString("null", "description");
                    //---FeatureProp descProp = new FeatureProp(domain,description,desc,0);
                    //---sequenceDao.persist(descProp);

                    short strand = 0;
                    int start = 0;
                    int end = 0;
                    if ( coordinates.size() > 0) {
                        String[] coord = (String[])coordinates.get(0);
                        start = Integer.parseInt(coord[0]);
                        end = Integer.parseInt(coord[1]);
                    }
                    //System.err.println("creating featureloc with " + polypeptide.getUniqueName() + " " + domain.getUniqueName() + " rank " + rank);
                    //---FeatureLoc floc = featureUtils.createLocation(polypeptide, domain, start, end, strand);
                    //---floc.setRank(rank);
                    //---sequenceDao.persist(floc);
                    rank++;
                    //---Iterator<FeatureLoc> iter = polypeptide.getFeatureLocsForFeatureId().iterator();
                    //---FeatureLoc featureLoc = (FeatureLoc)iter.next();
                    //---Feature parent = featureLoc.getFeatureBySrcFeatureId();
                    //---int start2 = ( start * 3 ) + polypeptide.getFeatureLocsForFeatureId().iterator().next().getFmin();
                    //---int end2 = ( end * 3 ) + polypeptide.getFeatureLocsForFeatureId().iterator().next().getFmin();
                    //System.err.println("creating featureloc with " + parent.getUniqueName() + " " + domain.getUniqueName() + " rank " + rank);
                    //---FeatureLoc floc2 = featureUtils.createLocation(parent,domain, start2, end2, strand);
                    //---floc2.setRank(rank);
                    rank++;
                    //---sequenceDao.persist(floc2);

                    //---session.flush();
                }
            }
        }
    }

    private void processInterProHit(Session session, Feature polypeptide,
            String ipNum, List<String> progs, List<String> progAcc,
            List<String[]> coordIP, List<String> scores) {
        Feature ipDomain = null;
        String secDB = "";
        if ( ipNum.equals("NULL")) {
            return;
        }


        Iterator<String> it2 = progs.iterator();
        int count = 0;
        int rank = 0;
        int rank2 = 0;
        while ( it2.hasNext() ) {
            //StringBuffer note = new StringBuffer();
            //note.append("Derived from hit: ");
            secDB = (String) dbs.get(it2.next());
            //note.append(secDB);
            String uniqueName = polypeptide.getUniqueName() + ":" + ipNum + ":" + progAcc.get(count);
            //sequenceDao.getFeatureByUniqueName(uniqueName, "polypeptide_domain");
            List<Feature> features = sequenceDao.getFeaturesByUniqueName(uniqueName + "%");
            //System.err.println("feature size is " + features.size());
            if (features.size() == 0) {
                //---ipDomain = featureUtils.createFeature("polypeptide_domain", uniqueName, polypeptide.getOrganism());
            } else {
                //System.err.println("feature size is in else " + features.size());

                uniqueName = uniqueName + ":" + (features.size() - 1);
                //---ipDomain = featureUtils.createFeature("polypeptide_domain", uniqueName, polypeptide.getOrganism());
            }
            //---DbXRef dbXRef = featureUtils.findOrCreateDbXRefFromString("InterPro:" + ipNum);

            //---ipDomain.setDbXRef(dbXRef);
            //---sequenceDao.persist(ipDomain);
            System.err.println("looking at -> '" + uniqueName + "' '" +  secDB + "'':''" + progAcc.get(count));
            //---dbXRef = featureUtils.findOrCreateDbXRefFromString(secDB + ":" + progAcc.get(count));
            //---FeatureDbXRef fdbXRef = new FeatureDbXRef();
            //---fdbXRef.setFeature(ipDomain);
            //---fdbXRef.setDbXRef(dbXRef);
            //---sequenceDao.persist(fdbXRef);
            //---CvTerm description = featureUtils.findOrCreateCvTermFromString("feature_property", "description");
            //---FeatureProp fp = new FeatureProp(ipDomain,description,note.toString(),rank2);
            //---sequenceDao.persist(fp);
            rank2++;
            String score = scores.get(count).split("=")[0];
            String desc = scores.get(count).split("=")[1];

            //---CvTerm scoreTerm = featureUtils.findOrCreateCvTermFromString("null", "score");
            //---FeatureProp scoreProp = new FeatureProp(ipDomain,scoreTerm,score,0);
            //---sequenceDao.persist(scoreProp);

            //---FeatureProp descProp = new FeatureProp(ipDomain,description,desc,rank2);
            //---sequenceDao.persist(descProp);

            int start = 0;
            int end = 0;
            short strand = 0;
            if ( coordIP.size() > 0) {
                String[] coord = (String[])coordIP.get(count);
                start = Integer.parseInt(coord[0]);
                end = Integer.parseInt(coord[1]);
            }

            //---FeatureLoc floc = featureUtils.createLocation(polypeptide, ipDomain, start, end, strand);
            //---floc.setRank(rank);
            //---sequenceDao.persist(floc);
            rank++;
            //---Iterator<FeatureLoc> iter = polypeptide.getFeatureLocsForFeatureId().iterator();
            //---FeatureLoc featureLoc = (FeatureLoc)iter.next();
            //---Feature parent = featureLoc.getFeatureBySrcFeatureId();
            //---int start2 = ( start * 3 ) + polypeptide.getFeatureLocsForFeatureId().iterator().next().getFmin();
            //---int end2 = ( end * 3 ) + polypeptide.getFeatureLocsForFeatureId().iterator().next().getFmin();

            //---FeatureLoc floc2 = featureUtils.createLocation(parent,ipDomain, start2, end2, strand);
            //---floc2.setRank(rank);
            rank++;
            //---sequenceDao.persist(floc2);
            count++;
            //---session.flush();
        }
        //System.err.println("Adding Interpro hit for "+gene.getId());


        //GeneUtils.addLink(brna, "InterPro", ipNum, note.toString());
    }


    //Molecular Function: protein kinase (GO:0004672),
    //Molecular Function: ATP binding (GO:0005524),
    //Biological Process: protein amino acid phosphorylation (GO:0006468)
    private void addGoTerms(Feature polypeptide, String[] row, Set<String> goIdsLinked, Session session) {
        String goTerms = row[COL_GO];
        if ( goTerms == null || "".equals(goTerms)) {
            return;
        }
        List<String> terms = new ArrayList<String>();
        int start=0;
        int end=0;
        while (end != -1) {
            int temp = goTerms.indexOf("(GO:", start);
            if (temp == -1) {
                end = -1;
            } else {
                end = goTerms.indexOf(")", temp);
                if ( end != -1) {
                    terms.add(goTerms.substring(start, end+1));
                }
                start = end+1;
            }
        }
        for (int i=0; i < terms.size(); i++) {
            String term = (String) terms.get(i);
            if ( term.startsWith(",")) {
                term = term.substring(1);
            }
            term = term.trim();
            //System.err.println(term);
            int lb = term.indexOf("(GO:");
            int rb = term.indexOf(")", lb);
            String acc = term.substring(lb+4, rb);

            if ( !goIdsLinked.contains(acc)) {
                goIdsLinked.add(acc);
                GoInstance c = new GoInstance();
                c.setId( acc );
                c.setEvidence( GoEvidenceCode.IEA );
                c.setWithFrom( "Interpro:" + row[COL_ACC] );
                c.setRef( "GOC:interpro2go" );
                String[] rawDate = row[10].split("-");
                String month = (String) months.get(rawDate[1]);
                String date = rawDate[2]+month+rawDate[0];
                c.setDate(date);

                int colon = term.indexOf(":");
                String label = term.substring(0, colon);
                String category = null;
                if ("Molecular Function".equals(label)
                    || "function".equals(label)) {
                    category = "function";
                }
                if ("Biological Process".equals(label)
                    || "process".equals(label)) {
                    category = "process";
                }
                if ("Cellular Component".equals(label)
                    || "component".equals(label)) {
                    category = "component";
                }
                c.setSubtype( category );
                //c.setAspect( c.getSubtype().substring(0, 1).toUpperCase() );
                //String name = GoQualifierDictionary.getName( acc );
                c.setName( acc );
                c.setGeneName(polypeptide.getUniqueName().split(":")[0]);

                //---featureUtils.createGoEntries(polypeptide,c);
                session.flush();
            }
        }
    }


    public void setFeatureUtils(FeatureUtils utils) {
        featureUtils = utils;
    }

    public void setHibernateTransactionManager(
            HibernateTransactionManager hibernateTransactionManager) {
        this.hibernateTransactionManager = hibernateTransactionManager;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    class InterproRow implements Comparable<InterproRow> {
        String[] row;

        InterproRow(String[] row) {
            this.row = row;
        }

        public int compareTo(InterproRow o) {
            String aAccNum = "NULL";
            String bAccNum = "NULL";
            if ( row.length > COL_ACC) {
                aAccNum = row[COL_ACC];
            }
            if ( o.row.length > COL_ACC) {
                bAccNum = o.row[COL_ACC];
            }
            int cmp = aAccNum.compareTo(bAccNum);
            if ( cmp != 0 ) {
                return cmp;
            }

            return (row[COL_NATIVE_PROG].compareTo(o.row[COL_NATIVE_PROG]));
        }

    }

    public static void main(String[] args) {
        InterProParser ipp = new InterProParser();
        ipp.parse("/nfs/team81/art/chr1merged.raw");

    }

}
