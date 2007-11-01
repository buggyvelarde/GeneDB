<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Generic Feature Page" />
        	

<h3>Naming</h3>
			<c:forEach items="${feature.featureSynonyms}" var="featSyn">
     			  <p><b>${featSyn.synonym.cvTerm.name}</b> ${featSyn.synonym.name} <c:if test="!${featSyn.current}">{Obsolete}</c:if></p>
			</c:forEach>
        	
        	<dl>
        	<dt><b>Name:</b></dt>
        	<dd>${feature.name}</dd>
        	
        	<dt><b>Unique name:</b></dt>
        	<dd>${feature.uniqueName}</dd>
        	
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

<h3>Feature Properties</h3>
        	
        	<table>
           <c:forEach items="${feature.featureProps}" var="featProp">
     		  <tr><td>[${featProp.rank}]</td><td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.cvTerm.name}</td><td>&nbsp;&nbsp;&nbsp;&nbsp;${featProp.value}</td></tr>
		   </c:forEach>
</table>


<h3>Feature Relationships</h3>
        	<h5>This feature is subject</h5>
        	
        	<table>
        <c:forEach items="${feature.featureRelationshipsForSubjectId}" var="featRel">
     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
     		  <td>this</td>
     		  <td> is ${featRel.cvTerm.name}</td>
     		  <td><a href="./FeatureByName?name=${featRel.featureByObjectId.uniqueName}">${featRel.featureByObjectId.uniqueName}</a> [${featRel.featureByObjectId.cvTerm.name}]</td></tr>

		   </c:forEach>
        	</table>

        	<h5>This feature is object</h5>
        	
        	<table>
           <c:forEach items="${feature.featureRelationshipsForObjectId}" var="featRel">
     		  <tr><td>${featRel.rank}</td><td>${featRel.value}</td>
     		  <td><a href="./FeatureByName?name=${featRel.featureBySubjectId.uniqueName}">${featRel.featureBySubjectId.uniqueName}</a> [${featRel.featureBySubjectId.cvTerm.name}]</td>
     		  <td> is ${featRel.cvTerm.name}</td><td>this</td></tr>
		   </c:forEach>
</table>

<h3>Database X-refs</h3>


			<c:if test="${!empty feature.dbXRef}">
			   <c:set var="dbxref" value="${feature.dbXRef}" />
			   <p><b>Xref:</b> ${dbxref.db.name}:${dbxRef.accession} : ${dbXRef.description}</p>
			   <c:remove var="dbxref"/>
			</c:if>
<p>---</p>
		   <c:forEach items="${feature.featureDbXRefs}" var="fdx">
     		  <p>[${fdx.current}]&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbXRef.db.name}:${fdx.dbXRef.accession}&nbsp;&nbsp;&nbsp;&nbsp;${fdx.dbxref.description}</p>
		   </c:forEach>		
		   
<h3>Feature Locations</h3>
 

<format:footer />