<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<script type="text/javascript">
	function doSomething() {
		var obj = document.getElementById("start");
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		var items = document.getElementById("itemsLength");
		var ilength = items.getAttribute("value");

		for (var i=0; i< ilength; i++) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			var depth = element.getAttribute("name").split('_');
			element.style.left = ( depth.length * 154 ) + 1;
			var top = -1;
			for (j=0;j<depth.length;j++) {
				top = top + depth[j] * 24;
 			}
 			element.style.top = top;
		}
		//alert(curleft);
		//alert(curtop);
	}
	
	function mouseclick(id) {
		var element = document.getElementById("menu_" + id);
		var elem = document.getElementById("organism");
		var selected = document.getElementById("selected");
		selected.value = element.textContent;
		elem.textContent = element.textContent;
		hideAllMenus();
	}
</script>
<style type="text/css">
#container{
position: relative;
}
</style>
<format:header name="Browse By Term">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<body onload="doSomething()">
<p>This is a page for a browse by term search

<form:form action="BrowseTerm" commandName="browseTerm" method="get">
<table>
<tr><td><form:errors path="*" /></td></tr>
    <tr>
      <td>Organisms:</td>
      <td><div id="container"><db:phylogeny/></div></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
    </tr>
    <tr>
      <td></td>
      <td><form:input id="selected" readonly="readonly" path="org"/></td>
    </tr>
    <tr>
	  <td>Browse category:</td>
	  <td><form:select path="category" items="${categories}" /></td>
	  <td></td>
    </tr>
        <tr>
	  <td>Term:</td>
	  <td><form:input path="term" /></td>
	  <td>This should be an auto-complete or subquery</td>
    </tr>
    <!-- <tr>
	  <td>Feature Type:</td>
	  <td>Gene</td>
	  <td>Restrict the type of features searched for</td>
    </tr> -->
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" /></td>
    </tr>

</table>
</form:form>

<format:footer />