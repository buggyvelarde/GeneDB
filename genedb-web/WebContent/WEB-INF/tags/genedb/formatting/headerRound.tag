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
            var anim = new YAHOO.util.Anim('advancedSearch', attributes, 0.3);
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
       var anim = new YAHOO.util.Anim('advancedSearch', attributes, 0.3);
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
        <td id="logo" valign="top" align="left" rowspan="2"><a href="<c:url value="/Homepage"/>"><img border="0" width="171" height="51" src="<c:url value="/includes/images/genedb-logo.gif"/>" alt="GeneDB"></img></a></td>
        <td id="name">${name}</td>
        <td id="search">
            <form name="searchForm" action="<c:url value="/"/>Query" method="GET">
                <c:if test="${!empty organism}">
                  <input type="hidden" name="organism" value="${organism}">
                </c:if>
                <input type="hidden" name="q" value="allNameProduct"/>
                <input type="hidden" name="pseudogene" value="true"/>
                <input type="hidden" name="product" value="true"/>
                <input type="hidden" name="allNames" value="true"/>
                <input id="query" name="search" type="text" align="middle"/>
                <input id="submit" type="submit" value="Search" title="Search" align="middle" /><br>
            </form>
        <span align="top" style="font-size:0.65em;">
          <a style="color:white;vertical-align:top;" href="#" onclick="loadAdvancedSearch(); return false;">
            Advanced Search
          </a>
        </span>
        <div id="advancedSearch">
          <c:if test="${!empty organism}">
            <form name="advSearchForm" action="<c:url value="/"/>Query?organism=${organism}" method="post">
          </c:if>
          <c:if test="${empty organism}">
            <form name="advSearchForm" action="<c:url value="/"/>Query" method="get">
          </c:if>
            <table id="advSearchTable" cellpadding="2">
              <tr>
                <td width="20%" style="text-align:left;">Search </td>
                <td width="80%">
                  <select name="q">
                    <option value="simpleName">Gene names</option>
                    <option value="product">Product</option>
                    <option value="curation">Curated annotations [comments & notes]</option>
                    <option value="go">GO term/id</option>
                    <option value="ec">EC number</option>
                    <option value="pfam">Pfam ID or keyword</option>
                  </select>
                </td>
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
      <td colspan="2"><div id="navigation" class="yuimenubar yuimenubarnav">
            <div class="bd">
                <ul class="first-of-type">
                    <li class="yuimenubaritem first-of-type">
                        <a class="yuimenuitemlabel" href="#">Searches</a>
                        <div class="yuimenu">
                            <div class="bd">
                                <ul>
                                    <li class="yuimenuitem"> <%-- Genes By Type --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=geneType"/>" title="ProteinLength Search">By Gene Type</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Genes By Location --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=geneLocation"/>" title="Protein Location Search">By Location</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Genes By Prediction Method --%>
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=genePredictionMethod"/>" title="Prediction Method Search">By Prediction Method</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Proteins By Length --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinLength"/>" title="Protein Length Search">By Protein Length</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Proteins By Molecular Mass --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinMass"/>" title="Protein mass Search">By Molecular Mass</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Proteins By Num Transmembrane Domains --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinNumTM"/>" title="Protein num TM Search">By No. TM domains</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Motif Search --%>
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/MotifSearch"/>" title="Motif Search">Motif Search</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Proteins By Targeting Sequence --%>
                                        <a class="yuimenuitemlabel" href="<c:url value="/Query?q=proteinTargetingSeq"/>" title="Protein Targeting Search">By Targeting Seqs.</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Proteins By Annotation Status --%>
                                        <a class="yuimenuitemlabel-disabled" href="<c:url value="/Query?q=annotationStatus"/>" title="Annotation Status Search">By Annotation Status</a>
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
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=genedb_products"/>" title="Browse Products">Products</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=ControlledCuration"/>" title="Browse Curation">Controlled Curation</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=biological_process"/>" title="Browse GO Biological Process">Biological Process</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=cellular_component"/>" title="Browse GO Cellular Component">Cellular Component</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<c:url value="/BrowseCategory?category=molecular_function"/>" title="Browse GO Molecular Function">Molecular Function</a>
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