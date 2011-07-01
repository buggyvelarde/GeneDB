<%@ tag display-name="searchbox"
        body-content="scriptless" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<div class="fieldset">
		<div class="legend">Quick Search</div>
			<br>
			<form name="query" action="<misc:url value="/"/>NameSearch" method="get">
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