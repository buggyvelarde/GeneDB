<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<script type="text/javascript" language="javascript">
	var ilength;
	
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
		ilength = items.getAttribute("value");

		for (var i=0; i< ilength; i++) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			var depth = element.getAttribute("name").split('_');
			element.style.left = ( depth.length * 154 ) + 1;
			var top = -1;
			for (j=0;j<depth.length;j++) {
				top = top + depth[j] * 29;
 			}
 			element.style.top = top;
		}
		//alert(curleft);
		//alert(curtop);
	}

	function mouseclick(id) {
		var selected = document.getElementById("selected");
		selected.value = '';
		for (var i=0; i< this.ilength; i++) {
			var element = document.getElementById("check_" + i);
			if(element.checked) {
				if (selected.value == '') {
					selected.value =  document.getElementById("menu_" + i).textContent;
				} else {	
					selected.value = selected.value + ',' + document.getElementById("menu_" + i).textContent;
				}
			}	
		}
	}
	
	function resetall() {
		for (var i=0; i< ilength; i++) {
			this.checked[i] = false;
			var element = document.getElementById("menu_" + i);
			element.checked = false;
		}
		var selected = document.getElementById("selected");
		selected.value = '';
		var url = document.URL.split('?');
		window.location = url[0];
	}

</script>
<format:headerRound title="Search All">
	<st:init />
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/extjs/ext-all.css"/>" />
	<script type="text/javascript" src="<misc:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
	<script src="<misc:url value="/includes/scripts/phylogeny.js"/>" type="text/javascript"></script>
</format:headerRound>
<table width="100%">
<tr>
	<td width="20%">
		<format:searchOptions/>
		<format:history-small/>
	</td>
	<td width="100%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Search</div>
		<br>
			<sp:form commandName="searchAll" action="SearchAll" method="get">
  <table>
    <tr><td colspan="3">
      <font color="red"><sp:errors path="*" /></font>
    </td></tr>
    <tr>
      <td>Organisms:</td>
      <td><div id="container"><db:phylogeny/></div></td>
    </tr>
    <tr>
      <td></td>
      <td><sp:input id="selected" readonly="readonly" path="orgs"/></td>
    </tr>
    <tr>
	  <td>Look Up:</td>
	  <td><sp:input id="textInput" path="query"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
	  <td>The name to lookup. It can include wildcards (*) to match any series of characters</td>
    </tr>
    <tr>
	  <td>Feature Type:</td>
	  <td>
	  	<sp:select path="field">
	  		<sp:option value="ALL">ALL</sp:option>
	  		<sp:option value="genedb_products">Product</sp:option>
	  		<sp:option value="CC_genedb_controlledcuration">Controlled Curation</sp:option>
	  		<sp:option value="biological_process">GO Biological Process</sp:option>
	  		<sp:option value="molecular_function">GO Molecular Function</sp:option>
	  		<sp:option value="cellular_component">GO Cellular Component</sp:option>
	  	</sp:select>
	  </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" /></td>
	  <td>&nbsp;</td>
    </tr>
  </table>
</sp:form>
	</td>
</tr>
</table>
<format:footer />