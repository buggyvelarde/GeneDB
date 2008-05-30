<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Form Input</title>
</head>
<body>
<p>Form Input</p>

<form:form>
<select name="topLevelFeature">
<option value="Pf3D7_01">Pf3D7_01</option>
<option value="Pf3D7_02">Pf3D7_02</option>
<option value="Pf3D7_03">Pf3D7_03</option>
<option value="Pf3D7_04">Pf3D7_04</option>
<option value="Pf3D7_05">Pf3D7_05</option>
<option value="Pf3D7_06">Pf3D7_06</option>
<option value="Pf3D7_07">Pf3D7_07</option>
<option value="Pf3D7_08">Pf3D7_08</option>
<option value="Pf3D7_09">Pf3D7_09</option>
<option value="Pf3D7_10">Pf3D7_10</option>
<option value="Pf3D7_11">Pf3D7_11</option>
<option value="Pf3D7_12">Pf3D7_12</option>
<option value="Pf3D7_13">Pf3D7_13</option>
<option value="Pf3D7_14">Pf3D7_14</option>
</select>
<input type="hidden" name="outputFormat" value="EMBL_REMAPPING" />

<input type="submit" value="Get output" />
</form:form>

</body>
</html>