<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test='${dto.organismCommonName == "Pchabaudi"}'>
<br />
<P>The Wellcome Trust Sanger Institute plans on publishing the completed and annotated sequences (i.e. 8X assembly and updated
annotation) of <i>P.chabaudi</i> AS in a peer reviewed journal as soon as possible. Permission of the principal investigator
should be obtained before publishing analyses of the sequence/open reading frames/genes on a chromosome or genome scale.</P>
<br />
</c:if>
<c:if test='${dto.organismCommonName == "Pberghei"}'>
<br />
<P>The Wellcome Trust Sanger Institute plans on publishing the completed and annotated sequences (i.e. 8X assembly and updated
annotation) of <i>P.berghei</i> ANKA in a peer reviewed journal as soon as possible. Permission of the principal investigator
should be obtained before publishing analyses of the sequence/open reading frames/genes on a chromosome or genome scale.</P>
<br />
</c:if>


<p style="text-align:right"><span style="color: #8b031b">New!</span> View in :
<span style="font-weight:bold;padding:10px 20px 10px 20px;" class="ui-state-default ui-corner-all"   >
<a target="web-artemis" href="http://www.genedb.org/web-artemis/?src=${dto.topLevelFeatureUniqueName}&base=${dto.min-100}&bases=${dto.max-dto.min +200}">Web-Artemis (alpha)</a>
</span>
</p>


<h2 style="padding-top:0px;margin-top:0px;">General Information</h2>
<div id="col-4-1">

<div class="main-grey-3-4-top"></div>
<div class="light-grey">
<span class="float-right grey-text"><misc:displayDate time="${dto.lastModified}" message="Last Modified" /></span>
<h2>Summary</h2>
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


<c:if test="${!empty dto.proteinSynonymsByTypes}">
  <c:forEach var="type" items="${dto.proteinSynonymsByTypes}">
  <tr>
    <th>${type.key}</th>
    <td>
      <misc:listItems collection="${type.value}"/>
    </td>
  </tr>
  </c:forEach>
</c:if>

<tr>
<th>Location</th>
<td>${dto.topLevelFeatureType} ${dto.topLevelFeatureDisplayName}; ${dto.location}</td>
</tr>

<c:if test="${!empty dto.dbXRefDTOs}">
  <tr>
    <th>See Also</th>
    <td>
    <ul>
    <c:forEach items="${dto.dbXRefDTOs}" var="dbxref" varStatus="status">
      <c:if test="${!empty dbxref.urlPrefix}">
        <c:if test="${dbxref.urlPrefix != 'http://plasmodb.org/plasmodb/servlet/sv?page=gene&source_id='}" >
            <!-- ignoring plasmo DB links, no displayed below like TriTrypDB links -->
            <c:set var="urlSuffix" value=""/>
                <c:if test="${dbxref.urlPrefix=='http://www.genedb.org/genedb/pathway_comparison_TriTryp/'}">
                    <c:set var="urlSuffix" value=".html"/>
                </c:if>
                <li><a href="${dbxref.urlPrefix}${dbxref.accession}${urlSuffix}"><db:dbName db="${dbxref.dbName}"/></a></li>
        </c:if>
      </c:if>
    </c:forEach>
    </ul>
    </td>
  </tr>
</c:if>

<!-- ignored Plasmo DB links introduced here -->
<c:forEach items="${dto.dbXRefDTOs}" var="dbxref" varStatus="status">
    <c:if test="${!empty dbxref.urlPrefix}">
        <c:if test="${dbxref.urlPrefix == 'http://plasmodb.org/plasmodb/servlet/sv?page=gene&source_id='}">
            <tr>
                <th>PlasmoDB</th>
                <td><a href="http://plasmodb.org/gene/${dbxref.accession}"><db:dbName db="${dbxref.accession}"/></a></td>
           </tr>
       </c:if>
    </c:if>
</c:forEach>

<c:if test="${dto.organismCommonName=='Lmajor' || dto.organismCommonName=='Linfantum' || dto.organismCommonName=='Lbraziliensis' || dto.organismCommonName=='Tbruceibrucei927' || dto.organismCommonName=='Tbruceibrucei427' || dto.organismCommonName=='Tbruceigambiense' || dto.organismCommonName=='Tvivax' || dto.organismCommonName=='Tcruzi' }">
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
<format:addToBasket uniqueName="${dto.uniqueName}" />
<a href="<misc:url value="/featureSeq/"/>${dto.uniqueName}"><img src="<misc:url value="/includes/image/button-view-sequence.gif"/>" height="46" width="144" alt="View Sequence" border="0" /></a>
<a href="<misc:url value="/ArtemisLaunch/${dto.organismCommonName}/${dto.topLevelFeatureUniqueName}.jnlp?start=${dto.min}&end=${dto.max}"/>"><img src="<misc:url value="/includes/image/button-artemis.gif"/>" height="46" width="144" alt="Launch Artemis" border="0" /></a>
<%-- The coordinates in the line below rely on JBrowse coping with coordinates off the end of the contig --%>
<a href="<misc:url value="/jbrowse/${dto.organismCommonName}/?loc=${dto.topLevelFeatureDisplayName}:${dto.min - 5000}..${dto.max + 5000}&tracks=Complex%20Gene%20Models"/>"><img src="<misc:url value="/includes/image/button-jbrowse.gif"/>" height="46" width="144" alt="Show in JBrowse" border="0" /></a>
</div>
<div class="main-blue-3-4-bot"></div>
</div><!-- end internal column -right -->

