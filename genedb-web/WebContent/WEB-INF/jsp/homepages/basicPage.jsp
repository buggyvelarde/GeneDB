<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<format:headerRound name="Organism: GeneDB" title="Organism: GeneDB" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>



<p></p>
<format:genePageSection className="greyBox">
    Some blurb here
</format:genePageSection>
<p></p>

<format:footer />