<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Homepage" />
<format:page>
<br>

<format:genePageSection>
  <h2>Stuff</h2>
  <p>Maybe a repeat of the logo, and a list of orgs here?</p>
</format:genePageSection>

<format:genePageSection>
  <h2>Group Description</h2>
  <p>${mainDescription}</p>
</format:genePageSection>

</format:page>
