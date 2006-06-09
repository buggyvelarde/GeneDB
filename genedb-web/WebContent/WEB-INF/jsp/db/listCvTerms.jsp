<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header name="Generic Feature Page" />

<table>
	<c:forEach items="${cvTerms}" var="cvTerm">
		<tr>
			<td><a href="./CvTermByCvName?cvTermName=${cvTerm.name}&cvName=${cvTerm.cv.name}">${cvTerm.name}</a></td>
			<td>${cv.definition}</td>
			<td>${cvTerm.cv.name}</td>
		</tr>
	</c:forEach>
</table>

<format:footer />
