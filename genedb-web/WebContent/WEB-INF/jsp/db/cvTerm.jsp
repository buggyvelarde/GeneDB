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
	<dd>${cvTerm.dbXRef.db.name}:${cvTerm.dbXRef.accession}&nbsp;&nbsp;&nbsp;&nbsp;${cvTerm.dbXRef.description}
	<dt><b>Obsolete?:</b></dt>
	<dd>${cvTerm.isObsolete}</dd>
</dl>



<h3>CvTerm Relationships</h3>
<h5>This feature is subject</h5>

<table>
	<c:forEach items="${cvTerm.cvTermRelationshipsForSubjectId}"
		var="cvTermRel">
		<tr>
			<td>this</td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.type.name}&cvName=${cvTermRel.type.cv.name}">${cvTermRel.type.name}</a></td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.object.name}&cvName=${cvTermRel.object.cv.name}">${cvTermRel.object.name}</a></td>
		</tr>

	</c:forEach>
	</table>

		<h5>This feature is object</h5>
<table>
	<c:forEach items="${cvTerm.cvTermRelationshipsForObjectId}"
		var="cvTermRel">
		<tr>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.subject.name}&cvName=${cvTermRel.subject.cv.name}">${cvTermRel.subject.name}</a></td>
			<td><a href="./CvTermByCvName?cvTermName=${cvTermRel.type.name}&cvName=${cvTermRel.type.cv.name}">${cvTermRel.type.name}</a></td>
			<td>this</td>
		</tr>

	</c:forEach>
	</table>


	<format:footer />