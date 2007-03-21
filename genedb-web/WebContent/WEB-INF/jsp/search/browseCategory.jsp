<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Browse catalogues</title>
</head>
<body>

<p>This is a page for launching browse category stuff

<form:form action="/BrowseCategory" commandName="browseCategory">

<p><form:errors path="*" /></p>

<input type="hidden" name="org" value="tcruzi" />

<form:select path="category" items="${categories}" />

<input type="submit" />

</form:form>

</body>
</html>