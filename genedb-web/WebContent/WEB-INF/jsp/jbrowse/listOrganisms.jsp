<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Browse GeneDB organisms with JBrowse List" >
<script type="text/javascript">
    $(function(){
        $("#organismSelection").change(function() {
            if ($(this).val() != "Select organism")
            {
                window.location.href = "/jbrowse/" + $(this).val();
            }
        });
    });
</script>
</format:header>
<format:page>

<div id="col-2-1">

<format:genePageSection>

<h2>JBrowsable organisms</h2>

<P>Please select organism to viewed using <a href="http://www.jbrowse.org/">JBrowse</a>.</P> 
<P>
<select id="organismSelection">
    <c:forEach items="${commonNames}" var="commonName">
        <option value="${commonName}">${commonName}</option>
    </c:forEach>
</select>
</P>

</format:genePageSection>

</div>
</format:page>
