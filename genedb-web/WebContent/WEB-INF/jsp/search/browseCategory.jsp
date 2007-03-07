<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Wibble data binding</title>
</head>
<body>

<p>This is a test of Wibble data binding 

<form:form action="/genedb-web/FlatFileReport">

<p><form:errors path="org" /></p>

<input type="hidden" name="org" value="leela:nyssa" />


<input type="submit" />

</form:form>

</body>
</html>