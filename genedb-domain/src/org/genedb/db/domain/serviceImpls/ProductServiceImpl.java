package org.genedb.db.domain.serviceImpls;

import java.util.ArrayList;
import java.util.List;

import org.genedb.db.domain.misc.GeneListReservations;
import org.genedb.db.domain.misc.MethodResult;
import org.genedb.db.domain.objects.Product;
import org.genedb.db.domain.services.ProductService;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ProductServiceImpl implements ProductService {

	private List<Product> products = new ArrayList<Product>();
	private GeneListReservations geneListReservations;
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public ProductServiceImpl() {
		products.add(new Product("peri", 1));
		products.add(new Product("rose", 2));
		products.add(new Product("donna", 3));
		products.add(new Product("martha", 4));
		products.add(new Product("susan", 5));
		products.add(new Product("sarah-jane", 6));
		products.add(new Product("leela", 7));
		products.add(new Product("romanadvertrelundar", 8));
		products.add(new Product("tegan", 9));
		products.add(new Product("nyssa", 10));
		products.add(new Product("mel", 11));
		products.add(new Product("ace", 12));
		products.add(new Product("jo", 13));
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public MethodResult rationaliseProduct(Product newProduct,
			List<Product> oldProducts) {
		
		List<String> problems = new ArrayList<String>();
		checkProduct(newProduct, problems);
		for (Product p : oldProducts) {
			checkProduct(p, problems);
		}
		if (problems.size() > 0) {
			return new MethodResult(StringUtils.collectionToCommaDelimitedString(problems));
		}
		
		for (Product p : oldProducts) {
			List<String> genes = sessionFactory.getCurrentSession().createQuery("select f.uniqueName" +
			        " from CvTerm cvt,FeatureCvTerm fct, Feature f" +
					"where f=fct.feature and cvt=fct.cvTerm and cvt.id="+p.getId()).list();
			// FIXME Check for locks
			for (String geneName : genes) {
				int count = sessionFactory.getCurrentSession().createQuery(
						"update FeatureCvTerm fct set fct.cvtTerm.id="+p.getId()+" where fct.feature.uniqueName= :geneName").executeUpdate();
				if (count != 1) {
					problems.add("Unable to update product for '"+geneName+"'");
				}
			}
		}
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
		return sessionFactory.getCurrentSession().createQuery("select new Product(cvt.name,cvt.id)" +
        " from CvTerm cvt,FeatureCvTerm fct" +
		"where cvt=fct.cvTerm and cvt.cv.name='genedb_products' group by cvt.name").list();
	}

	public void setGeneListReservations(GeneListReservations geneListReservations) {
		this.geneListReservations = geneListReservations;
	}

}
