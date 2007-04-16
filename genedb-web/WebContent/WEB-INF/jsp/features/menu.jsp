<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
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
			element.style.left = ( depth.length * 154 ) + 1 + curleft;
			var top = -1;
			for (j=0;j<depth.length;j++) {
				top = top + depth[j] * 24;
 			}
 			element.style.top = top;
		}
		//alert(curleft);
		//alert(curtop);
	}
	
	function calculate() {
		//var items = document.getElementById("itemsLength");
		//var ilength = items.getAttribute("value");
		//var coords = new Array();

		for (var i=0; i< 5; i++) {
			var elementID = "mi_0_" + i;
			var obj = document.getElementById(elementID);
			var curleft = curtop = 0;
			if (obj.offsetParent) {
				curleft = obj.offsetLeft
				curtop = obj.offsetTop
				while (obj = obj.offsetParent) {
					curleft += obj.offsetLeft
					curtop += obj.offsetTop
				}
				alert(curtop);
				alert(curleft);
			}
		}
		//alert (coords);	
	}
</script>
<style type="text/css">
#container{
position: relative;
}
</style>
<body onload="doSomething()">
<format:header name="Phylogeny Tree">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<div id="container">
<db:phylogeny/>
</div>
</div>
<format:footer/>
