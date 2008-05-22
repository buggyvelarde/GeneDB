<%@ page contentType="application/json" %>
<%@ page import="org.genedb.web.gui.ContextMapCache" %>
<%@ page import="org.genedb.web.gui.RenderedContextMap" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="json" %>

<%
    String contextMapURI = ContextMapCache.fileForDiagram((RenderedContextMap) request.getAttribute("renderedContextMap"),
        pageContext.getServletContext());
    String chromosomeThumbnailURI = ContextMapCache.fileForDiagram((RenderedContextMap) request.getAttribute("chromosomeThumbnailMap"),
        pageContext.getServletContext());
%>

<c:set var="diagram" value="${renderedContextMap.diagram}"/>
{
	"organism": <json:string value="${diagram.organism}"/>,
    "chromosome": <json:string value="${diagram.chromosome}"/>,
    "start": ${diagram.start},
    "end": ${diagram.end},
    "locus": ${diagram.locus},
    "basesPerPixel": ${renderedContextMap.basesPerPixel},
    "imageSrc": '<%= contextMapURI %>',
    "imageWidth":  ${renderedContextMap.width},
    "imageHeight": ${renderedContextMap.height},
    "chromosomeThumbnailSrc": '<%= chromosomeThumbnailURI %>',
}
