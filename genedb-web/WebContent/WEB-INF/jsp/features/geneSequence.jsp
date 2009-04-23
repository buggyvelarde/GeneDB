<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.sourceFeature}" />
<c:url value="/" var="base"/>

<format:headerRound name="${gene.organism.htmlShortName}" organism="${gene.organism.commonName}" title="Gene Sequences ${gene.displayName}" bodyClass="genePage">

<st:init />
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
<%-- Here we put those styles that contain URLs --%>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
</format:headerRound>

    <c:if test="${!empty protein}">
    <a href="">Send to GeneDB omniBLAST</a>    <a href="">Send to GeneDB BLAST</a>    <a href="">Send to BLAST at NCBI</a>
    <format:genePageSection id="proteinSequence">
        <div class="heading">Protein</div>
        <misc:format-sequence sequence="${protein}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty spliced && fn:length(coords) > 1}">
    <a href="">Send to GeneDB omniBLAST</a>    <a href="">Send to GeneDB BLAST</a>    <a href="">Send to BLAST at NCBI</a>
    <format:genePageSection id="splicedSequence">
        <div class="heading">Spliced</div>
        <misc:format-sequence sequence="${spliced}"/>
    </format:genePageSection>
    </c:if>

    <c:if test="${!empty unspliced}">
    <a href="">Send to GeneDB omniBLAST</a>    <a href="">Send to GeneDB BLAST</a>    <a href="">Send to BLAST at NCBI</a>
    <format:genePageSection id="unsplicedSequence">
        <div class="heading">Unspliced</div>
        <misc:format-sequence sequence="${unspliced}" includeSpaces="true" />
    </format:genePageSection>
    </c:if>

<format:footer />