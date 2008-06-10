<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="base"/>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />

<format:headerRound name="Gene: ${gene.displayName}" title="Gene Page ${gene.displayName}" bodyClass="genePage"
onLoad="initContextMap('${base}', '${gene.organism.commonName}', '${chromosome.uniqueName}', ${chromosome.seqLen}, ${primaryLoc.fmin}, ${primaryLoc.fmax});">

<st:init />
<%-- The next three are used by the scrollable context map --%>
<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/contextMap.css"/>" />
<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/jquery-1.2.6.min.js"/>"></script>
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
        <img class="upButton" src="<c:url value="/includes/images/close-button.png"/>">
        <div class="label">Gene:</div><div class="value" id="selectedGeneName"></div>
        <div class="label">Products:</div><div class="value" id="selectedGeneProducts"></div>
        <div class="label">Location:</div><div class="value" id="selectedGeneLocation"></div>
    </div>
</div>

<div id="geneDetails">
    <div id="firstRow" class="row">
        <!-- General Information -->
        <format:genePageSection id="generalInformation">
            <div class="heading">General Information</div>
            <table>
            <col style="width: 9em;">
            <c:if test="${gene.name != null}">
                <tr>
                    <td class="label">Gene Name</td>
                    <td class="value">${gene.name}</td>
                </tr>
             </c:if>
             <tr>
                <td class="label">Systematic Name</td>
                <td class="value">${gene.uniqueName}</td>
            </tr>
            <db:synonym name="obsolete_name" var="name" collection="${gene.featureSynonyms}">
                <tr>
                    <th>Previous IDs</th>
                    <td><db:list-string collection="${name}" /></td>
                </tr>
            </db:synonym>
            <db:synonym name="synonym" var="name" collection="${gene.featureSynonyms}">
                 <tr>
                    <td class="label">Synonyms</td>
                    <td class="value"><db:list-string collection="${name}" /></td>
                 </tr>
            </db:synonym>
            <tr>
                <td class="label">Protein names</td>
                <td class="value">
                    <c:forEach items="${polypeptide.featureCvTerms}" var="featCvTerm">
                        <c:if test="${featCvTerm.cvTerm.cv.name == 'genedb_products'}">
                            <span>${featCvTerm.cvTerm.name}</span><br>
                        </c:if>
                    </c:forEach>
                </td>
            </tr>
            <tr>
                <td class="label">Location</td>
                <td class="value">
                    <c:forEach items="${gene.featureLocsForFeatureId}" var="featLoc">
                        <c:set var="start" value="${featLoc.fmin}" />
                        <c:set var="end" value="${featLoc.fmax}" />
                        ${start}..${end},
                    </c:forEach>
                    chromosome ${chromosome.displayName}
                </td>
            </tr>
            <tr>
            	<td class="label">See Also</td>
            	<td class="value">
            		PlasmoDB , TDRtargets
            	</td>
            </tr>
            </table>
        </format:genePageSection>
        
        <format:genePageSection id="analysisTools" className="whiteBox">
            <div class="heading">Analysis tools</div>
            <form name="downloadRegion" action="SequenceDownload">
                <div>Download Region</div>
                as
                <select name="downloadType">
                    <option value="fasta">FASTA</option>
                    <option value="embl">EMBL</option>
                </select>
                <input type="hidden" name="featureType" value="<c:out value="${chromosome.cvTerm.name}" />">
                <input type="hidden" name="topLevelFeature" value="<c:out value="${chromosome.uniqueName}" />">
                <input type="submit" name="Submit">
            </form>
            <div style="margin-top: 1ex;">
            	<a href="ArtemisLaunch?organism=${gene.organism.commonName}&chromosome=${chromosome.uniqueName}&start=${primaryLoc.fmin}&end=${primaryLoc.fmax}">Show region in Artemis</a>
            </div>
        </format:genePageSection>
    </div>
    
    <!-- Comments Section -->
    <db:propByName items="${polypeptide.featureProps}" cvTerm="comment" var="comment"/>
    <c:if test="${fn:length(comment) > 0}">
		<format:genePageSection id="comment">
			<div class="heading">Comments</div>
			<table width="100%">
	   		<db:filtered-loop items="${polypeptide.featureProps}" cvTerm="comment" var="comment" varStatus="status">
		   		<tr>
		   			<td>${comment.value}</td>
		   		</tr>
	    	</db:filtered-loop>
	    	</table>
		</format:genePageSection>
	</c:if>
    
    <!-- Controlled Curation Section -->
    <db:propByName items="${polypeptide.featureCvTerms}" cv="CC_genedb_controlledcuration" var="controlledCurationTerms"/>
    <c:if test="${fn:length(controlledCurationTerms) > 0}">
        <format:genePageSection id="controlCur">
            <div class="heading">Controlled Curation</div>
            <table width="100%" class="go-section">
                <format:go-section featureCvTerms="${controlledCurationTerms}"/>
            </table>
        </format:genePageSection>
    </c:if>
    
    <!-- Gene Ontology Section -->
    <db:propByName items="${polypeptide.featureCvTerms}" cv="biological_process" var="biologicalProcessTerms"/>
    <db:propByName items="${polypeptide.featureCvTerms}" cv="molecular_function" var="molecularFunctionTerms"/>
    <db:propByName items="${polypeptide.featureCvTerms}" cv="cellular_component" var="cellularComponentTerms"/>
    <c:if test="${fn:length(biologicalProcessTerms) + fn:length(molecularFunctionTerms) + fn:length(cellularComponentTerms) > 0}">
        <format:genePageSection id="geneOntology">
            <div class="heading">Gene Ontology</div>
            <table width="100%" class="go-section">
                <format:go-section title="Biological Process" featureCvTerms="${biologicalProcessTerms}"/>
                <format:go-section title="Cellular Component" featureCvTerms="${cellularComponentTerms}" />
                <format:go-section title="Molecular Function" featureCvTerms="${molecularFunctionTerms}" />
            </table>
        </format:genePageSection>
    </c:if>
    
    <!-- Predicted Peptide Section -->
    <div id="peptideRow" class="row">
        <format:genePageSection id="peptideProperties">
            <div class="heading">Predicted Peptide Properties</div>
            <table>
            <tr>
                <td class="label">Isoelectric Point</td>
                <td class="value">pH ${polyprop.isoelectricPoint}</td>
            </tr>
            <tr>
                <td class="label">Mass</td>
                <td class="value">${polyprop.mass} kDa</td>
            </tr>
            <tr>
                <td class="label">Charge</td>
                <td class="value">${polyprop.charge}</td>
            </tr>
            <tr>
                <td class="label">Amino Acids</td>
                <td class="value">${polyprop.aminoAcids}</td>
            </tr>
            </table>
        </format:genePageSection>
        
        <format:genePageSection id="proteinMap" className="whiteBox">
            <div class="heading">Protein Map</div>
            <div align="center">
                <img src="<c:url value="/includes/images/protein.gif"/>" id="ProteinMap">
            </div>
        </format:genePageSection>
    </div>
    
    <!-- Domain Information -->
    <div id="domainInfo" style="clear: both;">
    </div>
    
    <!-- Ortholog / Paralog Section -->
    <format:genePageSection id="orthologs">
        <div class="heading">Orthologs / Paralogs</div>
        <db:ortholog polypeptide="${polypeptide}"/>
    </format:genePageSection>
</div>
<format:footer />