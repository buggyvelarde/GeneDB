<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:header name="Download File">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

Click <a href="<c:url value="/"/>includes/excel/${file}">Here</a> to download the ${format} file.

<br>
<br>

Back to <a href="<c:url value="/"/>History/View">History View</a>