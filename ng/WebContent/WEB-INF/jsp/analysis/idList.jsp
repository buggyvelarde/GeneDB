<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>
<format:header title="Id List Upload" />
<format:page>
<html>
  <head>
     <title>ID List Upload</title>
     <meta name="description" content="Identify protein sequences within
     GeneDB which match a particular pattern">

<sp:form action="IdList" method="post" enctype="multipart/form-data">

<br />
<p>Please enter your transcript ids:<br>

<textarea name="idList" rows="20" cols="70">${idList}
</textarea>

&nbsp;</p>&nbsp;</p>

<p>Or upload a file</p>
<input type="file" name="ids"/>

<p>
            <input type="submit"/>
</p>

</sp:form>


</format:page>