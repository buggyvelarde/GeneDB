<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.sourceFeature}" />
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
          <c:when test="${transcript.type.name == 'mRNA'}">Protein coding gene</c:when>
          <c:when test="${transcript.type.name == 'pseudogenic_transcript'}">Pseudogene</c:when>
          <c:otherwise>${transcript.type.name}</c:otherwise>
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
                  <c:forEach items="${polypeptide.featureDbXRefs}" var="fdbxref" varStatus="status">
                    <c:if test="${!empty fdbxref.dbXRef.db.urlPrefix}">
                      <span><a href="${fdbxref.dbXRef.db.urlPrefix}${fdbxref.dbXRef.accession}">${fdbxref.dbXRef.db.name}:${fdbxref.dbXRef.accession}</a><%--
                             --%><c:if test="${!status.last}">, </c:if></span>
                    </c:if>
                  </c:forEach>
              </td>
          </tr>
      </c:if>
        </table>
    </format:genePageSection>

    <format:genePageSection id="analysisTools" className="whiteBox">
        <div style="clear: both; margin-top: 1ex;">
             <a href="">Add gene to basket</a>
         </div>
        <div style="clear: both; margin-top: 1ex;">
             <a href="/new/FeatureSequence?name=${transcript.uniqueName}&seqs=true">View/analyze/download sequence</a>
         </div>
        <%-- <form name="downloadRegion" action="FeatureDownload" method="get">
            <div>Download Region as</div><br>
            <select name="downloadType">
                <option value="SPLICED_DNA">Spliced DNA</option>
                <c:if test="${polypeptide != null}">
                    <option value="PROTEIN">Protein</option>
                </c:if>
            </select>
            <input type="hidden" name="featureType" value="${transcript.type.name}" />
            <input type="hidden" name="featureName" value="<c:out value="${transcript.uniqueName}" />">
            <input type="submit" value="Submit">
        </form> --%>
        <div style="clear: both; margin-top: 1ex;">
            Show region in
            <a href="ArtemisLaunch?organism=${gene.organism.commonName}&chromosome=${chromosome.uniqueName}&start=${primaryLoc.fmin}&end=${primaryLoc.fmax}">Artemis</a>,
            GBrowse
         </div>
    </format:genePageSection>
</div>

