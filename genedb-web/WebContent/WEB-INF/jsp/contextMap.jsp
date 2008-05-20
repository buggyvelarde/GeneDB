<%@ page contentType="application/json" %>
<%@ page import="org.genedb.web.gui.ContextMapCache" %>
<%@ page import="org.genedb.web.gui.ContextMapDiagram" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="json" %>

{
	"organism": <json:string value="${diagram.organism}"/>,
    "chromosome": <json:string value="${diagram.chromosome}"/>,
    "start": ${diagram.start},
    "end": ${diagram.end}",
    "image": '<%= ContextMapCache.fileForDiagram((ContextMapDiagram) request.getAttribute("diagram")) %>'
}