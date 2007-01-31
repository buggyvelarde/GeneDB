<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:header name="Restriction mapping">
	<script type="text/javascript" src="<c:url value="/includes/scripts/overlib/overlib.js"/>"></script>
	<script type="text/javascript">
	function showMenu(left, right) {
	   var addr = '<a href="http://www.bbc.co.uk">';
	   return overlib(addr)
	}
	</script>
	<st:init />
</format:header>



<h3>Diagram</h3>

<p align="center">${settings.map}</p>


		   
<format:footer />