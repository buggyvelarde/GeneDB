package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Repository
public class ProductServiceImpl implements ProductService {

	private GeneListReservations geneListReservations;
	private SessionFactory sessionFactory;
	private CvDao cvDao;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> oldProducts) {
		Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		List<String> problems = new ArrayList<String>();
		checkProduct(newProduct, problems);
		for (Product p : oldProducts) {
			checkProduct(p, problems);
		}
		if (problems.size() > 0) {
			session.close();
			return new MethodResult(StringUtils.collectionToCommaDelimitedString(problems));
		}

		CvTerm newCvTerm = cvDao.getCvTermById(newProduct.getId());
		
		for (Product p : oldProducts) {
			List<FeatureCvTerm> fcts = session.createQuery("select fct" +
			        " from CvTerm cvt,FeatureCvTerm fct, Feature f" +
					" where f=fct.feature and cvt=fct.cvTerm and cvt.id="+p.getId()).list();
			// FIXME Check for locks
			for (FeatureCvTerm fct : fcts) {
				System.err.println("Found a fct '"+fct+"' for product '"+newCvTerm.getName()+"'");
				//fct.setCvTerm(newCvTerm);
//				cvDao.update(fct);
				//int count = session.createQuery(
				//		"update FeatureCvTerm fct set fct.cvtTerm.id="+p.getId()+" where fct.feature.uniqueName="+geneName").executeUpdate();
				//if (count != 1) {
				//	problems.add("Unable to update product for '"+geneName+"'");
				//}
			}
		}
		
		session.close();
		if (problems.size() > 0) {
			return new MethodResult(StringUtils.collectionToCommaDelimitedString(problems));
		}
		return MethodResult.SUCCESS;
	}

	
	private void checkProduct(Product newProduct, List<String> problems) {
		// TODO Auto-generated method stub
		return;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Product> getProductList() {
		Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		Query q = session.createQuery("select distinct new org.genedb.db.domain.objects.Product(cvt.name, cvt.id)" +
        " from CvTerm cvt, FeatureCvTerm fct" +
		" where cvt=fct.cvTerm and cvt.cv.name='genedb_products' order by cvt.name");
		List<Product> products = (List<Product>) q.list();
		session.close();
		return products;
	}

	public void setGeneListReservations(GeneListReservations geneListReservations) {
		this.geneListReservations = geneListReservations;
	}

	public void setCvDao(CvDao cvDao) {
		this.cvDao = cvDao;
	}

}
