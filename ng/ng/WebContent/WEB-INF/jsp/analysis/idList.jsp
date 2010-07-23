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

<p>Please enter your ids either:
<ul>
<li>as database cross-references (eg in the form GeneDB_Spombe:SPAC1002.09c).</li>
<li>or you can just use the <b>systematic IDs</b> eg (Tb927.1.710) <b>but</b> in this case you must also set the default organism.
</ul>

<textarea name="idList" rows="20" cols="70">
</textarea>
</p>

<p>Or upload a file</p>
<input type="file" name="ids"/>

<p>
            <input type="submit"/>
</p>

</sp:form>


</format:page>