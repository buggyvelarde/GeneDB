<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
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

<style>
ul {
    margin-left:10px;
}
li {
    margin-left:10px;
}
</style>

<%-- <div>
	<a href="./${gene.uniqueName}">${gene.uniqueName}</a>
	<c:forEach var="transcript" items="${gene.transcripts}">
		<ul>
			<li><a href="./${transcript.uniqueName}">${transcript.uniqueName}</a>
				<ul>
					<c:forEach var="exon" items="${transcript.exons}">
						<li><a href="./${exon.uniqueName}">${exon.uniqueName}</a>
						</li>
					</c:forEach>
					<li><a href="./${transcript.polypeptide.uniqueName}">${transcript.polypeptide.uniqueName}</a></li>
				</ul>
			</li>
		</ul>
	</c:forEach>
</div> --%>



<h2 style="padding-top:0px;margin-top:0px;">General Information</h2>
<div id="col-4-1">

<div class="main-grey-3-4-top"></div>
<div class="light-grey">
<span class="float-right grey-text"><misc:displayDate time="${dto.lastModified}" message="Last Modified" /></span>
<h2>Summary</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">

<c:if test="${dto.geneName}">
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
      <c:forEach var="transcript" items="${gene.transcripts}">
        <ul>
            <li><a href="./${transcript.uniqueName}">${transcript.uniqueName}</a></li>
        </ul>
    </c:forEach>
    </c:when>
    <c:otherwise>
      <misc:formatSystematicName name="${geneUniaueName}"/>
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
      
        <c:if test="${dbxref.urlPrefix != 'http://plasmodb.org/plasmodb/servlet/sv?page=gene&source_id='}" >
            <!-- ignoring plasmo DB links, no displayed below like TriTrypDB links -->
            <c:set var="urlSuffix" value=""/>
                <c:if test="${dbxref.urlPrefix=='http://www.genedb.org/genedb/pathway_comparison_TriTryp/'}">
                    <c:set var="urlSuffix" value=".html"/>
                </c:if>
                <li> <a href="${dbxref.urlPrefix}${dbxref.accession}${urlSuffix}">${dbxref.accession}</a> (<db:dbName db="${dbxref.dbName}"/>) </li>
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

<c:if test="${dto.organismCommonName=='Pchabaudi' || dto.organismCommonName=='Pberghei' }">
<tr>
  <th>PlasmoDB</th>
  <td><a href="http://plasmodb.org/gene/<misc:formatSystematicName name="${dto.uniqueName}"/>"><misc:formatSystematicName name="${dto.uniqueName}"/></a></td>
</tr>
</c:if>

<c:if test="${dto.organismCommonName=='Lmajor' || dto.organismCommonName=='Linfantum' || dto.organismCommonName=='Lbraziliensis' || dto.organismCommonName=='Tbruceibrucei927' || dto.organismCommonName=='Tbruceibrucei427' || dto.organismCommonName=='Tbruceigambiense' || dto.organismCommonName=='Tvivax' || dto.organismCommonName=='Tcruzi' }">
<tr>
  <th>TriTrypDB</th>
  <td><a href="http://tritrypdb.org/gene/<misc:formatSystematicName name="${dto.uniqueName}"/>"><misc:formatSystematicName name="${dto.uniqueName}"/></a></td>
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
<c:if test="${(fn:length(dto.controlledCurations) > 0) || ({fn:length(dto.goBiologicalProcesses) + fn:length(dto.goMolecularFunctions) + fn:length(dto.goCellularComponents) > 0)}">
<h2 style="padding-top:0px;margin-top:0px;">Curation</h2>
</c:if>
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

<c:if test="${dto.proteinCoding}">

<h2 style="padding-top:0px;margin-top:0px;">Protein Data</h2>

