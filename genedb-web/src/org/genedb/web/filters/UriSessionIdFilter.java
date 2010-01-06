package org.genedb.web.filters;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.tags.UrlTag;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

public class UriSessionIdFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// clear session if session id in URL
		if (httpRequest.isRequestedSessionIdFromURL()) {
			HttpSession session = httpRequest.getSession();
			if (session != null) {
				session.invalidate();
			}
		}

		// wrap response to remove URL encoding
		HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse) {
			@Override
			public String encodeRedirectUrl(String url) { return url; }

			@Override
			public String encodeRedirectURL(String url) { return url; }

			@Override
			public String encodeUrl(String url) { return url; }

			@Override
			public String encodeURL(String url) { return url; }
		};

		chain.doFilter(request, wrappedResponse);
	}


	@Override
	public void destroy() {
		// Deliberately empty
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// Deliberately empty
	}

}
