<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header name="Controlled Vocabulary Term" />


<h3>Naming</h3>

<dl>
	<dt><b>Name:</b></dt>
	<dd>${cvTerm.name}</dd>

	<dt><b>CV:</b></dt>
	<dd>${cvTerm.cv.name}</dd>

	<dt><b>Definition:</b></dt>
	<dd>${cvTerm.definition}</dd>

	<dt><b>Primary Db-Xref:</b></dt>
	<dd>${cvTerm.dbxref.db.name}:${cvTerm.dbxref.accession}&nbsp;&nbsp;&nbsp;&nbsp;${cvTerm.dbxref.description}
	<dt><b>Obsolete?:</b></dt>
	<dd>${cvTerm.isObsolete}</dd>
</dl>



<h3>CvTerm Relationships</h3>
<h5>This feature is subject</h5>

<table>
	<c:forEach items="${cvTerm.cvtermRelationshipsForSubjectId}"
		var="cvTermRel">
		<tr>
			<td>this</td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.cvtermByTypeId.name}&cvName=${cvTermRel.cvtermByTypeId.cv.name}">${cvTermRel.cvtermByTypeId.name}</a></td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.cvtermByObjectId.name}&cvName=${cvTermRel.cvtermByObjectId.cv.name}">${cvTermRel.cvtermByObjectId.name}</a></td>
		</tr>

	</c:forEach>
	</table>

		<h5>This feature is object</h5>
<table>
	<c:forEach items="${cvTerm.cvtermRelationshipsForObjectId}"
		var="cvTermRel">
		<tr>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.cvtermBySubjectId.name}&cvName=${cvTermRel.cvtermBySubjectId.cv.name}">${cvTermRel.cvtermBySubjectId.name}</a></td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.cvtermByTypeId.name}&cvName=${cvTermRel.cvtermByTypeId.cv.name}">${cvTermRel.cvtermByTypeId.name}</a></td>
			<td>this</td>
		</tr>

	</c:forEach>
	</table>


	<format:footer />