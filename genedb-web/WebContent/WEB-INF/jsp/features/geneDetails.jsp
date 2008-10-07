<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="primaryLoc" value="${gene.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.sourceFeature}" />
<c:set var="organism" value="${gene.organism.commonName}" />

<div id="firstRow" class="row">
    <%-- General Information --%>
    <format:genePageSection id="generalInformation">
        <div class="heading">General Information</div>
        <table>
        <c:if test="${organism == 'Pfalciparum'}">
            <tr colspan="2">
                <td class="value">See the <a href="http://www.genedb.org/genedb/Search?name=${dto.uniqueName}">original annotation for this gene</a> in 'classic' GeneDB</td>
            </tr>
         </c:if>
        <col style="width: 9em;">
        <c:if test="${!empty dto.properName && dto.properName != dto.uniqueName}">
            <tr>
                <td class="label">Gene Name</td>
                <td class="value">${dto.properName}</td>
            </tr>
         </c:if>
         <tr>
            <td class="label">Systematic Name</td>
            <td class="value">
                <c:choose>
                <c:when test="${dto.anAlternateTranscript}">
                    ${dto.uniqueName} (one splice form of ${dto.geneName})
                </c:when>
                <c:otherwise>
                  ${dto.uniqueName}
                </c:otherwise>
                </c:choose>
            </td>
        </tr>
<%-- ------------------------------------------------------- --%>
        <tr>
            <td class="label">Feature Type</td>
            <td class="value">${dto.typeDescription}</td>
        </tr>
<%-- ------------------------------------------------------- --%>
        <c:if test="${!empty dto.synonyms}">
            <tr>
                <td class="label">Synonyms</td>
                <td class="value">
                    <format:list-string list="${dto.synonyms}"/>
                </td>
            </tr>
        </c:if>
<%-- ------------------------------------------------------- --%>
        <c:if test="${!empty dto.obsoleteNames}">
            <tr>
                <td class="label">Obsolete names</td>
                <td class="value">
                    <format:list-string list="${dto.obsoleteNames}"/>
                </td>
            </tr>
        </c:if>
<%-- ------------------------------------------------------- --%>
        <c:if test="${!empty dto.products}">
            <tr>
                <td class="label">Product</td>
                <td class="value">
                    <c:forEach items="${dto.products}" var="product">
                        <span>${product}</span><br>
                    </c:forEach>
                </td>
            </tr>
        </c:if>
        <tr>
            <td class="label">Location</td>
            <td class="value">
                ${dto.topLevelFeatureType} ${dto.topLevelFeatureDisplayName},
                locations ${dto.location}
            </td>
        </tr>
        <c:if test="${!empty dto.dbXRefDTOs}">
          <tr>
              <td class="label">See Also</td>
              <td class="value">
                  <c:forEach items="${dto.dbXRefDTOs}" var="dbxref" varStatus="status">
                    <c:if test="${!empty dbxref.urlPrefix}">
                      <span><a href="${dbxref.urlPrefix}${dbxref.accession}">${dbxref.dbName}:${dbxref.accession}</a><%--
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
             <a href="<c:url  value="/" />/FeatureSequence?name=${dto.uniqueName}&seqs=true">View/analyze/ download sequence</a>
         </div>
        <div style="clear: both; margin-top: 1ex;">
            Show region in
            <a href="ArtemisLaunch?organism=${organism}&chromosome=${dto.topLevelFeatureUniqueName}&start=${dto.min}&end=${dto.max}">Artemis</a>,
            GBrowse
         </div>
    </format:genePageSection>
</div>

