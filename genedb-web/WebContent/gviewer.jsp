<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Gene Results List">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<table align="center" width="560">
  <tbody>
  	<tr>
      <td>
      <p align="center">
      <div id="GViewerGuide" align="left" style="font-family:Arial, Helvetica, sans-serif; font-size:12px"> </div>
	 <div id="GViewerSpace"> </div>
      <div id="gviewer">
           <embed src="<c:url value="/"/>GViewer/GViewer2.swf" quality="high" bgcolor="#FFFFFF" name="Spombe" allowscriptaccess="sameDomain" type="application/x-shockwave-flash" flashvars="&amp;titleBarText=sPombe Genome Data&amp;longestChromosomeLength=&amp<c:out value="${length}"/>&amp;lcId=1234567890&amp;baseMapURL=<c:url value="/"/>GViewer/data/sbase.xml&amp;annotationURL=<c:url value="/"/>GViewer/data/003.xml&amp;dimmedChromosomeAlpha=40&amp;bandDisplayColor=0x0099FF&amp;wedgeDisplayColor=0xCC0000&amp;browserURL=http://www.genedb.org/perl-gb/gbrowse/S.pombe/?name=SP&amp;" pluginspage="http://www.macromedia.com/go/getflashplayer" align="center" height="400" width="500">
		</div> 
      </td>
    </tr>
  </tbody>
</table>
<format:footer />