package org.genedb.query;

import org.springframework.beans.factory.annotation.Required;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

import com.sun.corba.se.spi.orbutil.fsm.Input;

public class QueryByURI implements Query {

	private URI uri;
	private List<String> results = null;
	
	public Object getResults() throws MalformedURLException, IOException {
		if (results != null) {
			return results;
		}
		// Fetch from URI
		URLConnection connection = uri.toURL().openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			results.add(line);
		}
		return null;
	}


	public URI getUri() {
		return uri;
	}

	@Required
	public void setUri(URI uri) {
		this.uri = uri;
	}

}
