<%@ tag display-name="header" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="title"  required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>${title} - GeneDB</title>

    <script type="text/javascript" src="http://js.sanger.ac.uk/urchin.js"></script>

    <link rel="stylesheet" href="<c:url value="/includes/style/genedb/main.css"/>" type="text/css" />

    <!--  YUI dependencies -->
    <script type="text/javascript" src="<c:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/yui/build/container/container_core.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/yui/build/animation/animation-min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/yui/build/yahoo/yahoo-min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/yui/build/event/event-min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/yui/build/connection/connection-min.js"/>"></script>
    <!-- YUI menu -->
    <script type="text/javascript" src="<c:url value="/includes/yui/build/menu/menu.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/scripts/phylogeny.js"/>"></script>
    <script type="text/javascript">
        YAHOO.util.Event.onContentReady("start",function() {
            init();
            adjustCoordinates()
        });

</script>
    <jsp:doBody />
</head>

