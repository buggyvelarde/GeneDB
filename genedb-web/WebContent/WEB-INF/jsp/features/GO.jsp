<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<format:header name="Gene Ontology Search">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<sp:form >
	<table align="center" border="0" cellspacing="0" cellpadding="0" width="100%">
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/images/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	<tr>
	<td width="40%" bgcolor="#E2E2FF" align="center">
			Search String :
			<form action="<c:url value="/" />/GoFeature">
			<input type="text" name="lookup"/> 
			<input type="submit" value="Submit"/> 
			</form>
	</td>
	</tr>
	<tr><td colspan="3"><img src="<c:url value="/" />/includes/blank.gif" width="100%" height="1" alt="--------"/>
	</tr>
	</table>
</sp:form>
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
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<display:table name="results" uid="count" pagesize="30" requestURI="/GoFeature" class="simple" cellspacing="0" cellpadding="4">
	<display:column property="organism.abbreviation" title="Organism" class="fonts"/>
	<display:column property="cvTerm.name" title="Type"/>
	<display:column property="uniqueName" href="./Search/FeatureByName" paramId="name"/>
</display:table>
<img src="<c:url value="/" />/includes/images/purpleDot.gif" width="100%" height="2" alt="----------------------">
<format:footer />