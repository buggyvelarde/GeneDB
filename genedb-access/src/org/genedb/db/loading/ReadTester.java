package org.genedb.db.loading;

import org.genedb.db.hibernate3gen.DbXRef;
import org.genedb.db.hibernate3gen.FeatureDbXRef;
import org.genedb.db.hibernate3gen.FeatureProp;
import org.genedb.db.hibernate3gen.FeatureSynonym;
import org.genedb.db.hibernate3gen.Organism;
import org.genedb.db.hibernate3gen.Pub;
import org.genedb.db.jpa.Feature;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Set;

public class ReadTester {

    public static void main(String[] args) {
        ReadTester test = new ReadTester();

        //testJdbcConnection();
        String uniqueName = "MAL8P1.162";
        if (args.length > 0) {
        	uniqueName = args[0];
        }
        
        test.displayDetails(uniqueName);

        HibernateUtil.getSessionFactory().close();
    }



    @SuppressWarnings("unchecked")
    private void displayDetails(String uniqueName) {

    	SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();
        
//        List<Cv> cvs = (List<Cv>) session.createQuery("from Cv as cv").list();
//        for (Cv cv : cvs) {
//			System.err.println(cv.getName()+"==="+cv.getDefinition());
//		}
        Query q = session.createQuery("select feature from Feature as feature where feature.uniquename= :uniqueName");
        q.setString("uniqueName", uniqueName);
        q.setMaxResults(20);
        List<Feature> features = q.list();
        for (Feature feature: features) {
        	System.err.println("=== "+feature.getName()+" ===");
        	
        	// Type
        	System.err.println("Type: "+feature.getCvTerm().getName());
        	
        	// Analysis
        	System.err.println("Analysis Feature: "+feature.isAnalysis());
        	
        	// Obsolete
        	System.err.println("Obsolete?: "+feature.isObsolete());
        	
        	// Timestamps
        	System.err.println("Date created: "+feature.getTimeAccessioned()+"     Date last modified:"+feature.getTimeLastModified());
        	
        	// Organism
        	Organism org = feature.getOrganism();
        	System.err.print("Organism: "+org.getGenus() + ' ' + org.getSpecies());
        	System.err.println("     Common name: "+org.getCommonName());
        	
        	// Synonyms
			System.err.println("Synonyms:");
			Set<FeatureSynonym> synonyms = feature.getFeatureSynonyms();
			for (FeatureSynonym featSyn : synonyms) {
				System.err.println("\t"+featSyn.getSynonym().getCvterm().getName()+"="+featSyn.getSynonym().getName()+"   pub="+showPublication(featSyn.getPub())+"  isCurrent="+featSyn.isCurrent()+" isInternal="+featSyn.isInternal());
			}
        	
			// Properties
			System.err.println("Feature properties:");
			Set<FeatureProp> props = feature.getFeatureProps();
			for (FeatureProp prop : props) {
				System.err.println("\t"+prop.getRank()+"   "+prop.getCvterm().getName()+"="+prop.getValue());
			}
			
			DbXRef oneXref = feature.getDbxref();
			if (oneXref != null) {
				System.err.println("Xref: "+oneXref.getDb().getName()+":"+oneXref.getAccession()+" : "+oneXref.getDescription());
			}
			
			System.err.println("Db xrefs:");
			for (FeatureDbXRef fdx : feature.getFeatureDbxrefs()) {
				System.err.println("\t"+fdx.getDbxref().getDb().getName()+":"+fdx.getDbxref().getAccession()+" : "+fdx.getDbxref().getDescription()+"  : isCurrent:"+fdx.isCurrent());
			}
			
			
			
		}
        
        System.err.println("======");
        
        session.close();
    }

    private String showPublication(Pub pub) {
    	StringBuilder ret = new StringBuilder("Publication: ");
    	if (pub == null) {
    		ret.append("None");
    	} else {
    		ret.append(pub.getTitle());
    	}
    	return ret.toString();
    }
    
//	private static void testJdbcConnection() {
//		Connection connection = null;
//        try {
//            // Load the JDBC driver
//            String driverName = "org.postgresql.Driver";
//            Class.forName(driverName);
//        
//            // Create a connection to the database
//            String serverName = "holly.internal.sanger.ac.uk";
//            String portNumber = "5432";
//            String sid = "chado";
//            String url = "jdbc:postgresql://" + serverName + ":" + portNumber + "/" + sid;
//            String username = "tgambiense";
//            String password = "";
//            connection = DriverManager.getConnection(url, username, password);
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM cv");
//            while (rs.next()) {
//            	System.err.println(rs.getString("name"));
//            }
//            rs.close();
//            stmt.close();
//            connection.close();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//	}
    
}