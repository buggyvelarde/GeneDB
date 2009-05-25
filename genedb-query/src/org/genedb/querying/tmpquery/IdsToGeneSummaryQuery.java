package org.genedb.querying.tmpquery;

import org.genedb.querying.core.LuceneQuery;
import org.genedb.querying.core.QueryException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.validation.Errors;

import java.util.List;

public class IdsToGeneSummaryQuery extends LuceneQuery {

    private List<String> ids;

//    @Override
//    protected <T> T convertDocumentToReturnType(Document document, Class<T> clazz) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    protected GeneSummary convertDocumentToReturnType(Document document) {
        GeneSummary ret = new GeneSummary(
                document.get("uniqueName"), // systematic
                document.get("organism.commonName"), // taxon-name,
                document.get("product"), // product
                document.get("chr"), // toplevename
                Integer.parseInt(document.get("start")) // leftpos
                );
        return ret;
    }

    @Override
    protected void extraValidation(Errors errors) {
        // Deliberately empty
    }

    @Override
    protected String[] getParamNames() {
        return new String[] {"ids"};
    }

    @Override
    protected void getQueryTerms(List<org.apache.lucene.search.Query> queries) {
        BooleanQuery bq = new BooleanQuery();
        for(String id : ids) {
            bq.add(new TermQuery(new Term("uniqueName",id)), Occur.SHOULD);
        }

        queries.add(bq);
        //queries.add(geneQuery);
    }

    @Override
    protected String getluceneIndexName() {
        return "org.gmod.schema.mapped.Feature";
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public List getResults() throws QueryException {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
//    public boolean supports(Class<?> arg0) {
//        // TODO Auto-generated method stub
//        return false;
//    }

}
