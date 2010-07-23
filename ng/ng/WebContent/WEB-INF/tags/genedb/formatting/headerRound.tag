<%@ tag display-name="header1" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="bodyClass" required="false" %>
<%@ attribute name="title"  required="true" %>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>${title} - GeneDB</title>

    <script type="text/javascript" src="http://js.sanger.ac.uk/urchin.js"></script>


    <link rel="stylesheet" href="<misc:url value="/includes/style/alternative.css"/>" type="text/css" />
    <link rel="stylesheet" href="<misc:url value="/includes/yui/build/reset-fonts/reset-fonts.css"/>" type="text/css" />
    <link rel="stylesheet" href="<misc:url value="/includes/yui/build/assets/skins/sam/menu.css"/>" type="text/css" />

    <!--  YUI dependencies -->
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/container/container_core.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/animation/animation-min.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/yahoo/yahoo-min.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/event/event-min.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/connection/connection-min.js"/>"></script>
    <!-- YUI menu -->
    <script type="text/javascript" src="<misc:url value="/includes/yui/build/menu/menu.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/phylogeny.js"/>"></script>
    <script type="text/javascript">
      var navigationMenuBar;
        YAHOO.util.Event.onContentReady("navigation", function () {
            navigationMenuBar = new YAHOO.widget.MenuBar("navigation", {autosubmenudisplay: true, showdelay: 0, hidedelay: 500, lazyload: false});
            navigationMenuBar.clickEvent.unsubscribeAll();
            navigationMenuBar.render();
        });

        YAHOO.util.Event.onContentReady("start",function() {
      init();
            adjustCoordinates()
        });

        var advancedSearchOpen = false;

        function toggleAdvancedSearch() {
            if (advancedSearchOpen) {
                closeAdvancedSearch();
                return;
            }
            loadAdvancedSearch();
        }

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
            advancedSearchOpen = true;
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
       advancedSearchOpen = false;
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
        <td id="logo" valign="top" align="left" rowspan="2"><a href="<misc:url value="/Homepage"/>"><img border="0" width="171" height="51" src="<misc:url value="/includes/images/genedb-logo.gif"/>" alt="GeneDB"></img></a></td>
        <td id="name">${name}</td>
        <td id="search">
        <form name="searchForm" action="<misc:url value="/"/>QuickSearchQuery" method="GET">
          <input type="hidden" name="taxons" value="${taxonNodeName}">
          <input type="hidden" name="pseudogenes" value="true"/>
          <input type="hidden" name="product" value="true"/>
          <input type="hidden" name="allNames" value="true"/>
          <input id="query" name="searchText" value="" type="text" align="middle"/>
          <input id="submit" type="submit" value="Search" title="Search" align="middle" /><br>
        </form>
        <span align="top" style="font-size:0.65em;">
          <a style="color:white;vertical-align:top;" href="#" onclick="toggleAdvancedSearch(); return false;">
            Advanced Search
          </a>&nbsp;&nbsp;&nbsp;
        </span>
        <div id="advancedSearch">
            <form name="advSearchForm" action="<misc:url value="/Query"/>" method="GET">
            <table id="advSearchTable" cellpadding="2">
              <tr>
                <td width="20%" style="text-align:left;">Search </td>
                <td width="80%">
                  <select name="q">
                    <option value="simpleName">Gene names</option>
                    <option value="product">Product</option>
                    <!-- <option value="curation">Curated annotations [comments & notes]</option> -->
                    <option value="go">GO term/id</option>
                    <option value="ec">EC number</option>
                    <option value="pfam">Pfam ID or keyword</option>
                  </select>
                </td>
              </tr>
              <tr>
              <td width="20%" style="text-align:left;">in </td>
              <td width="80%"><db:simpleselect /></td>
              </tr>
              <tr>
                <td width="20%" style="text-align:left;">
                  for
                </td>
                <td width="80%" style="text-align:left;">
                  <input id="query" name="search" type="text" align="middle">
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
                                        <a class="yuimenuitemlabel" href="<misc:url value="/Query?q=geneType&taxons=${taxonNodeName}&newSearch=true"/>" title="ProteinLength Search">By Gene Type</a>
                                    </li>
                                    <li class="yuimenuitem"> <%-- Genes By Location --%>
                                        <a class="yuimenuitemlabel" href="<misc:url value="/Query?q=geneLocation&taxons=${taxonNodeName}&newSearch=true"/>" title="Protein Location Search">By Location</a>
                                    </li>
                                    <%-- <li class="yuimenuitem">  Genes By Prediction Method
                                        <font color="gray">By Prediction Method</font>
                                    </li> --%>
                                     <li class="yuimenuitem"> <%-- Proteins By Length --%>
                                        <a class="yuimenuitemlabel" href="<misc:url value="/Query?q=proteinLength&taxons=${taxonNodeName}&newSearch=true"/>" title="Protein Length Search">By Protein Length</a>
                                    </li>
                                    <li class="yuimenuitem"><%-- Proteins By Molecular Mass --%>
                                        <a class="yuimenuitemlabel" href="<misc:url value="/Query?q=proteinMass&taxons=${taxonNodeName}&newSearch=true"/>" title="Protein Mass Search">By Molecular Mass</a>
                                    </li>
                                     <li class="yuimenuitem"> <%-- Proteins By Num Transmembrane Domains --%>
                                        <a class="yuimenuitemlabel" href="<misc:url value="/Query?q=proteinNumTM&taxons=${taxonNodeName}&newSearch=true"/>" title="Protein Length Search">By No. TM domains</a>
                                    </li>
                                    <%-- <li class="yuimenuitem">  Motif Search
                                        <font color="gray">Motif Search</font>
                                    </li> --%>
                                    <li class="yuimenuitem"> <%-- Proteins By Targeting Sequence --%>
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>Query?q=proteinTargetingSeq&taxons=${taxonNodeName}&newSearch=true" title="Protein Targeting Search">By Targeting Seqs.</a>
                                    </li>
                                     <%-- class="yuimenuitem"> Proteins By Annotation Status
                                        <font color="gray">By Annotation Status</font>
                                    </li> --%>
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
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>BrowseCategory?category=genedb_products&taxons=${taxonNodeName}" title="Browse Products">Products</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>BrowseCategory?category=ControlledCuration&taxons=${taxonNodeName}" title="Browse Curation">Controlled Curation</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>BrowseCategory?category=biological_process&taxons=${taxonNodeName}" title="Browse GO Biological Process">Biological Process</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>BrowseCategory?category=cellular_component&taxons=${taxonNodeName}" title="Browse GO Cellular Component">Cellular Component</a>
                                    </li>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" href="<misc:url value="/"/>BrowseCategory?category=molecular_function&taxons=${taxonNodeName}" title="Browse GO Molecular Function">Molecular Function</a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </li>
                   <li class="yuimenubaritem">
                        <a class="yuimenuitemlabel" href="<misc:url value="/History"/>">History</a>
                   </li>
                </ul>
            </div>
      </div></td>
    </tr>
</tbody></table>
