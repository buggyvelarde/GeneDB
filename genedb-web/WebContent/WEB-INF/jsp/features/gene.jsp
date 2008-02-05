<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header1 name="Gene: ${feature.displayName}">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/wtsi.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/frontpage1.css" type="text/css"/>
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/extjs/ext-all.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
</format:header1>
<table width="100%">
<tr>
	<td width="20%">
		<div class="fieldset">
		<div class="legend">Quick Search</div>
			<br>
			<form name="query" action="<c:url value="/"/>NamedFeature" method="get">
			<table>
				<tr>
					<td>Gene Name: </td>
					<td><input id="query" name="name" type="text" size="12"/></td>
				</tr>
				<%--<tr>
					<td><input type="hidden" name="orgs" value="${organism}"/></td>
				</tr> --%>
				<tr>
					<td><input type="submit" value="submit"/></td>
					<td><br></td>
				</tr>
			</table>
			</form>
		</div>
		<div class="fieldset">
		<div class="legend">Navigation</div>
			<br>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" id="active_menu">Home</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel">News</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Links</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Search</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >FAQs</a></td></tr>
			</table>
		</div>
		<%--   <div class="fieldset">	
		<div class="legend">History</div>
			<br>
			<div id="topic-grid"/>
		</div> --%>
	</td>
	<td width="100%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Gene Details</div>
			<br>
			<p>See <a href="http://www.genedb.org/genedb/Search?organism=All:*&name=${feature.uniqueName}">corresponding gene</a> in production GeneDB</p>

		<st:section name="Naming" id="gene_naming" collapsed="false" collapsible="false" hideIfEmpty="true">
		        
		  <table>	
		  <db:synonym name="primary_name" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Name:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		  <db:synonym name="protein_name" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Protein:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		  <db:synonym name="systematic_id" var="name" collection="${feature.featureSynonyms}" tmpSysId="tmpSysId">
		    <tr><td><b>Systematic id:</b></td><td> <db:list-string collection="${name}" /> 
		    <c:if test="${tmpSysId}"><span class="warning">(Note: this id is temporary and will change in future)</span></c:if></td></tr>
		  </db:synonym>
		  <db:synonym name="previous_systematic_id" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Prev. systematic id:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		  <db:synonym name="synonym" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Synonym:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		  <db:synonym name="obsolete_name" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Obsolete Name:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		  <db:synonym name="reserved_name" var="name" collection="${feature.featureSynonyms}">
		    <tr><td><b>Reserved Name:</b></td><td> <db:list-string collection="${name}" /></td></tr>
		  </db:synonym>
		
		  <tr><td colspan="2">&nbsp;</td>
		  
			<tr><td><b>Type:</b></td><td>${feature.cvTerm.name}</td></tr>      
			<tr><td><b>Organism:</b></td><td>${feature.organism.genus} ${feature.organism.species}</td></tr>  
			<tr><td><b>Product:</b></td><td><c:forEach items="${polypeptide.featureCvTerms}" var="featCvTerm">
						<c:if test="${featCvTerm.cvTerm.cv.name == 'genedb_products'}">
							<dd>${featCvTerm.cvTerm.name}</dd>
						</c:if>
					</c:forEach></td></tr>
					
		  </table>	
		  <c:forEach items="${feature.featureLocsForFeatureId}" var="featLoc">
			  <c:set var="start" value="${featLoc.fmin}"/>
			  <c:set var="end" value="${featLoc.fmax}"/>
			  <c:set var="chromosome" value="${featLoc.featureBySrcFeatureId.uniqueName}"/>
		  </c:forEach>
		  <a href="ArtemisLaunch?organism=${feature.organism.commonName}&chromosome=${chromosome}&start=${start}&end=${end}">Show region in Artemis</a>
		</st:section>
		<st:section name="Context Map" id="context_map" collapsed="false" collapsible="true" hideIfEmpty="false">
			<misc:contextmap gene="${feature}"></misc:contextmap>
			 <img src="<c:url value="/"/>includes/images/cmap/${feature.uniqueName}.gif" usemap="#context"/>
		</st:section>
		<st:section name="Location" id="gene_location" collapsed="false" collapsible="true" hideIfEmpty="true">
			<c:forEach items="${feature.featureLocsForFeatureId}" var="featLoc">
				<p>[${featLoc.rank}]&nbsp;&nbsp;${featLoc.strand}&nbsp;&nbsp;${featLoc.fmin}...${featLoc.fmax} on <i>to be done</i></p>
			</c:forEach>
	
		
		</st:section>
		
		
		<st:section name="Note" id="gene_note" collapsed="false" collapsible="true" hideIfEmpty="true">
		  <db:propByName collection="${polypeptide.featureProps}" name="note" var="props">
		    <c:forEach items="${props}" var="featProp">
		      <br /><db:highlight>${featProp.value}</db:highlight>
		    </c:forEach>
		  </db:propByName>
		</st:section>
		
		<st:section name="Curation" id="gene_curation" collapsed="false" collapsible="true" hideIfEmpty="true">
		<db:comment polypeptide="${polypeptide}"></db:comment>
		-- Controlled curation --
		<db:curation polypeptide="${polypeptide}"></db:curation>
		<br>-- Curation --
		  <db:propByName collection="${polypeptide.featureProps}" name="curation" var="props">
		    <c:forEach items="${props}" var="featProp">
		      <br /><db:highlight>${featProp.value}</db:highlight>
		    </c:forEach>
		  </db:propByName>
		</st:section>
		
		<%--
		<st:section name="Structure - wouldn't necessarily be shown" id="gene_structure" collapsed="false" collapsible="true" hideIfEmpty="true">
		  <h5>This feature is object</h5>
		    <table>
		      <c:forEach items="${feature.featureRelationshipsForObjectId}" var="featRel">
		        <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
		        <td><a href="./FeatureByName?name=${featRel.featureBySubjectId.uniqueName}">${featRel.featureBySubjectId.displayName}</a> [${featRel.featureBySubjectId.cvTerm.name}]</td>
		        <td> is ${featRel.cvTerm.name}</td><td>this</td></tr>
		      </c:forEach>
		    </table>
		  
		    <h5>This feature is subject</h5>
		        	
		        	<table>
		        <c:forEach items="${feature.featureRelationshipsForSubjectId}" var="featRel">
		     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
		     		  <td>this</td>
		     		  <td> is ${featRel.cvTerm.name}</td>
		     		  <td><a href="./FeatureByName?name=${featRel.featureByObjectId.uniqueName}">${featRel.featureByObjectId.displayName}</a> [${featRel.featureByObjectId.cvTerm.name}]</td></tr>
		
				   </c:forEach>
		        	</table>
		
		</st:section>
		--%>
		<st:section name="Predicted Peptide Properties" id="gene_pepprop" collapsed="false" collapsible="true" hideIfEmpty="true">
		<table class="simple">
			<tr>
				<td><b>Isoelectric Point</b></td>
				<td>pH ${polyprop.isoelectricPoint} </td>
				<td><b>Mass</b></td>
				<td>${polyprop.mass} kDa </td>
			</tr>
			<tr>
				<td><b>Charge</b></td>
				<td>${polyprop.charge}</td>
				<td><b>Amino Acids</b></td>
				<td>${polyprop.aminoAcids}</td>
			</tr>
		</table>
		</st:section>
		
		<st:section name="Gene Ontology Annotation" id="gene_go" collapsed="false" collapsible="true" hideIfEmpty="true">
		    <table border="1">
		      <format:go-section title="Biological Process" cvName="biological_process" feature="${polypeptide}" />
		      <format:go-section title="Cellular Component" cvName="cellular_component" feature="${polypeptide}" />
		      <format:go-section title="Molecular Function" cvName="molecular_function" feature="${polypeptide}" />  
		    </table>
		</st:section>
		
		<st:section name="Catalytic Activity" id="gene_catalytic" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>

		<st:section name="Phenotype" id="gene_phenotype" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Literature" id="gene_literature" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Domain Information" id="gene_domain" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		
		<st:section name="Database Cross-References" id="gene_xref" collapsed="false" collapsible="true" hideIfEmpty="true">
					<c:if test="${!empty feature.dbXRef}">
					   <c:set var="dbxref" value="${feature.dbXRef}" />
					   <p><b>Xref:</b> ${dbXRef.db.name}:${dbXRef.accession} : ${dbXRef.description}</p>
					   <c:remove var="dbxref"/>
					</c:if>
		<p>---</p>
		  <c:forEach items="${polypeptide.featureDbXRefs}" var="fdx">
		    <br /><a href="${fdx.dbXRef.db.urlPrefix}${fdx.dbXRef.accession}">${fdx.dbXRef.db.name}:${fdx.dbXRef.accession}</a>&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbXRef.description}
		  </c:forEach>
		
		
		</st:section>
		
		<st:section name="Orthologues" id="gene_orthologues" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Paralogue/Family" id="gene_paralogues" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Database Similarities" id="gene_dbsimilarity" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Similarity" id="gene_similarity" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		<st:section name="Uniprot Annotation For This Protein" id="gene_uniprot" collapsed="false" collapsible="true" hideIfEmpty="true">
		</st:section>
		
		
		<h3>Feature Relationships</h3>
		
		
		<h3>Database X-refs</h3>
		
		
		<st:section name="Modified" collapsed="false" collapsible="true" hideIfEmpty="true">
			<b>Date created:</b>         ${feature.timeAccessioned}<br>  
			<b>Date last modified:</b>   ${feature.timeLastModified}<br>
			<div align="left">
			<ul>
			<c:forEach items="${modified}" var="line">
				<li>${line}</li>
			</c:forEach>
			</ul>
			</div>
		</st:section>
	</td>
</tr>
</table>
<format:footer />