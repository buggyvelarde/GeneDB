<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<script src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
<script src='<c:url value="/dwr/interface/nameBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>

<script type="text/javascript" language="javascript">
	var ilength;

	function populateAutocomplete(autocompleter, token) {
      nameBrowse.getPossibleMatches(token, function(suggestions) {
          autocompleter.setChoices(suggestions);
      });
  }

  // should be in the "onload" of the body
  function createAutoCompleter() {
      new Autocompleter.DWR("textInput", "suggestions", populateAutocomplete, {});
  }

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
					selected.value = document.getElementById("menu_" + i).textContent;
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

	function check() {
		var selected = document.getElementById("selected");
		if (selected.value == '') {
			selected.style.border = "solid 1px red";
			alert ("Please select an organism from the tree");
			return false;
		}
		var org = document.getElementById("textInput");
		if (org.value == '') {
			org.style.border = "solid 1px red";
			alert ("Please enter the search term in the input box");
			return false;
		}


	}

	function checkThis(form) {
			if(form.query.value == ""){
			//document.writeln("look up is ...");
			form.query.value = "Please enter search text ...";
			}
		}
</script>
<style type="text/css">
#container{
	position: relative;
}

    #div.auto_complete {
      position:absolute;
      width:250px;
      background-color:white;
      border:1px solid #888;
      margin:0px;
      padding:0px;
    }

    li.selected { background-color: #ffb; }

</style>
<body onload="doSomething(); DWRUtil.useLoadingMessage(); createAutoCompleter()">
<format:header name="Main Search Page">
</format:header>
<div style="border: 1px solid #cccccc;margin-bottom: 5px; padding: 5px;">
<div align="center" style="background: url('/genedb-web/includes/images/subhead_bg.png') repeat;margin: 5px;">Free Text Search</div>
<sp:form name="lucene" commandName="luceneSearch" action="../LuceneSearch" method="post" onsubmit="checkThis(this)">
	<table>
		<tr>
			<td>Fields:
				<sp:select path="field">
					<sp:option value="ALL" label="ALL"/>
					<sp:options items="${fields}"/>
				</sp:select>
			</td>
			<td>Search String: <sp:input path="query" /></td>
			<td><input type="submit" value="Submit" /></td>
		</tr>
	</table>
</sp:form>
</div>
<div style="border: 1px solid #cccccc; padding: 5px;margin-top: 5px;">
<div align="center" style="background: url('/genedb-web/includes/images/subhead_bg.png') repeat;margin: 5px;">Gene Search</div>
<sp:form commandName="nameLookup" action="../NameSearch" method="get">
  <table>
    <tr><td colspan="3">
      <font color="red"><sp:errors path="*" /></font>
    </td></tr>
    <tr>
      <td>Organisms:</td>
      <td><div id="container"><db:phylogeny/></div></td>
      <td>You can choose either an individual organism or a group of them. (Note this is a temporary select box)</td>
    </tr>
    <tr>
      <td></td>
      <td><sp:input id="selected" readonly="readonly" path="org"/></td>
    </tr>
    <tr>
	  <td>Look Up:</td>
	  <td><sp:input id="textInput" path="name"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
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
</div>
<format:footer />