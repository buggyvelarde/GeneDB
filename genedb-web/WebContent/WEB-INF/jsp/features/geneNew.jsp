<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<c:url value="/" var="base"/>

<c:set var="primaryLoc" value="${feature.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />

<format:headerRound name="Gene: ${feature.displayName}" title="Gene Page ${feature.displayName}"
		onLoad="initContextMap('${base}', '${feature.organism.commonName}', 'Pf3D7_02', '${feature.uniqueName}')">
	<base href="<c:url value="/"/>">
	<st:init />
	<%-- The next two are used by the scrollable context map --%>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/contextMap.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
</format:headerRound>
<div id="contextMapDiv">
				<img src="<c:url value="/includes/images/default/grid/loading.gif"/>" id="contextMapLoadingImage">
</div>
<div id="genInfo">
<format:geninfo f1="${polypeptide}" f2="${feature}"/>
</div>
<div id="controlCur" style="clear: both;">
<format:controlledCur c1="${polypeptide}"/>
</div>
<div id="go" style="clear: both;">
<format:go g1="${polypeptide}"/>
</div>
<div id="predictedpep" style="clear: both;">
<format:predictedpep p1="${polyprop}"/>
</div>
<div id="domainInfo" style="clear: both;">
</div>
<div id="orthologs" style="clear: both;">
<format:orthologs o1="${polypeptide}"/>
</div>
<format:footer />