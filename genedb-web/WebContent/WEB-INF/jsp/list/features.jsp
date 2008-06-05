<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:headerRound title="Gene Results List">
    <st:init />
</format:headerRound>
<format:genePageSection id="controlCur">
    <display:table name="results" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
    	<display:column property="organism.abbreviation" title="Organism"/>
    	<display:column property="cvTerm.name" title="Type"/> 
    	<display:column property="uniqueName" href="./NamedFeature" paramId="name"/>
    </display:table>
</format:genePageSection>
<format:footer />