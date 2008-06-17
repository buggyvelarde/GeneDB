<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="bodyClass" required="false" %>
<%@ attribute name="title"  required="true" %>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>${title} - GeneDB</title>

    <link rel="stylesheet" href="<c:url value="/includes/style/alternative.css"/>" type="text/css" />
    <link rel="stylesheet" href="<c:url value="/includes/YUI-2.5.2/reset-fonts/reset-fonts.css"/>" type="text/css" />
    <link rel="stylesheet" href="<c:url value="/includes/YUI-2.5.2/menu/assets/skins/sam/menu.css"/>" type="text/css" />

    <!--  YUI dependencies -->
    <script type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/container/container_core.js"/>"></script>

    <!-- YUI menu -->
    <script type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/menu/menu.js"/>"></script>
    <script type="text/javascript">
        var navigationMenuBar;
        YAHOO.util.Event.onContentReady("navigation", function () {
            navigationMenuBar = new YAHOO.widget.MenuBar("navigation", {autosubmenudisplay: true, showdelay: 50, hidedelay: 1000, lazyload: false});
            navigationMenuBar.render();
        });
    </script>

    <jsp:doBody />
</head>
<% if (onLoad == null) { %>
<body class="yui-skin-sam ${bodyClass}">
<% } else { %>
<body class="yui-skin-sam ${bodyClass}" onLoad="${onLoad}">
<% } %>
<table id="header"><tbody>
    <tr id="top-row">
        <td id="logo">GeneDB</div>
        <td id="name">${name}</div>
        <td id="search">
        	<c:if test="${!empty organism}">
            	<form name="searchForm" action="<c:url value="/"/>NamedFeature?organism=${organism}" method="get">
            </c:if>
            <c:if test="${empty organism}">
            	<form name="searchForm" action="<c:url value="/"/>NamedFeature" method="get">
            </c:if>
            	<input id="query" name="name" type="text" align="middle"/>
            	<input id="submit" type="submit" value="Search" title="Search" align="middle" />
            </form>
        </td>
    </tr>
    <tr id="navigation-row">
    	<td colspan="3"><div id="navigation" class="yuimenubar yuimenubarnav">
            <div class="bd">
                <ul class="first-of-type">
                    <li class="yuimenubaritem first-of-type">
                        <a class="yuimenuitemlabel" href="#">Searches</a>
                        <div class="yuimenu">
                            <div class="bd">
                                <ul>
                                    <li class="yuimenuitem">
            						<c:if test="${!empty organism}">
                                        <a class="yuimenuitemlabel" href="<c:url value="/NamedFeature?organism=${organism}"/>" title="Name Search">By Name/Product</a>
            						</c:if>
            						<c:if test="${empty organism}">
                                        <a class="yuimenuitemlabel" href="<c:url value="/NamedFeature"/>" title="Name Search">By Name/Product</a>
            						</c:if>
                                    </li>
                                    <%-- Motif Search not yet implemented
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/"/>" title="Motif Search">Motif Search</a>
                                    </li>--%>
                                </ul>
                            </div>
                        </div>
                    </li>
                    <li class="yuimenubaritem">
                        <a class="yuimenuitemlabel" href="#">Browse</a>
                        <div class="yuimenu">
                            <div class="bd">
                                <ul>
                                    <li class="yuimenuitem">
            						<c:if test="${!empty organism}">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=genedb_products&organism=${organism}"/>" title="Browse Products">Products</a>
            						</c:if>
            						<c:if test="${empty organism}">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=genedb_products"/>" title="Browse Products">Products</a>
            						</c:if>
                                    </li>
                                    <li class="yuimenuitem">
                 						<c:if test="${!empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=CC_genedb_controlledcuration&organism=${organism}"/>" title="Browse Curation">Curation</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=CC_genedb_controlledcuration"/>" title="Browse Curation">Curation</a>
                 						</c:if>
                                    </li>
                                    <li class="yuimenuitem">
                 						<c:if test="${!empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=biological_process&organism=${organism}"/>" title="Browse GO Terms">GO Terms</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=biological_process"/>" title="Browse GO Terms">GO Terms</a>
                 						</c:if>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
    	</div></td>
    </tr>
</tbody></table>