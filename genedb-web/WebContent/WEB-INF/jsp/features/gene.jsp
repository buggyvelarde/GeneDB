<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<format:header name="Gene: ${feature.displayName}">
	<st:init />
</format:header>

<st:section name="Naming" id="gene_naming" collapsed="false" collapsible="false" hideIfEmpty="true">
        	
  <db:synonym name="primary_name" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Name:</b> <db:list-string collection="${name}" />
  </db:synonym>
  <db:synonym name="protein_name" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Protein:</b> <db:list-string collection="${name}" />
  </db:synonym>
  <db:synonym name="systematic_id" var="name" collection="${feature.featureSynonyms}" tmpSysId="tmpSysId">
    <br /><b>Systematic id:</b> <db:list-string collection="${name}" /> 
    <c:if test="${tmpSysId}"><span class="warning">(Note: this id is temporary and will change in future)</span></c:if>
  </db:synonym>
  <db:synonym name="previous_systematic_id" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Prev. systematic id:</b> <db:list-string collection="${name}" />
  </db:synonym>
  <db:synonym name="synonym" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Synonym:</b> <db:list-string collection="${name}" />
  </db:synonym>
  <db:synonym name="obsolete_name" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Obsolete Name:</b> <db:list-string collection="${name}" />
  </db:synonym>
  <db:synonym name="reserved_name" var="name" collection="${feature.featureSynonyms}">
    <br /><b>Reserved Name:</b> <db:list-string collection="${name}" />
  </db:synonym>
        	
        	
        	<dl>
        	
			<dt><b>Type:</b></dt>
			<dd>${feature.cvTerm.name}</dd>
        	
        	<dt><b>Analysis Feature:</b></dt>
        	<dd>${feature.analysis}</dd>
        	
        	<dt><b>Obsolete?:</b></dt>
        	<dd>${feature.obsolete}</dd>
        	
        	<dt><b>Date created:</b></dt>
        	<dd>${feature.timeAccessioned}</dd>
        	
        	<dt><b>Date last modified:</b></dt>
        	<dd>${feature.timeLastModified}</dd>
        	
			<dt><b>Organism:</b></dt>
			<dd>${feature.organism.genus} ${feature.organism.species}</dd>
</dl>
</st:section>


<st:section name="Location" id="gene_location" collapsed="false" collapsible="true" hideIfEmpty="true">
	<c:forEach items="${feature.featurelocsForFeatureId}" var="featLoc">
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
  <db:propByName collection="${polypeptide.featureProps}" name="curation" var="props">
    <c:forEach items="${props}" var="featProp">
      <br /><db:highlight>${featProp.value}</db:highlight>
    </c:forEach>
  </db:propByName>
</st:section>

<st:section name="Private - wouldn't really be shown" id="gene_private" collapsed="false" collapsible="true" hideIfEmpty="true">
  <db:propByName collection="${polypeptide.featureProps}" name="private" var="props">
    <c:forEach items="${props}" var="featProp">
      <br /><db:highlight>${featProp.value}</db:highlight>
    </c:forEach>
  </db:propByName>
</st:section>

<st:section name="Structure - wouldn't necessarily be shown" id="gene_structure" collapsed="false" collapsible="true" hideIfEmpty="true">
  <h5>This feature is object</h5>
    <table>
      <c:forEach items="${feature.featureRelationshipsForObjectId}" var="featRel">
        <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
        <td><a href="./FeatureByName?name=${featRel.featureBySubjectId.uniquename}">${featRel.featureBySubjectId.uniquename}</a> [${featRel.featureBySubjectId.cvTerm.name}]</td>
        <td> is ${featRel.cvterm.name}</td><td>this</td></tr>
      </c:forEach>
    </table>
  
    <h5>This feature is subject</h5>
        	
        	<table>
        <c:forEach items="${feature.featureRelationshipsForSubjectId}" var="featRel">
     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
     		  <td>this</td>
     		  <td> is ${featRel.cvterm.name}</td>
     		  <td><a href="./FeatureByName?name=${featRel.featureByObjectId.uniquename}">${featRel.featureByObjectId.uniquename}</a> [${featRel.featureByObjectId.cvTerm.name}]</td></tr>

		   </c:forEach>
        	</table>

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
			<c:if test="${!empty feature.dbxref}">
			   <c:set var="dbxref" value="${feature.dbxref}" />
			   <p><b>Xref:</b> ${dbxref.db.name}:${dbxref.accession} : ${dbxref.description}</p>
			   <c:remove var="dbxref"/>
			</c:if>
<p>---</p>
  <c:forEach items="${polypeptide.featureDbxrefs}" var="fdx">
    <br /><a href="${fdx.dbxref.db.urlprefix}${fdx.dbxref.accession}">${fdx.dbxref.db.name}:${fdx.dbxref.accession}</a>&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbxref.description}
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



		   
<format:footer />