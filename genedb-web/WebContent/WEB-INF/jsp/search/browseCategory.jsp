<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

<format:header name="Browse By Category">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

<p>This is a page for launching a browse by category search

<form:form action="/BrowseCategory" commandName="browseCategory" method="get">
<table>
<tr><td><form:errors path="*" /></td></tr>
    <tr>
      <td>Organisms:</td>
      <td><db:simpleselect /></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
    </tr>
    <tr>
	  <td>Browse category:</td>
	  <td><form:select path="category" items="${categories}" /></td>
	  <td></td>
    </tr>
    <!-- <tr>
	  <td>Feature Type:</td>
	  <td>Gene</td>
	  <td>Restrict the type of features searched for</td>
    </tr> -->
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" /></td>
	  <td>&nbsp;</td>
    </tr>

</table>
</form:form>

<format:footer />