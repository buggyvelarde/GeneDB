<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<div id="col-4-1">
<div class="main-grey-3-4-top"></div>
<div class="light-grey">
<span class="float-right grey-text">Last Modified March 10, 2009</span>
<h2>General Information</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">

<c:if test="${dto.properGeneName}">
  <tr>
    <th>Gene Name</th><td>${dto.geneName}</td>
  </tr>
</c:if>

<tr>
  <th>Systematic Name</th>
    <td>
    <c:choose>
    <c:when test="${dto.anAlternateTranscript}">
      <misc:formatSystematicName name="${dto.uniqueName}"/> (one splice form of <misc:formatSystematicName name="${dto.geneName}"/>)
    </c:when>
    <c:otherwise>
      <misc:formatSystematicName name="${dto.uniqueName}"/>
    </c:otherwise>
    </c:choose>
    </td>
</tr>

<tr><th>Feature Type</th><td>${dto.typeDescription}</td></tr>

<c:if test="${!empty dto.synonymsByTypes}">
  <c:forEach var="type" items="${dto.synonymsByTypes}">
  <tr>
    <th>${type.key}</th>
    <td>
      <misc:listItems collection="${type.value}"/>
    </td>
  </tr>
  </c:forEach>
</c:if>

<c:if test="${!empty dto.products}">
<tr><th>Product</th><td>
<c:forEach items="${dto.products}" var="product">
  <span>${product.typeName}</span>&nbsp;&nbsp;&nbsp;
  <span>
  <c:forEach items="${product.props.qualifier}" var="qualifier" varStatus="st">
    <c:if test="${!st.first}"> | </c:if>
    ${qualifier}
  </c:forEach>
  </span>
  <span class="evidence">
  <span><c:forEach items="${product.props.evidence}" var="evidence">${evidence}</c:forEach>&nbsp;</span>
  <span><c:forEach items="${product.pubs}" var="pub">(${pub})</c:forEach></span>
  <span><c:forEach items="${product.dbXRefDtoList}" var="fctdbx"><a href="${fctdbx.urlPrefix}${fctdbx.accession}">${fctdbx.dbName}:${fctdbx.accession}</a> </c:forEach></span>
    <c:if test="${product.withFrom != 'null'}"><db:dbXRefLink dbXRef="${product.withFrom}"/></c:if>
  </span>
  <format:n-others count="${product.count}" cvTermName="${product.typeName}" taxons="${dto.organismCommonName}" cv="genedb_products" suppress="${dto.uniqueName}" />
  <br />
</c:forEach>
</td></tr>
</c:if>

<tr>
<th>Location</th>
<td>${dto.topLevelFeatureType} ${dto.topLevelFeatureDisplayName}; ${dto.location}</td>
</tr>


<c:if test="${dto.organismCommonName=='Lmajor' || dto.organismCommonName=='Linfantum' || dto.organismCommonName=='Lbraziliensis' || dto.organismCommonName=='Tbruceibrucei927'}">
<tr>
  <th>TriTrypDB</th>
  <td><a href="http://tritrypdb.org/tritrypdb/showRecord.do?name=GeneRecordClasses.GeneRecordClass&project_id=TriTrypDB&source_id=<misc:formatSystematicName name="${dto.uniqueName}"/>"><misc:formatSystematicName name="${dto.uniqueName}"/></a></td>
</tr>
</c:if>

</table>

</div>
<div class="main-grey-3-4-bot"></div>
</div><!-- end internal column -left -->

<div id="col-4-2">
<div class="main-blue-3-4-top"></div>
<div class="baby-blue-nopad">
<a href=""><img src="/includes/images/button-artemis.gif" height="46" width="144" alt="Launch Artemis" border="0" /></a><br /><br />
<a href=""><img src="/includes/images/button-view-sequence.gif" height="46" width="144" alt="View Sequence" border="0" /></a>
</div>
<div class="main-blue-3-4-bot"></div>
</div><!-- end internal column -right -->

<br class="clear" /><br />

<c:if test="${dto.proteinCoding}">

<%-- Merged Notes & Comments Section --%>
<c:if test="${(fn:length(dto.notes) + fn:length(dto.comments) + fn:length(dto.publications)) > 0}">
  <format:genePageSection>
  <h2>Comments</h2>
  <db:hyperlinkDbs>
  <c:forEach items="${dto.notes}" var="note">
    <div class="comment">${note}</div>
  </c:forEach>
  <c:forEach items="${dto.comments}" var="comment">
    <div class="comment">${comment}</div>
  </c:forEach>
  <c:if test="${fn:length(dto.publications) > 0}">
    <br>
    <div class="comment">Key information on this gene is available from
    <c:forEach items="${dto.publications}" var="publication" varStatus="vs">
      <c:if test="${!vs.first}">, </c:if>
      <db:dbXRefLink dbXRef="${publication}" />
    </c:forEach>
    </div>
  </c:if>
  </db:hyperlinkDbs>
  </format:genePageSection>
