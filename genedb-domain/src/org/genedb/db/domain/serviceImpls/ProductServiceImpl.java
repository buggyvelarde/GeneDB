package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.db.dao.CvDao;
import org.genedb.db.domain.misc.GeneListReservations;
import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Repository
@Transactional
public class ProductServiceImpl implements ProductService {

	private Logger semantic = Logger.getLogger("semanticLogger");
	private GeneListReservations geneListReservations;
	private SessionFactory sessionFactory;
	private CvDao cvDao;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	@Override
	@Transactional
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> oldProducts) {
		//Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		List<String> problems = new ArrayList<String>();
		checkProduct(newProduct, problems);
		for (Product p : oldProducts) {
			checkProduct(p, problems);
		}
		if (problems.size() > 0) {
			//session.close();
			return new MethodResult(StringUtils.collectionToCommaDelimitedString(problems));
		}

		semantic.info("New product rationalisation");
		//CvTerm newCvTerm = cvDao.getCvTermById(newProduct.getId());
		CvTerm nct = (CvTerm) sessionFactory.getCurrentSession()
		.createQuery("from CvTerm cvt where cvt.id = ?").setInteger(0, newProduct.getId()).uniqueResult();	
		
		
		for (Product p : oldProducts) {
			CvTerm oldProduct = (CvTerm) sessionFactory.getCurrentSession()
			.createQuery("from CvTerm cvt where cvt.id = ?").setInteger(0, newProduct.getId()).uniqueResult();	
			changeProductInFeatureCvTerms(p, nct);
			deleteProduct(oldProduct);
		}
		
		//session.close();
		if (problems.size() > 0) {
			return new MethodResult(StringUtils.collectionToCommaDelimitedString(problems));
		}
		// Remove product itself
		
		return MethodResult.SUCCESS;
	}


	@SuppressWarnings("unchecked")
	@Transactional
	private void changeProductInFeatureCvTerms(Product p, CvTerm nct) {
		List<FeatureCvTerm> fcts = sessionFactory.getCurrentSession().createQuery("select fct" +
		        " from CvTerm cvt,FeatureCvTerm fct, Feature f" +
				" where f=fct.feature and cvt=fct.cvTerm and cvt.id="+p.getId()).list();
		// FIXME Check for locks
		System.err.println("Found '"+fcts.size()+"' fcts for the product '"+p.toString()+"'");
		for (FeatureCvTerm fct : fcts) {
			System.err.println("Found a fct '"+fct+"' for product '"+nct.getName()+"'");
			fct.setCvTerm(nct);
			
//				cvDao.update(fct);
			//int count = session.createQuery(
			//		"update FeatureCvTerm fct set fct.cvtTerm.id="+p.getId()+" where fct.feature.uniqueName="+geneName").executeUpdate();
			//if (count != 1) {
			//	problems.add("Unable to update product for '"+geneName+"'");
			//}
		}
	}


	private void deleteProduct(CvTerm p) {
		sessionFactory.getCurrentSession().delete(p);
//		if (del != 1) {
//			problems.add("Tried to delete '"+p.toString()+"' but affected '"+del+"' rows");
//		}
	}

	
	private void checkProduct(Product newProduct, List<String> problems) {
		// TODO Auto-generated method stub
		return;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Product> getProductList(boolean restrictToGeneLinked) {
		//Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		Query q;
		if (restrictToGeneLinked) {
//			q = sessionFactory.getCurrentSession().createQuery("select distinct new org.genedb.db.domain.objects.Product(cvt.name, cvt.id)" +
//					" from CvTerm cvt, FeatureCvTerm fct" +
//			" where cvt=fct.cvTerm and cvt.cv.name='genedb_products' order by cvt.name");
			q = sessionFactory.getCurrentSession().createQuery("select distinct new org.genedb.db.domain.objects.Product(cvt.name, cvt.id)" +
					" from CvTerm cvt, FeatureCvTerm fct" +
			" where cvt=fct.cvTerm and cvt.cv.name='genedb_products' order by cvt.name");
		} else {
			q = sessionFactory.getCurrentSession().createQuery("select distinct new org.genedb.db.domain.objects.Product(cvt.name, cvt.id)" +
					" from CvTerm cvt" +
			" where cvt.cv.name='genedb_products' order by cvt.name");
		}
			List<Product> products = (List<Product>) q.list();
		//session.close();
		return products;
	}

	public void setGeneListReservations(GeneListReservations geneListReservations) {
		this.geneListReservations = geneListReservations;
	}

	public void setCvDao(CvDao cvDao) {
		this.cvDao = cvDao;
	}

}
