<%@ tag display-name="page" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

<% if (onLoad == null) { %>
<body>
<% } else { %>
<body onLoad="${onLoad}">
<% } %>

<div id="container">
<div id="header">
<img src="/includes/image/genedb-logo.gif" height="84" width="386" alt="GeneDB" class="float-left" />

<div class="float-right" >
<div class="baby-blue-top"></div>
<div id="search" class="baby-blue">
<form action="" method="post">
<table cellpadding="0" cellspacing="0" width="100%" class="search-table">
<tr>
<td><input type="text" name="search" class="search-box" /></td><td align="right"><input type="image" src="images/button-search.gif" /></td>
</tr>
<tr>
<td colspan="2">
<select>
<option>All organisms</option>
</select>
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
<li><a href="">Home</a></li>
<li class="has-sub"><a href="">About us</a>
<ul class="sub-menu">
<li><a href="">One</a></li>
<li><a href="">A really long title just for testing</a></li>
<li><a href="">Three</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Searches</a>
<ul class="sub-menu">
<li><a href="">Consectetur</a></li>
<li><a href="">Vivamus</a></li>
<li><a href="">Nec eros</a></li>
<li><a href="">Sit amet</a></li>
<li><a href="">Cras tempor rhoncus</a></li>
<li><a href="">Dolor sit</a></li>
</ul>
<!-- end sub menu -->
</li>
<li class="has-sub"><a href="">Browse</a>
<ul class="sub-menu">
<li><a href="">Sub-link one</a></li>
<li><a href="">Sub-link two</a></li>
<li><a href="">Sub-link three</a></li>
<li><a href="">Sub-link four</a></li>
</ul>
</li>
<li class="has-sub">Pages
<ul class="sub-menu">
<li><a href="index.html">Home</a></li>
<li><a href="search.html">Search results</a></li>
<li><a href="entrypoint.html">Database entry point</a></li>
<li><a href="infantum.html">L. infantum JPCM5</a></li>
</ul><!-- end sub menu -->
</li>
</ul>
</div><!-- end navigation block -->


<jsp:doBody />

<br class="clear" />

<div id="footer">
<div class="footer-top"></div>
<div class="light-grey">
<table cellpadding="0" cellspacing="0" width="100%">
<tr>
<td valign="top" align="left">
<p>&copy; 2009 The Sanger Institute | Hosted by the <a href="http://www.sanger.ac.uk/">Sanger Institute</a></p>
</td>

<td valign="top" align="right">
<p><a href="">Send us your comments on GeneDB</a> | <a href="">Curator feedback</a> | <a href="">Technical feedback</a></p>
<p>Version 4.88</p>
</td>
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
