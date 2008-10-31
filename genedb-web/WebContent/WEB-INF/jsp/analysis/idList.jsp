<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>
<format:header name="GeneDB Motif Search">
	<st:init />
</format:header>
<html>
  <head>
     <title>ID List Upload</title>
     <meta name="description" content="Identify protein sequences within
     GeneDB which match a particular pattern">

<sp:form action="IdList" method="post">

<p>Please enter your ids either:
<ul>
<li>as database cross-references (eg in the form GeneDB_Spombe:SPAC1002.09c).
<li>or you can just use the <b>systematic IDs</b> eg (Tb927.1.710) <b>but</b> in this case you must also set the default organism.
</ul>

<textarea name="ids" rows="20" cols="70">
</textarea>

        <form method="post" action="upload.form" enctype="multipart/form-data">
            <input type="file" name="file"/>
            <input type="submit"/>
        </form>


<p>

</sp:form>


<table>
<tr>
<td>

Organism:
</td>
<td>
</tr>

</table>

<table width="100%">
	<tr valign="center">
	  <td align="center">
	    <INPUT TYPE="submit" VALUE="Start Motif Search">
	    &nbsp;&nbsp;&nbsp;&nbsp;<INPUT TYPE="reset">
	    <BR><BR>
	  </td>
	</tr>
</table>
<!-- </FORM> -->

<format:footer />