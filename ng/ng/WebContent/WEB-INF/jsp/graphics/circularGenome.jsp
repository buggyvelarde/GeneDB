<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:headerRound title="Restriction mapping">
	${array}
	<script type="text/javascript" src="<misc:url value="/includes/scripts/overlib/overlib.js"/>"></script>
	<script type="text/javascript" src="<misc:url value="/includes/scripts/graphics/wz_jsgraphics.js"/>"></script>
	<script type="text/javascript">

	</script>
	<st:init />
</format:headerRound>
<br>
<div id="divHighlight" style="position:absolute; background-color: red;width:28px;height:2px;visibility:hidden;"></div>

<div id="main" style="min-width:920px;">
	<div id="circular" style="position:relative;width:600px; float:left">
		${circular}
	</div>
	<div id="gelimg" style="width:100px; float:left">
		${gel}
	</div>
	<div id="ftable" style="width:200px; height:600px; float:right; overflow: auto;">
		${table}
	</div>
</div>
<script type="text/javascript">
	var circ = new jsGraphics("circular");
	var gel = new jsGraphics("gelimg");

	function highlight(x1,x2,y1,y2,value) {
		//remove previous line and label
		circ.clear();

		//show gel
		showGel(value);
		//draw current line and label
		circ.setColor("#ff0000"); // red
  		circ.drawLine(x1, y1,x2,y2);
  		circ.drawString(value, x2, y2-10);
  		circ.paint();
	}

	function highlightOff() {
		circ.clear();
		gel.clear();
		//hideGel();
	}

	function showGel(label) {
		var img = document.getElementById('gel');
		var imgLeft = img.offsetLeft;
		var imgTop = img.offsetTop;
	   	//var highlightDiv = document.getElementById('divHighlight');
	   	//highlightDiv.style.left = imgLeft + 'px';
	   	//highlightDiv.style.top = imgTop + coords[label-1][0] + 'px';
	   	//highlightDiv.style.visibility = 'visible';

	   	gel.clear();
	   	gel.setColor("#ff0000"); // red
	   	gel.setStroke(2);
		var x1 = imgLeft;
		var y1 = coords[label-1][0] + imgTop;
		var x2 = coords[label-1][1]/3 + imgLeft;
		var y2 = coords[label-1][2] + imgTop;

	   	gel.drawLine(x1,y1,x2,y2);
	   	gel.paint();
	}

	function hideGel() {

	   	var highlightDiv = document.getElementById('divHighlight');
	   	highlightDiv.style.visibility = 'hidden';

	}

</script>
<div style="clear:both">
</div>
<format:footer />