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

    <!--
    <format:genePageSection id="sequence1">
        <div class="heading">Unspliced</div>
        <div class="comment">ACCTGTGACTGTACGTGATHISISJUNK</div>
    </format:genePageSection>
    -->

    <format:genePageSection id="splicedSequence">
        <div class="heading">Spliced</div>
        <div class="comment">${transcript.residues}</div>
    </format:genePageSection>

    <format:genePageSection id="proteinSequence">
        <div class="heading">Protein</div>
        <div class="comment">${polypeptide.residues}</div>
    </format:genePageSection>

<format:footer />