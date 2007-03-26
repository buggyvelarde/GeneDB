<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header name="Name Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>

<p>This page allows you to look up a feature by name.

<form:form commandName="nameLookup" action="NamedFeature" method="post">
  <table>
    <tr><td colspan="3">
      <font color="red"><form:errors path="*" /></font>
    </td></tr>
    <tr>
      <td>Organisms:</td>
      <td><db:simpleselect /></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
    </tr>
    <tr>
	  <td>Look Up:</td>
	  <td><form:input path="lookup"/></td>
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
</form:form>

<format:footer />