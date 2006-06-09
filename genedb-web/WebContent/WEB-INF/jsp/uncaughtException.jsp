<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header>Internal Error</format:header>
<P>

<% 
try {
	// The Servlet spec guarantees this attribute will be available
	Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception"); 

	if (exception != null) {
		Throwable rootCause = exception.getCause();
    		if (rootCause != null) {
			rootCause.printStackTrace(new java.io.PrintWriter(out)); 
		} else {
			exception.printStackTrace(new java.io.PrintWriter(out)); 
		}
	} else  {
        	out.println("No error information available");
	} 

	// Display cookies
	out.println("\nCookies:\n");
	Cookie[] cookies = request.getCookies();
	if (cookies != null) {
    	for (int i = 0; i < cookies.length; i++) {
      		out.println(cookies[i].getName() + "=[" + cookies[i].getValue() + "]");
		}
	}
	    
} catch (Exception ex) { 
	ex.printStackTrace(new java.io.PrintWriter(out));
}
%>

<P>
<BR>


<format:footer />
