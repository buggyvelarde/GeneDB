package org.genedb.query.custom;

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.NumericQueryVisibility;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenesByURI implements Query {

    private static final String NAME = "GenesByURI";
    private URI uri;
    private List<String> results = null;

    public List<String> getResults() throws QueryException {
        if (results != null) {
            return results;
        }
        // Fetch from URI
        BufferedReader br = null;
        try {
            URLConnection connection = uri.toURL().openConnection();
            InputStream is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                results.add(line);
            }
        } catch (IOException exp) {
            throw new QueryException(exp);
        } finally {
            IOUtils.closeQuietly(br);
        }
            return null;
    }


    public URI getUri() {
        return uri;
    }

    @Required
    public void setUri(URI uri) {
        this.uri = uri.normalize();
    }


    public String getParseableDescription() {
        // TODO Auto-generated method stub
        return NAME + "{uri=\""+uri.toASCIIString()+"\"}";
    }


    public String[] prepareModelData(int count) {
        return null;
    }


    @Override
    public Map<String, Object> prepareModelData() {
        return Collections.emptyMap();
    }


    public int getOrder() {
        return 0;
    }


    public String getQueryDescription() {
        return "Fetch IDs by URL";
    }

    @Override
    public void validate(Object target, Errors errors) {
        return;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return GenesByURI.class.isAssignableFrom(clazz);
    }


    @Override
    public boolean isMaxResultsReached() {
        // Not Implemented
        return false;
    }


    @Override
    public String getQueryName() {
        return "Genes By URI";
    }
    
}
