<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:headerRound title="Feature Download" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="featureDownload">
	<format:genePageSection className="whiteBox">
		<pre><FONT size="4"><c:out value="${sequence}"/></FONT></pre>
	</format:genePageSection>
</div>
<format:footer />