<%@ include file="/WEB-INF/jsp/topinclude.jspf"%>

<format:header name="Generic Feature Page" />

<table>
	<c:forEach items="${cvs}" var="cv">
		<tr>
			<td><a href="./FindCvByName?name=${cv.name}">${cv.name}</a></td>
			<td>is ${cv.name}</td>
		</tr>
	</c:forEach>
</table>

<format:footer />
