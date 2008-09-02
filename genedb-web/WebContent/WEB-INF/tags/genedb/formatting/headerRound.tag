<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="bodyClass" required="false" %>
<%@ attribute name="title"  required="true" %>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

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
	<script type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/animation/animation-min.js"/>"></script>
    <!-- YUI menu -->
    <script type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/menu/menu.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/includes/scripts/phylogeny.js"/>"></script>
    <script type="text/javascript">
    	var navigationMenuBar;
        YAHOO.util.Event.onContentReady("navigation", function () {
            navigationMenuBar = new YAHOO.widget.MenuBar("navigation", {autosubmenudisplay: true, showdelay: 0, hidedelay: 500, lazyload: false});
            navigationMenuBar.clickEvent.unsubscribeAll();
            navigationMenuBar.render();
        });

        YAHOO.util.Event.onContentReady("start",function() {
			init();
            adjustCoordinates();
        });

        function loadAdvancedSearch() {
			var dom = YAHOO.util.Dom;
			var div = dom.get("advancedSearch");
			var attributes = {
			        height: { to: 150 }
			 };
			 var anim = new YAHOO.util.Anim('advancedSearch', attributes);
			 anim.animate();
			 dom.setStyle(div, "overflow", "visible");
			 dom.setStyle(div, "border", "1px solid grey");
        }

        function closeAdvancedSearch() {
        	var dom = YAHOO.util.Dom;
			var div = dom.get("advancedSearch");
			dom.setStyle(div, "overflow", "hidden");
			dom.setStyle(div, "border", "");
			var attributes = {
			        height: { to: 0 }
		    };
		 	var anim = new YAHOO.util.Anim('advancedSearch', attributes);
			anim.animate();
        }

    </script>

    <jsp:doBody />
</head>
<% if (onLoad == null) { %>
<body class="yui-skin-sam ${bodyClass}" onLoad="">
<% } else { %>
<body class="yui-skin-sam ${bodyClass}" onLoad="${onLoad}">
<% } %>
<table id="header"><tbody>
    <tr id="top-row">
        <td id="logo" valign="top" align="left"><a href="<c:url value="/Homepage"/>"><img border="0" width="170" height="49" src="<c:url value="/includes/images/genedb-logo.png"/>" alt="GeneDB"></img></a></td>
        <td id="name">${name}</td>
        <td id="search">
        	<c:if test="${!empty organism}">
            	<form name="searchForm" action="<c:url value="/"/>Query?q=allNameProduct&organism=${organism}&pseudogene=true&obsolete=false&product=true&allNames=true" method="GET">
            </c:if>
            <c:if test="${empty organism}">
            	<form name="searchForm" action="<c:url value="/Query?q=allNameProduct&pseudogene=true&obsolete=false&product=true&allNames=true"/>" method="GET">
            </c:if>
            	<input id="query" name="query" type="text" align="middle"/>
            	<input id="submit" type="submit" value="Search" title="Search" align="middle" /><br>
				</form>
				<span align="top" style="font-size:0.65em;">
					<a style="color:white;vertical-align:top;" href="#" onclick="loadAdvancedSearch(); return false;">
						Advanced Search
					</a>
				</span>
				<div id="advancedSearch">
					<c:if test="${!empty organism}">
						<form name="advSearchForm" action="<c:url value="/"/>Query?organism=${organism}" method="get">
					</c:if>
					<c:if test="${empty organism}">
						<form name="advSearchForm" action="<c:url value="/"/>Query" method="get">
					</c:if>
						<table id="advSearchTable" cellpadding="2">
							<tr>
								<td width="20%" style="text-align:left;">Search </td>
								<td width="80%">
									<select name="q">
										<option value="allNames">Gene names</option>
										<option value="product">Product</option>
										<option value="annotation">curated annotations [comments & curation]</option>
										<option value="goTerm">GO term/id</option>
										<option value="ecNum">EC number</option>
										<option value="pfam">Pfam ID or keyword</option>
									</select>
								</td>
							</tr>
							<tr>
								<td width="20%" style="text-align:left;"></td>
								<td width="80%" style="text-align:left;">
									<input id="organism" name="organism" type="text" align="middle">
								</td>
							</tr>
							<tr>
								<td width="20%" style="text-align:left;">
									in
								</td>
								<td width="80%" style="text-align:center;"><db:phylogeny/></td>
							</tr>
							<tr>
								<td width="20%" style="text-align:left;">
									for
								</td>
								<td width="80%" style="text-align:left;">
									<input id="query" name="term" type="text" align="middle">
									<input id="submit" type="submit" value="Go" title="Search" align="middle" />
								</td>
							</tr>
						</table>
					</form>
					<br>
					<span><a href="#" onclick="closeAdvancedSearch(); return false;">Close</a></span>
				</div>
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

                                    <%-- Genes By Type --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=geneType&organism=${organism}"/>" title="ProteinLength Search">By Gene Type</a>
                                    </li>

                                    <%-- Genes By Location --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=geneLocation&organism=${organism}"/>" title="ProteinLength Search">By Location</a>
                                    </li>

                                    <%-- Genes By Prediction Method --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=genePredictionMethod&organism=${organism}"/>" title="ProteinLength Search">By Prediction Method</a>
                                    </li>

                                    <%-- Proteins By Length --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinLength&organism=${organism}"/>" title="ProteinLength Search">By Protein Length</a>
                                    </li>

                                    <%-- Proteins By Molecular Mass --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinMass&organism=${organism}"/>" title="ProteinLength Search">By Molecular Mass</a>
                                    </li>

                                    <%-- Proteins By Num Transmembrane Domains --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=proteinNumTM&organism=${organism}"/>" title="ProteinLength Search">By No. TM domains</a>
                                    </li>

                                    <%-- Motif Search --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/MotifSearch"/>" title="Motif Search">Motif Search</a>
                                    </li>

                                    <%-- Proteins By Targeting Sequence --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=proteinTargetingSeq&organism=${organism}"/>" title="ProteinLength Search">By Targetting Seqs.</a>
                                    </li>

                                    <%-- Proteins By Annotation Status --%>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinAnnotationStatus&organism=${organism}"/>" title="ProteinLength Search">By Annotation Status</a>
                                    </li>

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
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=ControlledCuration&organism=${organism}"/>" title="Browse Curation">Curation</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=ControlledCuration"/>" title="Browse Curation">Curation</a>
                 						</c:if>
                                    </li>
                                    <li class="yuimenuitem">
                 						<c:if test="${!empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=biological_process&organism=${organism}"/>" title="Browse GO Terms">Biological Process</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=biological_process"/>" title="Browse GO Terms">Biological Process</a>
                 						</c:if>
                                    </li>
                                    <li class="yuimenuitem">
                 						<c:if test="${!empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=cellular_component&organism=${organism}"/>" title="Browse GO Terms">Cellular Component</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=cellular_component"/>" title="Browse GO Terms">Cellular Component</a>
                 						</c:if>
                                    </li>
                                    <li class="yuimenuitem">
                 						<c:if test="${!empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=molecular_function&organism=${organism}"/>" title="Browse GO Terms">Molecular Function</a>
                 						</c:if>
                 						<c:if test="${empty organism}">
                                            <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=molecular_function"/>" title="Browse GO Terms">Molecular Function</a>
                 						</c:if>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </li>
					<li class="yuimenubaritem">
						<a class="yuimenuitemlabel" href="<c:url value="/History/View"/>">History</a>
					</li>
                </ul>
            </div>
    	</div></td>
    </tr>
</tbody></table>