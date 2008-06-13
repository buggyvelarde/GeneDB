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
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css" />
	<c:set var="pageTitle" value="${title} - GeneDB"/>
	<c:if test="!empty title">
		<c:set var="pageTitle" value="${title}" />
	</c:if>
	<title>${pageTitle}</title>
    <jsp:doBody />
</head>
<% if (onLoad == null) { %>
<body class="${bodyClass}">
<% } else { %>
<body class="${bodyClass}" onLoad="${onLoad}">
<% } %>
<div id="header">
    <div id="logo">GeneDB</div>
    <div id="name">${name}</div>
    <div id="search">
    	<c:if test="${organism != null}">
    		<form name="query" action="<c:url value="/NamedFeature?organism=${organism}"/>" method="get" style="vertical-align: middle;">
    	</c:if>
    	<c:if test="${organism == null}">
    		<form name="query" action="<c:url value="/NamedFeature?organism"/>" method="get" style="vertical-align: middle;">
    	</c:if>
    	
        	<input id="query" name="name" type="text" align="middle"/>
        	<input id="submit" type="submit" value="Search" title="Search" align="middle" />
    	</form>
    </div>
	<div id="navigation">
		<ul id="simple-menu">
			<li>
				<a href="#">Searches<![if !(lte IE 6)]></a><![endif]>
    				<ul>
    					<li class="w">
    						<c:if test="${organism != null}">
    							<a class="w" href="<c:url value="/NamedFeature?organism=${organism}"/>" title="Name Search">By Name/Product</a>
    						</c:if>
    						<c:if test="${organism == null}">
    							<a class="w" href="<c:url value="/NamedFeature"/>" title="Name Search">By Name/Product</a>
    						</c:if>
    					</li>
    					<li class="w">
    						<a class="w" href="<c:url value="/"/>" title="Motif Search">Motif Search</a>
    					</li>
    				</ul>
                <!--[if lte IE 6]></a><![endif]-->
			</li>
			<li>
				<a href="#">Browse<![if !(lte IE 6)]></a><![endif]>
    				<ul class="w">
    					<li class="w">
    						<c:if test="${organism != null}">
    							<a class="w" href="<c:url value="/"/>BrowseCategory?category=genedb_products&organism=${organism}" title="Browse Products">Products</a>
    						</c:if>
    						<c:if test="${organism == null}">
    							<a class="w" href="<c:url value="/"/>BrowseCategory?category=genedb_products" title="Browse Products">Products</a>
    						</c:if>
    					</li>
     					<li class="w">
     						<c:if test="${organism != null}">
     							<a class="w" href="<c:url value="/"/>BrowseCategory?category=CC_genedb_controlledcuration&organism=${organism}" title="Browse Curation">Curation</a>
     						</c:if>
     						<c:if test="${organism == null}">
     							<a class="w" href="<c:url value="/"/>BrowseCategory?category=CC_genedb_controlledcuration" title="Browse Curation">Curation</a>
     						</c:if>
     					</li>
     					<li class="w">
     						<c:if test="${organism != null}">
     							<a class="w" href="<c:url value="/"/>BrowseCategory?category=biological_process&organism=${organism}" title="Browse GO Terms">GO Terms</a>
     						</c:if>
     						<c:if test="${organism == null}">
     							<a class="w" href="<c:url value="/"/>BrowseCategory?category=biological_process" title="Browse GO Terms">GO Terms</a>
     						</c:if>
     					</li>
    				</ul>
                <!--[if lte IE 6]></a><![endif]-->
			</li>
		</ul>
	</div>
</div>