<c:set var="hasAlgorithmData" value="${fn:length(dto.algorithmData) > 0}"/>
<c:choose>
  <c:when test="${hasAlgorithmData}">
    <format:genePageSection>
        <format:algorithmPredictions algData="${dto.algorithmData}"/>
        <format:peptideProperties pepProps="${dto.polypeptideProperties}"/>
    </format:genePageSection>
  </c:when>
  <c:otherwise>
    <format:genePageSection>
      <format:peptideProperties pepProps="${dto.polypeptideProperties}"/>
    </format:genePageSection>
  </c:otherwise>
</c:choose>

<%-- <c:if test="${dto.ims != null}">
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
 --%>



  <%-- Domain Information --%>
<c:if test="${fn:length(dto.domainInformation) > 0 || hasAlgorithmData || fn:length(dto.membraneStructureComponents) > 0 }">
<br>
<format:genePageSection>
<h2>Protein map</h2>
        <c:set var="proteinMapCount" value="0" scope="page" />
        <c:set var="proteinMapWidth" value="875" scope="page" />
        <c:set var="proteinMapDomainHeight" value="15" scope="page" />
        <c:set var="proteinMapDomainHeightSpace" value="5" scope="page" />
        <c:set var="proteinMapBaseWidth" value="${(dto.max - dto.min) / 3}" scope="page" />
        
        <script>
        var urls = {};
        var subsections = {};
        </script>
        
        <div id='proteinMap' style="position:relative; width:${proteinMapWidth}px;border-bottom:1px solid black;margin:30px 0px;">
	        <c:forEach var="subsection" varStatus="status" items="${dto.domainInformation}" >
	               
	               <div class='proteinMapDomainSection' id='subsectionhit-${proteinMapCount}' title='${subsection.uniqueName} - ${subsection.description}' style="font-size:small;text-align:right;  background: rgb(222,222,222) ;  cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace) - (proteinMapDomainHeightSpace/2)  }px; height: ${ (fn:length(subsection.subfeatures) + 1 ) * (proteinMapDomainHeight+proteinMapDomainHeightSpace) }px; width: ${proteinMapWidth }px; " > ${subsection.uniqueName} </div>
	               
	               
	               <c:if test="${subsection.url != null}">
                        <script>
                        urls["subsectionhit-${proteinMapCount}"] = "${subsection.url}"; 
                        </script>
                    </c:if>
                   
                   <c:set var="proteinMapCountSubsection" value="${proteinMapCount}" scope="page" />
	               <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
	               
	               
	             <c:forEach var="hit" items="${subsection.subfeatures}"  varStatus="substatus" >
                    
	                <div class='proteinMapDomain' id='hit-${proteinMapCount}' title='${hit.uniqueName} - ${hit.description}' style="background: rgb(${hit.color.red},${hit.color.green},${hit.color.blue}) ;  cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px; left: ${ ( hit.fmin / proteinMapBaseWidth ) * proteinMapWidth  }px; width: ${ ( (hit.fmax - hit.fmin) / proteinMapBaseWidth ) * proteinMapWidth  }px; height:15px; " >&nbsp;</div>
	                
	                <c:if test="${hit.url != null}">
		                <script>
		                urls["hit-${proteinMapCount}"] = "${hit.url}"; 
		                subsections["hit-${proteinMapCount}"] = "subsectionhit-${proteinMapCountSubsection}";
				        </script>
			        </c:if>
	                <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
	                
	             </c:forEach>
	        </c:forEach>
	        
	        
	        <c:if test="${hasAlgorithmData}">
	           
	           <div class='proteinMapDomainSection' id='subsectionhit-${proteinMapCount}' title='TMHMM' style="font-size:small;text-align:right;  background: rgb(222,222,222) ;  cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace) - (proteinMapDomainHeightSpace/2)  }px; height: ${ (fn:length(dto.algorithmData) + 1) * (proteinMapDomainHeight+proteinMapDomainHeightSpace) }px; width: ${proteinMapWidth }px; " > Algorithm Data </div>
	           <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
	           
                <c:if test="${dto.algorithmData.TMHMM != null}">
                     <c:forEach var="helix" varStatus="status" items="${dto.algorithmData.TMHMM}">
                         <c:set var="t" value="${fn:split(helix,'-')}" scope="page" />
                         <div class='proteinMapDomain' id='hit-${proteinMapCount}' title='TMHMM - ${helix}' style="background: blue; cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px; left: ${ ( t[0] / proteinMapBaseWidth ) * proteinMapWidth  }px; width: ${ ( (t[1]-t[0]) / proteinMapBaseWidth ) * proteinMapWidth  }px; height:15px; " >&nbsp;</div> 
                    </c:forEach>
                    <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
                </c:if>
                
                <c:if test="${dto.algorithmData.SignalP != null}">
                    <div class='proteinMapDomain' id='hit-${proteinMapCount}' title='Signal P' style="background: green; cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px; left: ${ ( 1 / proteinMapBaseWidth ) * proteinMapWidth  }px; width: ${ ( (dto.algorithmData.SignalP.cleavageSite) / proteinMapBaseWidth ) * proteinMapWidth  }px; height:15px; " >&nbsp;</div>
                    <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
                </c:if>
                
                <c:if test="${dto.algorithmData.DGPI != null && dto.algorithmData.DGPI.anchored}">
                    <div class='proteinMapDomain' id='hit-${proteinMapCount}' title='DGPI' style="background: orange; cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px; left: ${ ( dto.algorithmData.DGPI.location / proteinMapBaseWidth ) * proteinMapWidth  }px; width: ${ ( (proteinMapBaseWidth -  dto.algorithmData.DGPI.location) / proteinMapBaseWidth ) * proteinMapWidth  }px; height:15px; " >&nbsp;</div>
                    <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
                </c:if>
                
            </c:if>
            
            <c:if test="${dto.membraneStructureComponents != null }">
                <c:if test="${fn:length(dto.membraneStructureComponents) > 0}" >
                    
                    <div class='proteinMapDomainSection' id='subsectionhit-${proteinMapCount}' title='Membrane' style="font-size:small;text-align:right;  background: rgb(222,222,222) ;  cursor:pointer;position:absolute;top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace) - (proteinMapDomainHeightSpace/2)  }px; height: ${ (2) * (proteinMapDomainHeight+proteinMapDomainHeightSpace) }px; width: ${proteinMapWidth }px; " > Membrane </div>
                    <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
                
                    <c:forEach var="component" varStatus="status" items="${dto.membraneStructureComponents}">
                        <c:choose>
                            
                            <c:when test="${component.compartment == 'cytoplasmic' }">
                                <c:set var="componentStyle" value="height:1px; top: ${(proteinMapCount) * (proteinMapDomainHeight + proteinMapDomainHeightSpace) + 14}px;" scope="page"/>
                            </c:when>
                            <c:when test="${component.compartment == 'noncytoplasmic' }">
                                <c:set var="componentStyle" value="height:1px; top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px; " scope="page"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="componentStyle" value="height:15px; top: ${proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace)}px;" scope="page"/>
                            </c:otherwise>
                            
                        </c:choose>
                    
                        <div class='proteinMapDomain' id='hit-${proteinMapCount}' title='${component.uniqueName}' style="${componentStyle} background: blue; cursor:pointer;position:absolute; left: ${ ( component.fmin / proteinMapBaseWidth ) * proteinMapWidth  }px; width: ${ ( (component.fmax - component.fmin) / proteinMapBaseWidth ) * proteinMapWidth  }px;  " >&nbsp;</div>  
                    </c:forEach>
                    <c:set var="proteinMapCount" value="${proteinMapCount + 1}" scope="page"/>
                </c:if>
                
            </c:if>
            
            
	        
	        <c:forEach var="i" begin="0" end="${proteinMapBaseWidth}" step="${proteinMapStep}" varStatus="istatus">
                <div style="font-size:x-small;position:absolute; bottom:-20px; left: ${ ( i / proteinMapBaseWidth ) * proteinMapWidth  }px; ">${i}</div>
            </c:forEach>
            
            <c:forEach var="i" begin="50" end="${proteinMapBaseWidth}" step="${proteinMapStep/2}" varStatus="istatus">
                <div style="position:absolute; height:5px; border-left:1px solid black; bottom:-5px; left: ${ ( i / proteinMapBaseWidth ) * proteinMapWidth  }px; "></div>
            </c:forEach>
            
            
        
        </div>
        
        
        <script>
	        $(document).ready(function() { 
	        	$('#proteinMap').css("height", parseInt("${(proteinMapCount) * (proteinMapDomainHeight+proteinMapDomainHeightSpace)}"));
	        	
	        	var darkBackground = "rgb(0,0,0)";
	        	var lightBackground = "rgb(222,222,222)";
	        	
	        	var darkColor = "rgb(0,0,0)";
	        	var lightColor = "rgb(255,255,255)";
	        	
	        	$('.proteinMapDomain,.proteinMapDomainSection').click(function(e) {
	        		if (e.target.id in urls)
	        		    window.location = urls[e.target.id];
	        	});
	        	
	        	$('.proteinMapDomain').hover(function(e) {
	        		$(this).stop().fadeTo('slow', 1);
	        		var subsection = subsections[e.target.id];
	        		
	        		$("#" + subsection).stop().animate({
                        backgroundColor: darkBackground,
                        color : lightColor
                    }, 'slow');
	        		
	        	}, function(e) {
	        		$(this).stop().fadeTo('fast', 0.5);
	        		var subsection = subsections[e.target.id];
	        		$("#" + subsection).stop().animate({
                        backgroundColor: lightBackground,
                        color : darkColor
                    }, 'slow');
	        		
	        	}).fadeTo('fast', 0.5);
	        	
	        	$('.proteinMapDomainSection').hover(function(e) {
                    $(this).stop().animate({
                        backgroundColor: darkBackground,
                        color : lightColor
                    }, 'slow');
                    
                }, function(e) {
                	$(this).stop().animate({
                        backgroundColor: lightBackground,
                        color : darkColor
                    }, 'slow');
                });
	        	
	        });
        </script>
        
        <br>
        