<c:if test="${polypeptide != null}">

    <!-- Comments Section -->
    <db:filterByType items="${polypeptide.featureProps}" cvTerm="comment" var="comment"/>
    <c:if test="${fn:length(comment) > 0}">
        <format:genePageSection id="comment">
            <div class="heading">Comments</div>
            <db:filtered-loop items="${polypeptide.featureProps}" cvTerm="comment" var="comment" varStatus="status">
                <div class="comment">${comment.value}</div>
            </db:filtered-loop>
        </format:genePageSection>
    </c:if>

    <!-- Curation Section -->
    <db:filterByType items="${polypeptide.featureProps}" cvTerm="curation" var="curation"/>
    <c:if test="${fn:length(curation) > 0}">
        <format:genePageSection id="curation">
            <div class="heading">Curation</div>
            <db:filtered-loop items="${polypeptide.featureProps}" cvTerm="curation" var="curation" varStatus="status">
                <div class="comment">${curation.value}</div>
            </db:filtered-loop>
        </format:genePageSection>
    </c:if>

    <!-- Controlled Curation Section -->
    <db:filterByType items="${polypeptide.featureCvTerms}" cvPattern="CC_.*" var="controlledCurationTerms"/>
    <c:if test="${fn:length(controlledCurationTerms) > 0}">
        <format:genePageSection id="controlCur">
            <div class="heading">Controlled Curation</div>
            <table width="100%" class="go-section">
                <format:go-section featureCvTerms="${controlledCurationTerms}" featureCounts="${CC}" organism="${organism}"/>
            </table>
        </format:genePageSection>
    </c:if>

    <!-- Gene Ontology Section -->
    <db:filterByType items="${polypeptide.featureCvTerms}" cv="biological_process" var="biologicalProcessTerms"/>
    <db:filterByType items="${polypeptide.featureCvTerms}" cv="molecular_function" var="molecularFunctionTerms"/>
    <db:filterByType items="${polypeptide.featureCvTerms}" cv="cellular_component" var="cellularComponentTerms"/>
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
            <c:set var="hasAlgorithmData" value="${fn:length(algorithmData) > 0}"/>
            <c:if test="${hasAlgorithmData}">
                <c:set var="peptidePropertiesClass" value="leftBox"/>
            </c:if>
            <format:genePageSection id="peptideProperties" className="${peptidePropertiesClass}">
                <div class="heading">Predicted Peptide Data</div>
                <table>
                <c:if test="${pepstats.isoelectricPoint != null}">
                    <tr>
                        <td class="label">Isoelectric Point</td>
                        <td class="value">pH ${pepstats.isoelectricPoint}</td>
                    </tr>
                </c:if>
                <c:if test="${pepstats.mass != null}">
                    <tr>
                        <td class="label">Mass</td>
                        <td class="value">${pepstats.mass}</td>
                    </tr>
                </c:if>
                <tr>
                    <td class="label">Charge</td>
                    <td class="value">${pepstats.charge}</td>
                </tr>
                <tr>
                    <td class="label">Amino Acids</td>
                    <td class="value">${pepstats.aminoAcids}</td>
                </tr>
                </table>
            </format:genePageSection>

            <c:if test="${hasAlgorithmData}">
                <format:genePageSection id="peptideAlgorithms" className="rightBox">
                    <div class="heading">Algorithmic Predictions</div>
                    <table>
                    <c:if test="${algorithmData.SignalP != null}">
                        <tr>
                            <td class="label">SignalP</td>
                            <td class="value">Predicted ${algorithmData.SignalP.prediction}
                            (Signal peptide probability ${algorithmData.SignalP.peptideProb},
                            signal anchor probability ${algorithmData.SignalP.anchorProb}).
                            <c:if test="${algorithmData.SignalP.cleavageSite != null}">
                                Predicted cleavage site at ${algorithmData.SignalP.cleavageSite}
                                with probability ${algorithmData.SignalP.cleavageSiteProb}.
                            </c:if></td>
                        </tr>
                    </c:if>
                    <c:if test="${algorithmData.TMHMM != null}">
                        <tr>
                            <td class="label">TMHMM</td>
                            <td class="value">Predicted ${fn:length(algorithmData.TMHMM)}
                            transmembrane region<c:if test="${fn:length(algorithmData.TMHMM) > 1}">s</c:if>
                            at locations
                            <c:forEach var="helix" varStatus="status" items="${algorithmData.TMHMM}"><%--
                                --%><c:if test="${!status.first && !status.last}">,</c:if>
                                <c:if test="${status.last && !status.first}">and </c:if>
                                ${helix}</c:forEach>.</td>
                        </tr>
                    </c:if>
                    <c:if test="${algorithmData.DGPI != null && algorithmData.DGPI.anchored}">
                        <tr>
                            <td class="label">DGPI</td>
                            <td class="value">
                                <c:if test="${algorithmData.DGPI.anchored}">This protein is GPI-anchored.</c:if>
                                <c:if test="${!algorithmData.DGPI.anchored}">This protein is <b>not</b> GPI-anchored.</c:if>
                                <c:if test="${algorithmData.DGPI.location != null}">Predicted cleavage site at ${algorithmData.DGPI.location} with score ${algorithmData.DGPI.score}.</c:if>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${algorithmData.PlasmoAP != null}">
                        <tr>
                            <td class="label">PlasmoAP</td>
                            <td class="value">${algorithmData.PlasmoAP.description} apicoplast-targeting protein (score ${algorithmData.PlasmoAP.score}).</td>
                        </tr>
                    </c:if>
                </table></format:genePageSection>
            </c:if>
        </div>
    </c:if>

    <!-- Protein map section -->
    <c:if test="${proteinMap != null}">
        <format:genePageSection id="proteinMap">
            <div class="heading">Protein map</div>
            ${proteinMapMap}
         <!--[if lte IE 6]>
                <div style="position:relative; height: ${proteinMapHeight}px">
                    <div style="position:absolute; z-index: 1000;">
                        <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${proteinMapWidth}" height="${proteinMapHeight}" useMap="#proteinMapMap">
                    </div>
                    <div style="position:static; z-index: 900;">
                        <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${proteinMapWidth}" height="${proteinMapHeight}"
                            style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='${proteinMap}', sizingMethod='image')"/>
                    </div>
                </div>
            <![endif]-->
            <![if ! lte IE 6]>
                <img src="${proteinMap}" useMap="#proteinMapMap" id="proteinMapImage">
            <![endif]>
        </format:genePageSection>
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
                            <c:if test="${subsection.url != null}">
                                <a href="${subsection.url}">${subsection.uniqueName}</a>
                                    <i>${subsection.description}</i>
                                    matches:
                            </c:if>
                            <c:if test="${subsection.url == null}">
                                ${subsection.uniqueName}:
                            </c:if>
                        </td>
                    </tr>
                    <c:forEach var="hit" items="${subsection.subfeatures}">
                        <tr>
                            <td class="domainAccession">
                                <c:if test="${hit.url != null}">
                                    <a href="${hit.url}">${hit.uniqueName}</a>
                                </c:if>
                                <c:if test="${hit.url == null}">
                                    ${hit.uniqueName}
                                </c:if>
                            </td>
                            <td class="domainDescription">${hit.description}</td>
                            <td class="domainPosition">${1 + hit.fmin} - ${hit.fmax}</td>
                            <td class="domainScore">${hit.score}</td>
                        </tr>
                    </c:forEach>
                </c:forEach>
            </tbody></table>
        </format:genePageSection>
    </c:if>

    <!-- Ortholog / Paralog Section -->
    <db:filterByType items="${polypeptide.featureRelationshipsForSubjectId}" cvTerm="orthologous_to" var="orthologs"/>
    <c:if test="${fn:length(orthologs) > 0}">
        <format:genePageSection id="orthologs">
            <div class="heading">Orthologues and Paralogues</div>
            <db:filtered-loop items="${orthologs}" var="ortholog" varStatus="status">
                <c:set var="feat" value="${ortholog.objectFeature}"/>
                <c:if test="${feat.type.name eq 'protein_match'}">
                    <span>${feat.uniqueName} <a href="<c:url value="/"/>Orthologs?cluster=${feat.uniqueName}">${fn:length(feat.featureRelationshipsForObjectId)} Others</a></span><br>
                </c:if>
                <c:if test="${feat.type.name eq 'polypeptide'}">
                    <span><a href="<c:url value="/"/>NamedFeature?name=${feat.gene.uniqueName}">${feat.gene.uniqueName}</a></span><br>
                </c:if>
            </db:filtered-loop>
        </format:genePageSection>
    </c:if>

</c:if>
