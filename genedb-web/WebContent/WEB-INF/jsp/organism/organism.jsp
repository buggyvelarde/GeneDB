<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:header1 name="${organism} Home Page">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/wtsi.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/frontpage1.css" type="text/css"/>
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-base.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-all.js"/>"></script>
	 <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/extjs/ext-all.css"/>" />
	<script type="text/javascript" src="<c:url value="/includes/scripts/extjs/ext-history.js"/>"></script>
</format:header1>
<table width="100%">
<tr>
	<td width="20%">
		<div class="fieldset">
		<div class="legend">Quick Search</div>
			<br>
			<form name="query" action="NameSearch" method="get">
			<table>
				<tr>
					<td>Gene Name: </td>
					<td><input id="query" name="name" type="text" size="12"/></td>
				</tr>
				<tr>
					<td><input type="submit" value="submit"/></td>
					<td><br></td>
				</tr>
			</table>
			</form>
		</div>
		<div class="fieldset">
		<div class="legend">Navigation</div>
			<br>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" id="active_menu">Home</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel">News</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Links</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >Search</a></td></tr>
				<tr align="left"><td><a href="www.genedb.org" class="mainlevel" >FAQs</a></td></tr>
			</table>
		</div>
		<!--   <div class="fieldset">
		<div class="legend">History</div>
			<br>
			<div id="topic-grid"/>
		</div> -->
	</td>
	<td width="100%">
		<div class="fieldset" align="center" style="width: 98%;">
		<div class="legend">Chromosome View</div>
			  <div id="GViewerGuide" align="left" style="font-family:Arial, Helvetica, sans-serif; font-size:12px"> </div>
	 		<div id="GViewerSpace"> </div>
      		<div id="gviewer">
           		<embed src="<c:url value="/"/>GViewer/GViewer2.swf" quality="high" bgcolor="#FFFFFF" name="${organism}" allowscriptaccess="sameDomain" type="application/x-shockwave-flash" flashvars="&amp;titleBarText=${organism} Genome Data&amp;longestChromosomeLength=3291871&amp;lcId=1234567890&amp;baseMapURL=<c:url value="/"/>GViewer/data/base.xml&amp;annotationURL=<c:url value="/"/>GViewer/data/0002.xml&amp;dimmedChromosomeAlpha=40&amp;bandDisplayColor=0x0099FF&amp;wedgeDisplayColor=0xCC0000&amp;browserURL=http://localhost:8080/genedb-web/NamedFeature?name=&amp;" pluginspage="http://www.macromedia.com/go/getflashplayer" align="center" height="400" width="500"/>
			</div>
	</td>
</tr>
</table>
<format:footer />