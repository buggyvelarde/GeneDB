<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>
<script src="<c:url value="/includes/scripts/script.aculo.us/prototype.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/script.aculo.us/scriptaculous.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/autocomplete.js"/>" type="text/javascript"></script>
<script src="<c:url value="/includes/scripts/spring-util.js"/>" type="text/javascript"></script>
<script src='<c:url value="/dwr/interface/goPBrowse.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/engine.js"/>' type="text/javascript"></script>
<script src='<c:url value="/dwr/util.js"/>' type="text/javascript"></script>

<script type="text/javascript" language="javascript">
	var checked = new Array();
	var ilength;
			
	function populateAutocomplete(autocompleter, token) {
      goPBrowse.getPossibleMatches(token,document.getElementById("category").value, function(suggestions) {
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
			this.checked[i] = false; 
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
	
	function fillBox() {
		var selected = document.getElementById("selected");
		selected.value = '';
		for (var i=0; i< this.ilength; i++) {
			if (this.checked[i]) {
				var element = document.getElementById("menu_" + i);	
				if (selected.value == '') {
					selected.value = element.textContent;
				} else {	
					selected.value = selected.value + ',' + element.textContent;
				}
			}
		}
		
	}
	
	function mouseclick(id) {
		this.checked[id] = !(this.checked[id]);
		fillBox();
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
<format:header name="Browse By Term"/>


<p>This is a page for a browse by term search

<form:form action="BrowseTerm" commandName="browseTerm" method="get" onsubmit="return check()">
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
	  <td><form:select id="category" path="category" items="${categories}" /></td>
	  <td></td>
    </tr>
        <tr>
	  <td>Term:</td>
	  <td><form:input id="textInput" path="term" size="50"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
	  <td>This should be an auto-complete or subquery</td>
    </tr>
    <!-- <tr>
	  <td>Feature Type:</td>
	  <td>Gene</td>
	  <td>Restrict the type of features searched for</td>
    </tr> -->
    <tr>
      <td>&nbsp;</td>
	  <td colspan="2"><input type="submit" value="Submit" /> <input type="reset" value="reset" onclick="resetall()"/></td>
    </tr>

</table>
</form:form>

<format:footer />