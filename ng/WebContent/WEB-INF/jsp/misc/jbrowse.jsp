<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:header title="Browse GeneDB organisms with JBrowse List" />
<format:page>

<div id="col-2-1">

<format:genePageSection>

<h2>JBrowsable organisms</h2>

<P>Please select organism to view using <a href="http://www.jbrowse.org/">JBrowse</a>.</P>

<db:homepageselect leafOnly="true" baseUrl="/jbrowse/" suffix="/?tracks=Complex%20Gene%20Models" />

</format:genePageSection>

</div>
</format:page>