</c:if>

<%-- Controlled Curation Section --%>
<c:if test="${fn:length(dto.controlledCurations) > 0}">
  <format:genePageSection id="controlCur">
  <h2>Controlled Curation</h2>
  <table width="100%" class="go-section">
    <format:featureCvTerm-section featureCvTerms="${dto.controlledCurations}" organism="${dto.organismCommonName}" cvName="ControlledCuration"/>
  </table>
  </format:genePageSection>
</c:if>



<%-- Gene Ontology Section --%>
<c:if test="${fn:length(dto.goBiologicalProcesses) + fn:length(dto.goMolecularFunctions) + fn:length(dto.goCellularComponents) > 0}">
  <format:genePageSection id="geneOntology">
  <h2>Gene Ontology</h2>
  <table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
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
                <c:if test="${dto.polypeptideProperties.hasMass}">
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
</c:if>


<div class="full-light-grey-bot"></div>
<br />
<div class="full-light-grey-top"></div>
<div class="light-grey">
<h2>BROKEN Predicted Peptide Data</h2>
<table cellpadding="4" cellspacing="4" border="0" class="sequence-table">
<tr>
<th>Isoelectric Point</th><td>pH 7.05</td>
</tr>
<tr>
<th>Mass</th><td>193.55 kDa</td>
</tr>
<tr>
<th>Charge</th><td>11.5</td>
</tr>
<tr>
<th>Amino Acids</th><td>1806</td>
</tr>
</table>
</div>
<div class="full-light-grey-bot"></div>
<br />

<c:if test="${dto.ims != null}">
<format:genePageSection>
<h2>Protein map</h2>
${dto.ims.imageMap}
<!--[if lte IE 6]>
<div style="position:relative; height: ${dto.ims.height}px">
<div style="position:absolute; z-index: 1000;">
  <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap">
</div>
<div style="position:static; z-index: 900;">
  <img src="<c:url value="/includes/images/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}"
       style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<c:url value="/Image?key=" />${dto.ims.path}', sizingMethod='image')"/>
</div>
</div>
<![endif]-->
<![if ! lte IE 6]>
  <img src="<c:url value="/Image?key=" />${dto.ims.path}" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap" id="proteinMapImage">
<![endif]>
</format:genePageSection>
</c:if>

  <%-- Domain Information --%>
<c:if test="${fn:length(dto.domainInformation) > 0}">
  <format:genePageSection>
  <h2>Domain Information</h2>
  <table cellpadding="4" cellspacing="4" border="0" class="sequence-table">
  <c:forEach var="subsection" varStatus="status" items="${dto.domainInformation}">
    <tr>
      <td>
      <c:if test="${subsection.url != null}">
        <a href="${subsection.url}">
      </c:if>
      ${subsection.uniqueName}
      <c:if test="${subsection.url != null}">
        </a>
      </c:if>
      </td><td colspan="4">
      <i>${subsection.description}</i> matches:
      </td>
    </tr>
    <c:forEach var="hit" items="${subsection.subfeatures}">
      <tr>
        <td></td>
        <td>
          <c:if test="${hit.url != null}"><a href="${hit.url}">${hit.uniqueName}</a></c:if>
          <c:if test="${hit.url == null}">${hit.uniqueName}</c:if>
        </td>
        <td>${hit.description}</td>
        <td>${1 + hit.fmin} - ${hit.fmax}</td>
        <td>${hit.score}</td>
      </tr>
    </c:forEach>
  </c:forEach>
  </table>
</format:genePageSection>
</c:if>


<%-- Ortholog / Paralog Section --%>
<c:if test="${fn:length(dto.clusterIds) + fn:length(dto.orthologueNames) > 0}">
<format:genePageSection id="orthologs">
<h2>Orthologues and Paralogues</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
<c:forEach items="${dto.clusterIds}" var="clusterId">
  <tr><td>${clusterId}</td><td><a href="<c:url value="/Orthologs"><c:param name="cluster" value="${clusterId}" /></c:url>">Look up others in cluster</a></span></td></tr>
</c:forEach>
<tr><td></td><td></td></tr>
<tr><th>Curated Orthologues</th><td>
<c:forEach items="${dto.orthologueNames}" var="orthologueName">
  <a href="<c:url value="/NamedFeature"><c:param name="name" value="${orthologueName}" /></c:url>">${orthologueName}</a>
</c:forEach>
</td></tr>
</table>
</format:genePageSection>
</c:if>