<br class="clear" /><br />

<c:if test="${dto.proteinCoding}">
<style>
div.comment {
    display:list-item;
    margin-left:20px;
}
</style>
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
<h2 style="padding-top:0px;margin-top:0px;">Curation</h2>
<%-- Controlled Curation Section --%>
<c:if test="${fn:length(dto.controlledCurations) > 0}">
  <format:genePageSection id="controlCur">
  <h2>Phenotype</h2>
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
    <format:go-section title="Biological Process" featureCvTerms="${dto.goBiologicalProcesses}" category="biological_process" organism="${organism}"/>
    <format:go-section title="Cellular Component" featureCvTerms="${dto.goCellularComponents}" category="cellular_component" organism="${organism}"/>
    <format:go-section title="Molecular Function" featureCvTerms="${dto.goMolecularFunctions}" category="molecular_function" organism="${organism}"/>
  </table>
  </format:genePageSection>
</c:if>

<h2 style="padding-top:0px;margin-top:0px;">Protein Data</h2>

<c:set var="hasAlgorithmData" value="${fn:length(dto.algorithmData) > 0}"/>
<c:choose>
  <c:when test="${hasAlgorithmData}">
    <format:algorithmPredictions algData="${dto.algorithmData}"/>
    <div id="col-4-2">
      <div class="sub-grey-3-4-top"></div>
        <div class="light-grey-nopad">
          <format:peptideProperties pepProps="${dto.polypeptideProperties}"/>
        </div>
      <div class="sub-grey-3-4-bot"></div>
    </div><!-- end internal column -right -->
    <br class="clear" />
  </c:when>
  <c:otherwise>
    <format:genePageSection>
      <format:peptideProperties pepProps="${dto.polypeptideProperties}"/>
    </format:genePageSection>
  </c:otherwise>
</c:choose>

<c:if test="${dto.ims != null}">
<format:genePageSection>
<h2>Protein map</h2>
${dto.ims.imageMap}
<!--[if lte IE 6]>
<div style="position:relative; height: ${dto.ims.height}px">
<div style="position:absolute; z-index: 1000;">
  <img src="<misc:url value="/includes/image/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap">
</div>
<div style="position:static; z-index: 900;">
  <img src="<misc:url value="/includes/image/transparentPixel.gif"/>" width="${dto.ims.width}" height="${dto.ims.height}"
       style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<misc:url value="/Image/" />${dto.ims.path}', sizingMethod='image')"/>
</div>
</div>
<![endif]-->
<![if ! lte IE 6]>
  <img src="<misc:url value="/Image/" />${dto.ims.path}" width="${dto.ims.width}" height="${dto.ims.height}" useMap="#proteinMapMap" id="proteinMapImage" border="0">
<![endif]>
</format:genePageSection>
</c:if>

  <%-- Domain Information --%>
<c:if test="${fn:length(dto.domainInformation) > 0}">
  <format:genePageSection>
  <h2>Domain Information</h2>
  <table cellpadding="4" cellspacing="4" border="0" class="sequence-table">
  <tr>
    <td colspan="3"></td>
    <td class="domainPosition">Position</td>
    <td class="domainScore">E-value</td>
  </tr>
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
  <format:genePageSection>
  <h2>Orthologues and Paralogues</h2>
  <table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
    <c:forEach items="${dto.clusterIds}" var="clusterId">
      <tr><td>${clusterId}</td><td><a href="<misc:url value="/Orthologs/${clusterId}" />">Look up others in cluster</a></td></tr>
    </c:forEach>
    <tr><td></td><td></td></tr>
    <tr><th>Curated Orthologues</th><td>

    <misc:url value="/gene/" var="geneBaseUrl" />
    <db:orthologueFilter baseUrl="${geneBaseUrl}" sequenceDao="${sequenceDao}" dto="${dto}" />

    </td></tr>
  </table>
  </format:genePageSection>
</c:if>

</c:if>

<p>See this gene in <a href="<misc:url value="/gene/${dto.uniqueName}.xml" />">XML</a> or <a href="<misc:url value="/gene/${dto.uniqueName}.json" />">JSON</a> formats</p>
