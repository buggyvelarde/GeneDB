<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
	<st:init />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection className="whiteBox">
		<display:table name="results"  uid="row" pagesize="30" requestURI="/BrowseCategory" class="simple" cellspacing="0" cellpadding="4">
		   	<display:column property="name" title="Category" href="BrowseTerm?org=${organism}&category=${category}" paramId="term"/>
		   	<display:column property="count" title="Count"/>
		</display:table>
	</format:genePageSection>
</div>
<format:footer />