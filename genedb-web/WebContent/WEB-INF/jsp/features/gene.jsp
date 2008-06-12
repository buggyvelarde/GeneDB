<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />
<c:url value="/" var="base"/>

<format:headerRound name="Gene: ${gene.displayName}" title="Gene Page ${gene.displayName}" bodyClass="genePage"
onLoad="initContextMap('${base}', '${gene.organism.commonName}', '${chromosome.uniqueName}', ${chromosome.seqLen}, ${primaryLoc.fmin}, ${primaryLoc.fmax}, '${transcript.uniqueName}');">

<st:init />
<%-- The next three are used by the scrollable context map --%>
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/jquery-1.2.6.min.js"/>"></script>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/interface-1.2/ifx.js"/>"></script>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/interface-1.2/ifxhighlight.js"/>"></script>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery/jquery.history.js"/>"></script>
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
</format:headerRound>
<!-- Context Map -->
<div id="contextMapOuterDiv">
    <div id="contextMapTopPanel">
        <div id="contextMapThumbnailDiv"></div>
    </div>
    <div id="contextMapDiv">
        <div id="contextMapLoading">
            <img src="<c:url value="/includes/images/default/grid/loading.gif"/>" id="contextMapLoadingImage">
            Loading...
        </div>
        <div id="contextMapContent">
            <div id="highlighter"></div>
        </div>
    </div>
    <div id="contextMapInfoPanel">
        <div class="closeButton"><a href="#"></a></div>
        <div id="loadDetails"><a href="#">Load details »</a></div>
        <div class="value" id="selectedGeneName"></div>
        <div class="value" id="selectedGeneProducts"></div>
    </div>
</div>

<format:genePageSection id="geneDetailsLoading" className="greyBox">
    <img src="<c:url value="/includes/images/default/grid/loading.gif"/>">
    Loading Gene Details...
</format:genePageSection>
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>
<format:footer />