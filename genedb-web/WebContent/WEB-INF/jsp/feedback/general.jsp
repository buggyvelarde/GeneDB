<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Feature: ${dto.uniqueName}">
<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
<misc:url value="/" var="base"/>
</format:header>
<format:page>
<br>

<div id="col-2-1">


<script type="text/javascript" src="http://api.recaptcha.net/challenge?k=6Lca2AgAAAAAAGMEG-487KWwxCTg9Ud4jIrpc4xn"></script>

<noscript>
   <iframe src="http://api.recaptcha.net/noscript?k=6Lca2AgAAAAAAGMEG-487KWwxCTg9Ud4jIrpc4xn"
       height="300" width="500" frameborder="0"></iframe><br>
   <textarea name="recaptcha_challenge_field" rows="3" cols="40">
   </textarea>
   <input type="hidden" name="recaptcha_response_field"
       value="manual_challenge">
</noscript>


<br />
<div id="geneDetails">
    <p>Blah, blah</p>
</div>

</div>
</format:page>
