<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Gene: ${feature.uniquename}">
	<st:init />
</format:header>


<h3>Naming</h3>
			<c:forEach items="${feature.featureSynonyms}" var="featSyn">
     			  <p><b>${featSyn.synonym.cvterm.name}</b> ${featSyn.synonym.name} <c:if test="!${featSyn.isCurrent}">{Obsolete}</c:if></p>
			</c:forEach>
        	
        	<dl>
        	<dt><b>Name:</b></dt>
        	<dd>${feature.name}</dd>
        	
        	<dt><b>Unique name:</b></dt>
        	<dd>${feature.uniquename}</dd>
        	
			<dt><b>Type:</b></dt>
			<dd>${feature.cvterm.name}</dd>
        	
        	<dt><b>Analysis Feature:</b></dt>
        	<dd>${feature.isAnalysis}</dd>
        	
        	<dt><b>Obsolete?:</b></dt>
        	<dd>${feature.isObsolete}</dd>
        	
        	<dt><b>Date created:</b></dt>
        	<dd>${feature.timeaccessioned}</dd>
        	
        	<dt><b>Date last modified:</b></dt>
        	<dd>${feature.timelastmodified}</dd>
        	
			<dt><b>Organism:</b></dt>
			<dd>${feature.organism.genus} ${feature.organism.species}</dd>
</dl>

<st:section name="Location" id="gene_location" collapsed="false" collapsible="true" hideIfEmpty="true">
	<c:forEach items="${feature.featurelocsForFeatureId}" var="featLoc">
		<p>[${featLoc.rank}]&nbsp;&nbsp;${featLoc.strand}&nbsp;&nbsp;${featLoc.fmin}...${featLoc.fmax} on ${featLoc.featureBySrcfeatureId.uniquename}</p>
	</c:forEach>	
    	
	<c:forEach items="${feature.featurelocsForSrcfeatureId}" var="featLoc">
		<p>[${featLoc.rank}]&nbsp;&nbsp;${featLoc.strand}&nbsp;&nbsp;${featLoc.fmin}...${featLoc.fmax} on ${featLoc.featureByFeatureId.uniquename}</p>
	</c:forEach>	
</st:section>

<st:section name="Curation" id="gene_curation" collapsed="true" collapsible="true" hideIfEmpty="true">

<p>This is a crucial gene!</p>
</st:section>


<st:section name="Predicted Peptide Properties" id="gene_pepprop" collapsed="false" collapsible="true" hideIfEmpty="true">

<p>Yes, it has some</p>

</st:section>

<st:section name="Gene Ontology Annotation" id="gene_go" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>

<st:section name="Catalytic Activity" id="gene_catalytic" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>

<st:section name="Pathways" id="gene_pathway" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>

<st:section name="Published Expression Profiles" id="gene_expression" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>
<%-- MiscSectionsHelper.getStructureSection( gene ) --%>


<st:section name="Phenotype" id="gene_phenotype" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>

<st:section name="Literature" id="gene_literature" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>

<st:section name="Domain Information" id="gene_domain" collapsed="false" collapsible="true" hideIfEmpty="true">
</st:section>


<st:section name="Database Cross-References" id="gene_xref" collapsed="false" collapsible="true" hideIfEmpty="true">
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

<h3>Feature Properties</h3>
        	
        	<table>
           <c:forEach items="${feature.featureprops}" var="featProp">
     		  <tr><td>[${featProp.rank}]</td><td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.cvterm.name}</td><td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.value}</td></tr>
		   </c:forEach>
</table>


<h3>Feature Relationships</h3>
        	<h5>This feature is subject</h5>
        	
        	<table>
        <c:forEach items="${feature.featureRelationshipsForSubjectId}" var="featRel">
     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
     		  <td>this</td>
     		  <td> is ${featRel.cvterm.name}</td>
     		  <td><a href="./FeatureByName?name=${featRel.featureByObjectId.uniquename}">${featRel.featureByObjectId.uniquename}</a> [${featRel.featureByObjectId.cvterm.name}]</td></tr>

		   </c:forEach>
        	</table>

        	<h5>This feature is object</h5>
        	
        	<table>
           <c:forEach items="${feature.featureRelationshipsForObjectId}" var="featRel">
     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
     		  <td><a href="./FeatureByName?name=${featRel.featureBySubjectId.uniquename}">${featRel.featureBySubjectId.uniquename}</a> [${featRel.featureBySubjectId.cvterm.name}]</td>
     		  <td> is ${featRel.cvterm.name}</td><td>this</td></tr>
		   </c:forEach>
</table>

<h3>Database X-refs</h3>


			<c:if test="${!empty feature.dbxref}">
			   <c:set var="dbxref" value="${feature.dbxref}" />
			   <p><b>Xref:</b> ${dbxref.db.name}:${dbxref.accession} : ${dbxref.description}</p>
			   <c:remove var="dbxref"/>
			</c:if>
<p>---</p>
		   <c:forEach items="${feature.featureDbxrefs}" var="fdx">
     		  <p>[${fdx.isCurrent}]&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbxref.db.name}:${fdx.dbxref.accession}&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbxref.description}</p>
		   </c:forEach>
		   
<format:footer />