</format:genePageSection>
</c:if>


<c:if test="${fn:length(dto.domainInformation) > 0}">
  <format:genePageSection>
  <h2>Domain Information</h2>
  <table cellpadding="4" cellspacing="4" border="0" class="sequence-table">
  <tr>
    <td colspan="3"></td>
    <td class="domainPosition">Position</td>
    <td class="domainScore">Score</td>
    <td class="domainSignificance">Significance</td>
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
        <td>${hit.significance}</td>
      </tr>
    </c:forEach>
  </c:forEach>
  </table>
</format:genePageSection>
</c:if>

</c:if>

<%-- Ortholog / Paralog Section --%>
<c:if test="${fn:length(dto.clusterIds) + fn:length(dto.orthologueNames) > 0}">
  <format:genePageSection>
  <h2>Orthologues and Paralogues</h2>
  <table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
    <c:forEach items="${dto.clusterIds}" var="clusterId">
      <tr><td>${clusterId}</td><td><a href="<misc:url value="/Query/proteinMatchClusterOrthologue?clusterName=${clusterId}&extraParam=extraExtra" />">Look up others in cluster</a></td></tr>
    </c:forEach>
    <tr><td></td><td></td></tr>
    
    <c:if test="${!empty orthologues}">
        <tr><th>Curated Orthologues</th><td>
        <c:forEach items="${orthologues}" var="orthologue">
            <a href="<misc:url value="/gene/${orthologue}" />" >${orthologue}</a> 
        </c:forEach>
    </td></tr>
    </c:if>
  </table>
  </format:genePageSection>
</c:if>



<!-- <p>See this gene in <a href="<misc:url value="/gene/${dto.uniqueName}.xml" />">XML</a> or <a href="<misc:url value="/gene/${dto.uniqueName}.json" />">JSON</a> formats</p> -->

