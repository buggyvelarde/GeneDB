<%@ tag display-name="page" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="structure" tagdir="/WEB-INF/tags/genedb/structure" %>

<% if (onLoad == null) { %>
<body>
<% } else { %>
<body onLoad="${onLoad}">
<% } %>

<c:url value="/" var="base" scope="page"/>
<c:url value="/" var="baseUrl" scope="request"/>
<div id="container">
<div id="header">
<a href="<misc:url value="/Homepage"/>"><img src="<misc:url value="/includes/image/GeneDB-logo.png"/>" border="0" alt="GeneDB" class="float-left-and-offset" id="logo" /></a>

<div class="float-right" >
<div class="baby-blue-top"></div>
<div id="search" class="baby-blue">
<form action="<misc:url value="/QuickSearchQuery"/>" name="quicksearch" method="get">
<table cellpadding="0" cellspacing="0" width="100%" class="search-table">
<tr>
<td>
<input type="hidden" name="pseudogenes" value="true" />
<input type="hidden" name="product" value="true" />
<input type="hidden" name="allNames" value="true" />
<input type="text" name="searchText" class="search-box" /></td><td align="right"><input type="image" src="<misc:url value="/includes/image/button-search.gif" />" /></td>
</tr>
<tr>
<td colspan="2">
<db:simpleselect selection="${taxonNodeName}"/>
</td>
</tr>
</table>
</form>
</div>
<div class="baby-blue-bot"></div>
</div><!-- end float right of search box -->
<br class="clear" />
</div><!-- end header block -->

<br class="clear" />

<div id="navigation">
<ul id="nav">
<!-- <li>
<li><a href="<misc:url value="/Homepage"/>">Home</a>
</li> -->
<li><a href="<misc:url value="/Page/aboutUs"/>">About us</a></li>
<li class="has-sub"><a href="<misc:url value="/Query" />">Searches</a>
<ul class="sub-menu">
<li><a href="<misc:url value="/Query/geneType"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>" >By Gene Type</a></li>
<li><a href="<misc:url value="/Query/geneLocation"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>" >By Location</a></li>
<li><a href="<misc:url value="/Query/proteinLength"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>" >By Protein Length</a></li>
<li><a href="<misc:url value="/Query/proteinMass"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">By Molecular Mass</a></li>
<li><a href="<misc:url value="/Query/proteinNumTM"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">By No. TM domains</a></li>
<li><a href="<misc:url value="/Query/proteinTargetingSeq"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">By Targeting Seqs.</a></li>
<li><a href="<misc:url value="/Query/simpleName"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Gene names</a></li>
<li><a href="<misc:url value="/Query/product"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Product</a></li>
<%-- <li><a href="<misc:url value="/Query/curation"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Curated annotations [comments & notes]</a></li> --%>
<li><a href="<misc:url value="/Query/go"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">GO term/id</a></li>
<li><a href="<misc:url value="/Query/ec"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">EC number</a></li>
<li><a href="<misc:url value="/Query/pfam"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Pfam ID or keyword</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Browse</a>
<ul class="sub-menu">
<li><a href="<misc:url value="/category/genedb_products"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Products</a></li>
<li><a href="<misc:url value="/category/ControlledCuration"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Controlled Curation</a></li>
<li><a href="<misc:url value="/category/biological_process"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Biological Process</a></li>
<li><a href="<misc:url value="/category/cellular_component"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Cellular Component</a></li>
<li><a href="<misc:url value="/category/molecular_function"><spring:param name="taxons" value="${taxonNodeName}"/></misc:url>">Molecular Function</a></li>
</ul>
</li>
<li class="has-sub"><a href="">Tools</a>
<ul class="sub-menu">
<li><a href="<misc:url value="/History" />">History</a></li>
<li><a href="<misc:url value="/IdList" />">ID List Upload</a></li>
</ul>
</li>
<!-- end sub menu -->

</ul>

</div><!-- end navigation block -->
<structure:motdMessage />
<structure:flashMessage />

<db:breadcrumb selection="${taxonNodeName}" showingHomepage="${showingHomepage}" />



<jsp:doBody />

<br class="clear" />

<div id="footer">
<div class="footer-top"></div>
<div class="light-grey">
<table cellpadding="0" cellspacing="0" width="100%">
<tr>
<td valign="top" align="left">
<p>&copy; 2009-2010 and hosted by the <a href="http://www.sanger.ac.uk/">Sanger Institute</a></p>
</td>

<td valign="top" align="right">
<p>Comments/Questions: <a href="mailto:webmaster@genedb.org">Email us</a>
<%--
<a href="<misc:url value="/Feedback" />">Curator/Data</a> or
<a href="<misc:url value="/Feedback" />">website</a> | Version 4.98</p>
--%></td>
</tr>
</table>
</div>
<div class="footer-bot"></div>
</div><!-- end footer -->
</div><!-- end container -->

<script type="text/javascript">
_userv=0;
urchinTracker();
</script>
</body>
</html>

