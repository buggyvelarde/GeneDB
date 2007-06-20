package org.genedb.query;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Required;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

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

}