<c:if test="${dto.proteinCoding}">

    <%-- Notes Section --%>
    <c:if test="${fn:length(dto.notes) > 0}">
        <format:genePageSection id="comment">
            <div class="heading">Comments</div>
            <c:forEach items="${dto.notes}" var="note">
                <div class="comment">${note}</div>
            </c:forEach>
        </format:genePageSection>
    </c:if>

    <%-- Comment Section --%>
    <c:if test="${fn:length(dto.comments) > 0}">
        <format:genePageSection id="curation">
            <div class="heading">Curation</div>
            <c:forEach items="${dto.comments}" var="comment">
                <div class="comment">${comment}</div>
            </c:forEach>
        </format:genePageSection>
    </c:if>

    <%-- Controlled Curation Section --%>
    <c:if test="${fn:length(dto.controlledCurations) > 0}">
        <format:genePageSection id="controlCur">
            <div class="heading">Controlled Curation</div>
            <table width="100%" class="go-section">
                <format:featureCvTerm-section featureCvTerms="${dto.controlledCurations}" organism="${organism}"/>
            </table>
        </format:genePageSection>
    </c:if>

    <%-- Gene Ontology Section --%>
    <c:if test="${fn:length(dto.goBiologicalProcesses) + fn:length(dto.goMolecularFunctions) + fn:length(dto.goCellularComponents) > 0}">
        <format:genePageSection id="geneOntology">
            <div class="heading">Gene Ontology</div>
            <table width="100%" class="go-section">
                <format:go-section title="Biological Process" featureCvTerms="${dto.goBiologicalProcesses}" organism="${organism}"/>
                <format:go-section title="Cellular Component" featureCvTerms="${dto.goCellularComponents}" organism="${organism}"/>
                <format:go-section title="Molecular Function" featureCvTerms="${dto.goMolecularFunctions}" organism="${organism}"/>
            </table>
        </format:genePageSection>
    </c:if>

    <c:if test="${!dto.pseudo}">
        <%-- Predicted Peptide Section --%>
        <div id="peptideRow" class="row">
            <c:set var="hasAlgorithmData" value="${fn:length(dto.algorithmData) > 0}"/>
            <c:if test="${hasAlgorithmData}">
                <c:set var="peptidePropertiesClass" value="leftBox"/>
            </c:if>
            <format:genePageSection id="peptideProperties" className="${peptidePropertiesClass}">
                <div class="heading">Predicted Peptide Data</div>
                <table>
                <c:if test="${dto.polypeptideProperties.isoelectricPoint != null}">
                    <tr>
                        <td class="label">Isoelectric Point</td>
                        <td class="value">pH ${dto.polypeptideProperties.isoelectricPoint}</td>
                    </tr>
                </c:if>
                <c:if test="${dto.polypeptideProperties.mass != null}">
                    <tr>
                        <td class="label">Mass</td>
                        <td class="value">${dto.polypeptideProperties.mass}</td>
                    </tr>
                </c:if>
                <tr>
                    <td class="label">Charge</td>
                    <td class="value">${dto.polypeptideProperties.charge}</td>
                </tr>
                <tr>
                    <td class="label">Amino Acids</td>
                    <td class="value">${dto.polypeptideProperties.aminoAcids}</td>
                </tr>
                </table>
            </format:genePageSection>

            <c:if test="${hasAlgorithmData}">
                <format:genePageSection id="peptideAlgorithms" className="rightBox">
                    <div class="heading">Algorithmic Predictions</div>
                    <table>
                    <c:if test="${dto.algorithmData.SignalP != null}">
                        <tr>
                            <td class="label">SignalP</td>
                            <td class="value">Predicted ${dto.algorithmData.SignalP.prediction}
                            (Signal peptide probability ${dto.algorithmData.SignalP.peptideProb},
                            signal anchor probability ${dto.algorithmData.SignalP.anchorProb}).
                            <c:if test="${dto.algorithmData.SignalP.cleavageSite != null}">
                                Predicted cleavage site at ${dto.algorithmData.SignalP.cleavageSite}
                                with probability ${dto.algorithmData.SignalP.cleavageSiteProb}.
                            </c:if></td>
                        </tr>
                    </c:if>
                    <c:if test="${dto.algorithmData.TMHMM != null}">
                        <tr>
                            <td class="label">TMHMM</td>
                            <td class="value">Predicted ${fn:length(dto.algorithmData.TMHMM)}
                            transmembrane region<c:if test="${fn:length(dto.algorithmData.TMHMM) > 1}">s</c:if>
                            at locations
                            <c:forEach var="helix" varStatus="status" items="${dto.algorithmData.TMHMM}"><%--
                                --%><c:if test="${!status.first && !status.last}">,</c:if>
                                <c:if test="${status.last && !status.first}">and </c:if>
                                ${helix}</c:forEach>.</td>
                        </tr>
                    </c:if>
                    <c:if test="${dto.algorithmData.DGPI != null && dto.algorithmData.DGPI.anchored}">
                        <tr>
                            <td class="label">DGPI</td>
                            <td class="value">
                                <c:if test="${dto.algorithmData.DGPI.anchored}">This protein is GPI-anchored.</c:if>
                                <c:if test="${!dto.algorithmData.DGPI.anchored}">This protein is <b>not</b> GPI-anchored.</c:if>
                                <c:if test="${dto.algorithmData.DGPI.location != null}">Predicted cleavage site at ${dto.algorithmData.DGPI.location} with score ${dto.algorithmData.DGPI.score}.</c:if>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${dto.algorithmData.PlasmoAP != null}">
                        <tr>
                            <td class="label">PlasmoAP</td>
                            <td class="value">${dto.algorithmData.PlasmoAP.description} apicoplast-targeting protein (score ${dto.algorithmData.PlasmoAP.score}).</td>
                        </tr>
                    </c:if>
                </table></format:genePageSection>
            </c:if>
        </div>
    </c:if>

    <%-- Protein map section --%>
    <c:if test="${dto.ims != null}">
        <format:genePageSection id="proteinMap">
            <div class="heading">Protein map</div>
            ${dto.ims.imageMap}
         <!--[if lte IE 6]>
                <div style="position:relative; height: ${dto.ims.height}px">
                    <div style="position:absolute; z-index: 1000;">
                        <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap">
                    </div>
                    <div style="position:static; z-index: 900;">
                        <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}"
                            style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='${dto.ims.path}', sizingMethod='image')"/>
                    </div>
                </div>
            <![endif]-->
            <![if ! lte IE 6]>
                <img src="${dto.ims.path}" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap" id="proteinMapImage">
            <![endif]>
        </format:genePageSection>
    </c:if>

    <c:if test="${fn:length(dto.domainInformation) > 0}">
        <%-- Domain Information --%>
        <format:genePageSection id="domainInfo">
            <div class="heading">Domain Information</div>
            <table class="domainTable"><tbody>
                <tr>
                    <td colspan="2"></td>
                    <td class="domainPosition">Position</td>
                    <td class="domainScore">E-value</td>
                </tr>
                <c:forEach var="subsection" varStatus="status" items="${dto.domainInformation}">
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

    <%-- Ortholog / Paralog Section --%>
    <c:if test="${fn:length(dto.clusterIds) + fn:length(dto.orthologueNames) > 0}">
        <format:genePageSection id="orthologs">
            <div class="heading">Orthologues and Paralogues</div>
            <c:forEach items="${dto.clusterIds}" var="clusterId">
                <span>${clusterId} <a href="<c:url value="/"/>Orthologs?cluster=${clusterId}">Look up others in cluster</a></span><br>
            </c:forEach>
            <c:forEach items="${dto.orthologueNames}" var="orthologueName">
                <span><a href="<c:url value="/"/>NamedFeature?name=${orthologueName}">${orthologueName}</a></span><br>
            </c:forEach>
        </format:genePageSection>
    </c:if>

</c:if>
