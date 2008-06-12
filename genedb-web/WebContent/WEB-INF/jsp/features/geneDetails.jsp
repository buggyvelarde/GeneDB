<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />

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
            <td class="value">
                ${gene.uniqueName}<c:if test="${fn:length(gene.transcripts) > 1}">
                    (Transcript ${transcript.uniqueName})
                </c:if>
            </td>
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
        <c:if test="${!empty(polypeptide.products)}">
            <tr>
                <td class="label">Protein names</td>
                <td class="value">
                    <c:forEach items="${polypeptide.products}" var="product">
                        <span>${product}</span><br>
                    </c:forEach>
                </td>
            </tr>
        </c:if>
        <tr>
            <td class="label">Location</td>
            <td class="value">
                ${transcript.exonLocsTraditional},
                chromosome ${chromosome.displayName}
            </td>
        </tr>
        <c:if test="${!empty(polypeptide.featureDbXRefs)}">
	        <tr>
	            <td class="label">See Also</td>
	            <td class="value">
	                <c:forEach items="${polypeptide.featureDbXRefs}" var="fdbxref">
	                	<c:if test="${fn:length(fdbxref.dbXRef.db.urlPrefix) > 0}">
	                		<span><a href="${fdbxref.dbXRef.db.urlPrefix}${fdbxref.dbXRef.accession}">${fdbxref.dbXRef.db.name}</a></span>
	                	</c:if>
	                </c:forEach>
	            </td>
	        </tr>
	    </c:if>
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
        <div style="clear: both; margin-top: 1ex;">
            <a href="ArtemisLaunch?organism=${gene.organism.commonName}&chromosome=${chromosome.uniqueName}&start=${primaryLoc.fmin}&end=${primaryLoc.fmax}">Show region in Artemis</a>
        </div>
    </format:genePageSection>
</div>

<c:if test="${polypeptide != null}">

    <!-- Comments Section -->
    <db:propByName items="${polypeptide.featureProps}" cvTerm="comment" var="comment"/>
    <c:if test="${fn:length(comment) > 0}">
        <format:genePageSection id="comment">
            <div class="heading">Comments</div>
            <db:filtered-loop items="${polypeptide.featureProps}" cvTerm="comment" var="comment" varStatus="status">
                <div class="comment">${comment.value}</div>
            </db:filtered-loop>
        </format:genePageSection>
    </c:if>

    <!-- Curation Section -->
    <db:propByName items="${polypeptide.featureProps}" cvTerm="curation" var="curation"/>
    <c:if test="${fn:length(curation) > 0}">
        <format:genePageSection id="curation">
            <div class="heading">Curation</div>
            <db:filtered-loop items="${polypeptide.featureProps}" cvTerm="curation" var="curation" varStatus="status">
                <div class="comment">${curation.value}</div>
            </db:filtered-loop>
        </format:genePageSection>
    </c:if>

    <!-- Controlled Curation Section -->
    <db:propByName items="${polypeptide.featureCvTerms}" cv="CC_genedb_controlledcuration" var="controlledCurationTerms"/>
    <c:if test="${fn:length(controlledCurationTerms) > 0}">
        <format:genePageSection id="controlCur">
            <div class="heading">Controlled Curation</div>
            <table width="100%" class="go-section">
                <format:go-section featureCvTerms="${controlledCurationTerms}" featureCounts="${CC}"/>
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
                <format:go-section title="Biological Process" featureCvTerms="${biologicalProcessTerms}" featureCounts="${BP}"/>
                <format:go-section title="Cellular Component" featureCvTerms="${cellularComponentTerms}" featureCounts="${CellularC}"/>
                <format:go-section title="Molecular Function" featureCvTerms="${molecularFunctionTerms}" featureCounts="${MF}"/>
            </table>
        </format:genePageSection>
    </c:if>

    <c:if test="${!gene.pseudo}">
        <!-- Predicted Peptide Section -->
        <div id="peptideRow" class="row">
            <format:genePageSection id="peptideProperties">
                <div class="heading">Predicted Peptide Properties</div>
                <table>
                <tr>
                    <td class="label">Isoelectric Point</td>
                    <td class="value">pH ${polyprop.isoelectricPoint}</td>
                </tr>
                <c:if test="${polyprop.mass != null}">
                    <tr>
                        <td class="label">Mass</td>
                        <td class="value">${polyprop.mass} kDa</td>
                    </tr>
                </c:if>
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
    </c:if>

    <!-- Domain Information -->
    <div id="domainInfo" style="clear: both;">
    </div>

    <!-- Ortholog / Paralog Section -->
    <db:propByName items="${polypeptide.featureRelationshipsForSubjectId}" cvTerm="orthologous_to" var="orthologs"/>
    <c:if test="${fn:length(orthologs) > 0}">
	    <format:genePageSection id="orthologs">
	        <div class="heading">Orthologs / Paralogs</div>
	        <db:filtered-loop items="${orthologs}" var="ortholog" varStatus="status">
	        	<c:set var="feat" value="${ortholog.featureByObjectId}"/>
	        	<c:if test="${feat.cvTerm.name eq 'protein_match'}">
	        		<span>${feat.uniqueName} <a href="<c:url value="/"/>Orthologs?cluster=${feat.uniqueName}">${fn:length(feat.featureRelationshipsForObjectId)} Others</a></span><br>
	        	</c:if>
	        	<c:if test="${feat.cvTerm.name eq 'polypeptide'}">
	        		<span><a href="<c:url value="/"/>NamedFeature?name=${feat.gene.uniqueName}">${feat.gene.uniqueName}</a></span><br>
	        	</c:if>
	        </db:filtered-loop>
	    </format:genePageSection>
	</c:if>

</c:if>
