<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header name="${tn.shortName} Homepage"/>

First we have '${tn}'

Then we'll try another tag
:
<db:breadcrumb />
:

<misc:debug />
<format:footer />
