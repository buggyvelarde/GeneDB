<%@ tag display-name="header1"
        body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="title"  required="true" %>
<%@ attribute name="onLoad" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/jimmac.css" type="text/css"/>
	<c:set var="pageTitle" value="${title} - GeneDB"/>
	<c:if test="!empty title">
		<c:set var="pageTitle" value="${title}" />
	</c:if>
	<title>${pageTitle}</title>
    <jsp:doBody />
</head>
<% if (onLoad == null) { %>
<body>
<% } else { %>
<body onLoad="${onLoad}">
<% } %>
<!--  <div class="rounded" style="width: 100%; background-color: rgb(55, 124, 177); text-align: center; font-size: 2em; line-height: 3em; font-size-adjust: none; font-stretch: normal; color: rgb(0, 0, 0); font-family: Georgia; font-variant: small-caps; font-weight: bold;"> -->
	<table width="100%" style="background-color: rgb(55, 124, 177); height: 60px;">
		<tr>
			<td width="100%" align="center" style="background-color: rgb(55, 124, 177); text-align: center; font-size: 2em; line-height: 1em; font-size-adjust: none; font-stretch: normal; color: #e0e0d5; font-family: Georgia; font-variant: small-caps; font-weight: bold;vertical-align: middle">
				GeneDB
				<c:if test="${!empty name}">
					<br>${name}
				</c:if>
			</td>
		</tr>
	</table>
		<div id="demo-container">
			<ul id="simple-menu">
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home" class="current">Home</a></li>
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">Resources</a></li>
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">Downloads</a></li>
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">FAQs</a></li>
				<li><a href="http://www.13styles.com/css-menus/simple-menu/" title="Home">About Us</a></li>
				<li style="float:right;"><form name="query" action="<c:url value="/"/>NamedFeature" method="get" style="vertical-align: middle;">
							<input id="query" name="name" type="text" style="width: 219px; border:0; padding: 4px; vertical-align: middle;" align="middle"/>
							<select id="type" name="type">
								<option value="geneid">All Names</option>
								<option value="geneid">Gene ID</option>
								<option value="genename">Gene Name</option>
								<option value="product">Product</option>
							</select>
							<input type="submit" value="Search" title="Search" align="middle" style="vertical-align: middle; margin:0 0 0 7px;background:transparent url(<c:url value="/"/>includes/images/bgsprite.gif) no-repeat -47px top;border:0;width:66px;height:24px;font-weight:normal;vertical-align:bottom;cursor:pointer;font-size:1em;font-weight:bold;"/> 
				</form>
				</li>
			</ul>
		</div>