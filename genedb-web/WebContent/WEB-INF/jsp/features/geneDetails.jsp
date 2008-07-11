<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />
<c:set var="organism" value="${gene.organism.commonName}" />

<div id="firstRow" class="row">
    <!-- General Information -->
    <format:genePageSection id="generalInformation">
        <div class="heading">General Information</div>
        <table>
        <col style="width: 9em;">
        <c:if test="${!empty gene.name && gene.name != gene.systematicId}">
            <tr>
                <td class="label">Gene Name</td>
                <td class="value">${gene.name}</td>
            </tr>
         </c:if>
         <tr>
            <td class="label">Systematic Name</td>
            <td class="value">
				<c:choose>
                <c:when test="${fn:length(gene.transcripts) > 1}">
                    ${transcript.uniqueName} (one splice form of ${gene.systematicId})
				</c:when>
				<c:otherwise>
					${gene.systematicId}
				</c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td class="label">Feature Type</td>
            <td class="value">
				<c:choose>
					<c:when test="${transcript.cvTerm.name == 'mRNA'}">Protein coding gene</c:when>
					<c:when test="${transcript.cvTerm.name == 'pseudogenic_transcript'}">Pseudogene</c:when>
					<c:otherwise>${transcript.cvTerm.name}</c:otherwise>
				</c:choose>
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
                <td class="label">Product</td>
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
                Chromosome ${chromosome.displayName},
                locations ${transcript.exonLocsTraditional}
            </td>
        </tr>
        <c:if test="${!empty(polypeptide.featureDbXRefs)}">
	        <tr>
	            <td class="label">See Also</td>
	            <td class="value">
	                <c:forEach items="${polypeptide.featureDbXRefs}" var="fdbxref">
	                	<c:if test="${!empty fdbxref.dbXRef.db.urlPrefix}">
	                		<span><a href="${fdbxref.dbXRef.db.urlPrefix}${fdbxref.dbXRef.accession}">${fdbxref.dbXRef.db.name}:${fdbxref.dbXRef.accession}</a></span>
	                	</c:if>
	                </c:forEach>
	            </td>
	        </tr>
	    </c:if>
        </table>
    </format:genePageSection>

    <format:genePageSection id="analysisTools" className="whiteBox">
        <div class="heading">Analysis tools</div>
        <form name="downloadRegion" action="FeatureDownload" method="get">
            <div>Download Region as</div><br>
            <select name="downloadType">
                <option value="SPLICED_DNA">Spliced DNA</option>
                <c:if test="${polypeptide != null}">
                    <option value="PROTEIN">Protein</option>
                </c:if>
            </select>
            <input type="hidden" name="featureType" value="${transcript.cvTerm.name}" />
            <input type="hidden" name="featureName" value="<c:out value="${transcript.uniqueName}" />">
            <input type="submit" value="Submit">
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
    <db:propByName items="${polypeptide.featureCvTerms}" cvPattern="CC_.*" var="controlledCurationTerms"/>
    <c:if test="${fn:length(controlledCurationTerms) > 0}">
        <format:genePageSection id="controlCur">
            <div class="heading">Controlled Curation</div>
            <table width="100%" class="go-section">
                <format:go-section featureCvTerms="${controlledCurationTerms}" featureCounts="${CC}" organism="${organism}"/>
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
                <format:go-section title="Biological Process" featureCvTerms="${biologicalProcessTerms}" featureCounts="${BP}" organism="${organism}"/>
                <format:go-section title="Cellular Component" featureCvTerms="${cellularComponentTerms}" featureCounts="${CellularC}" organism="${organism}"/>
                <format:go-section title="Molecular Function" featureCvTerms="${molecularFunctionTerms}" featureCounts="${MF}" organism="${organism}"/>
            </table>
        </format:genePageSection>
    </c:if>

    <c:if test="${!gene.pseudo}">
        <!-- Predicted Peptide Section -->
        <div id="peptideRow" class="row">
            <format:genePageSection id="peptideProperties">
                <div class="heading">Predicted Peptide Properties</div>
                <table>
                <c:if test="${polyprop.isoelectricPoint != null}">
                    <tr>
                        <td class="label">Isoelectric Point</td>
                        <td class="value">pH ${polyprop.isoelectricPoint}</td>
                    </tr>
                </c:if>
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

            <%-- <format:genePageSection id="proteinMap" className="whiteBox">
                <div class="heading">Protein Map</div>
                <div align="center">
                    <img src="<c:url value="/includes/images/protein.gif"/>" id="ProteinMap">
                </div>
            </format:genePageSection> --%>
        </div>
    </c:if>

    <c:if test="${fn:length(domainInformation) > 0}">
        <!-- Domain Information -->
        <format:genePageSection id="domainInfo">
            <div class="heading">Domain Information</div>
            <table class="domainTable"><tbody>
                <tr>
                    <td colspan="2"></td>
                    <td class="domainPosition">Position</td>
                    <td class="domainScore">E-value</td>
                </tr>
                <c:forEach var="subsection" varStatus="status" items="${domainInformation}">
                    <tr>
                        <td colspan="2" class="domainTitle<c:if test="${status.first}">First</c:if>">
                            <c:if test="${subsection.interproDbXRef != null}">
                                <a href="${subsection.interproDbXRef.db.urlPrefix}${subsection.interproDbXRef.accession}"
                                    >${subsection.interproDbXRef.db.name}:${subsection.interproDbXRef.accession}</a>
                                    <i>${subsection.interproDbXRef.description}</i>
                                    matches:
                            </c:if>
                            <c:if test="${subsection.interproDbXRef == null}">
                                ${subsection.title}:
                            </c:if>
                        </td>
                    </tr>
                    <c:forEach var="hit" items="${subsection.hits}">
                        <tr>
                            <td class="domainAccession">
                                <a href="${hit.dbxref.db.urlPrefix}${hit.dbxref.accession}"
                                    >${hit.dbxref.db.name}:${hit.dbxref.accession}</a>
                            </td>
                            <td class="domainDescription">${hit.dbxref.description}</td>
                            <td class="domainPosition">${1 + hit.fmin} - ${hit.fmax}</td>
                            <td class="domainScore">${hit.score}</td>
                        </tr>
                    </c:forEach>
                </c:forEach>
            </tbody></table>
        </format:genePageSection>
    </c:if>
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
