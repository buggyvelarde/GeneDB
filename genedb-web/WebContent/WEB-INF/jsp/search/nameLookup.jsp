<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Name Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

<p>This page allows you to look up a feature by name.

<sp:form commandName="nameLookup" action="NamedFeature" method="post">
  <table>
    <tr><td colspan="3">
      <c:forEach items="${status}" var ="errorMessage"> 
        <font color="red"><c:out value ="${errorMessage}"/><br></font>
      </c:forEach>
    </td></tr>
    <tr>
      <td>Organisms:</td>
      <td><db:simpleselect /></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
    </tr>
    <tr>
	  <td>Look Up:</td>
	  <td><sp:input path="lookup"/></td>
	  <td>The name to lookup. It can include wildcards (*) to match any series of characters</td>
    </tr>
    <tr>
	  <td>Feature Type:</td>
	  <td>Gene</td>
	  <td>Restrict the type of features searched for</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" /></td>
	  <td>&nbsp;</td>
    </tr>
  </table>
</sp:form>

<format:footer />