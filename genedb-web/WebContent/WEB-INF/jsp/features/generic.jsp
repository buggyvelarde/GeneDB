<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Generic Feature Page" />
        	

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
        	<table>

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
		   
<h3>Feature Locations</h3>

        	
           <c:forEach items="${feature.featurelocsForFeatureId}" var="featLoc">
     		  <p>[${featLoc.rank}]&nbsp;&nbsp;${featLoc.strand}&nbsp;&nbsp;${featLoc.fmin}...${featLoc.fmax} on ${featLoc.featureBySrcfeatureId.uniquename}</p>
		   </c:forEach>	
        	
           <c:forEach items="${feature.featurelocsForSrcfeatureId}" var="featLoc">
               		  <p>[${featLoc.rank}]&nbsp;&nbsp;${featLoc.strand}&nbsp;&nbsp;${featLoc.fmin}...${featLoc.fmax} on ${featLoc.featureByFeatureId.uniquename}</p>
		   </c:forEach>		   

<format:footer />