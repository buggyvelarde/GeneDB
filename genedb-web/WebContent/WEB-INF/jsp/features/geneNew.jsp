<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<c:url value="/" var="base"/>

<c:set var="primaryLoc" value="${feature.rankZeroFeatureLoc}" />
<c:set var="chromosome" value="${primaryLoc.featureBySrcFeatureId}" />

<format:headerRound name="Gene: ${feature.displayName}" title="Gene Page ${feature.displayName}"
		onLoad="initContextMap('${base}', '${feature.organism.commonName}', '${chromosome.uniqueName}', ${chromosome.seqLen}, ${primaryLoc.fmin}, ${primaryLoc.fmax})">
	<base href="<c:url value="/"/>">
	<st:init />
	<%-- The next two are used by the scrollable context map --%>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/contextMap.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
</format:headerRound>
<!-- Context Map -->
<div id="contextMapOuterDiv">
	<div id="contextMapTopPanel">
	    <div id="contextMapThumbnailDiv"></div>
	    <div id="contextMapGeneInfo"></div>
	</div>
	<div id="contextMapDiv">
	    <img src="<c:url value="/includes/images/default/grid/loading.gif"/>" id="contextMapLoadingImage">
	</div>
</div>

<!-- General Information -->
<div id="genInfo">
    <div id="one">
	<format:roundStart/>
  <div class="inner" style="height:160px;">
   <span><b>General Information</b></span><br><br>
   <table cellpadding="3px;" width="100%">
     <tr>
       <th>Gene Name</th>
       <td>${feature.displayName}</td>
       <db:synonym name="synonym" var="name"
				collection="${feature.featureSynonyms}">
					<th>Synonym</th>
					<td><db:list-string collection="${name}" /></td>
			</db:synonym>
     </tr>
     <tr>
       <th>Systematic Name</th>
       <td>${feature.uniqueName}</td>
       <db:synonym name="obsolete_name" var="name"
				collection="${feature.featureSynonyms}">
					<th>Previous IDs</th>
					<td><db:list-string collection="${name}" /></td>
		</db:synonym>
     </tr>
     <tr>
       <th>Protein names</th>
				<td><c:forEach items="${polypeptide.featureCvTerms}"
					var="featCvTerm">
					<c:if test="${featCvTerm.cvTerm.cv.name == 'genedb_products'}">
						<span>
						${featCvTerm.cvTerm.name}
						</span><br>
					</c:if>
				</c:forEach></td>
     </tr>
     <tr>
       <th>Location</th>
		<td><c:forEach items="${feature.featureLocsForFeatureId}"
			var="featLoc">
			<c:set var="start" value="${featLoc.fmin}" />
			<c:set var="end" value="${featLoc.fmax}" />
			<c:set var="chromosome"
				value="${featLoc.featureBySrcFeatureId.uniqueName}" />
			<span>${start}..${end}</span>
		</c:forEach></td>
		<th>Chromosome</th>
		<td><span>${chromosome}</span></td>		
     </tr>
   </table>
  </div>
  <format:roundEnd/>
</div>
<div id="two">
<format:roundStart/>
  <div class="inner" style="height:160px;" align="center">
		<span>Send</span>
		<br>
		<form name="" action=""><select name="type">
			<option value="dna">Nucleotide</option>
			<option value="protein">Protein</option>
		</select> to <select name="analysis">
			<option value="blast">Blast</option>
			<option value="omni">omniBlast</option>
		</select></form>
		<br><br>
		<span>Download Region</span>
		<br>
		<form name="" action="">as <select name="type">
			<option value="fasta">FASTA</option>
			<option value="embl">EMBL</option>
		</select>
		</form><br>
		<a href="">GBrowse</a>&nbsp;&nbsp;&nbsp;<a href="">Synview</a>
  </div>
  <format:roundEnd/>
</div>
</div>

<!-- Controlled Curation Section -->
<div id="controlCur" style="clear: both;">
  <div class="outer">
  <format:roundStart/>
  <div class="inner">
  	<span><b>Controlled Curation</b></span>
   	<table width="100%">
		<format:go-section title="Controlled Curation"
				cvName="CC_genedb_controlledcuration" feature="${polypeptide}" />
	</table>
  </div>
  <format:roundEnd/>
 </div>
</div>

<!-- Gene Ontology Section -->
<div id="go" style="clear: both;">
  <div class="outer">
  <format:roundStart/>
  <div class="inner">
  	<span><b>Gene Ontology</b></span>
   	<table width="100%">
   		<tr>
   			<th><b>Biological Process</b></th>
   			<td>
   				<table>
				<format:go-section title="Biological Process"
					cvName="biological_process" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
	<table width="100%">
		<tr>
   			<th><b>Cellular Component</b></th>
   			<td>
   				<table>
				<format:go-section title="Cellular Component"
					cvName="cellular_component" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
	<table width="100%">
		<tr>
   			<th><b>Molecular Function</b></th>
   			<td>
   				<table>
				<format:go-section title="Molecular Function"
					cvName="molecular_function" feature="${polypeptide}" />
				</table>
			</td>
		</tr>
	</table>
  </div>
  <format:roundEnd/>
  </div>
</div>

<!-- Predicted Peptide Section -->
<div id="predictedpep" style="clear: both;">
    <div id="two">
	<format:roundStart/>
  <div class="inner" style="height:160px">
  	<span><b>Predicted Peptide Properties</b></span>
   	<table class="simple">
		<tr>
			<td><b>Isoelectric Point</b></td>
			<td>pH ${polyprop.isoelectricPoint}</td>
		</tr>
		<tr>
			<td><b>Mass</b></td>
			<td>${polyprop.mass} kDa</td>
		</tr>
		<tr>
			<td><b>Charge</b></td>
			<td>${polyprop.charge}</td>
		</tr>
		<tr>
			<td><b>Amino Acids</b></td>
			<td>${polyprop.aminoAcids}</td>
		</tr>
	</table>
  </div>
  <format:roundEnd/>
 </div>
 <div id="one">
 <format:roundStart/>
  <div class="inner" style="height:160px;">
  	<span><b>Protein Map</b></span><br></br>
  	<div align="center">	
   		<img src="<c:url value="/includes/images/protein.gif"/>" id="ProteinMap">
   	</div>
  </div>
  <format:roundEnd/>
 </div>
</div>

<!-- Domain Information -->
<div id="domainInfo" style="clear: both;">
</div>

<!-- Ortholog / Paralog Section -->
<div id="orthologs" style="clear: both;">
    <div class="outer">
	<format:roundStart/>
  <div class="inner">
  	<span><b>Orthologs / Paralogs</b></span>
   	<table>
   		<tr>
   		<td><db:ortholog polypeptide="${polypeptide}"/></td>
   		</tr>
	</table>
  </div>
  <b class="round">
  <b class="round5"></b>
  <b class="round4"></b>
  <b class="round3"></b>
  <b class="round2"><b></b></b>
  <b class="round1"><b></b></b></b>
 </div>
 <format:roundEnd/>
</div>

<format:footer />