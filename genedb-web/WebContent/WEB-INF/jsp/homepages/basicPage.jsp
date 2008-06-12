<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />
<c:url value="/" var="base"/>

<format:headerRound name="Organism: GeneDB" title="Organism: GeneDB" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>


<format:genePageSection id="geneDetailsLoading" className="greyBox">
    Some blurb here
</format:genePageSection>
<format:footer />