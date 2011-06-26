<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="misc" uri="misc" %>
<format:header title="Date Search" />
<format:page>
<br>

<script>

$(function() {

	$("#date").datepicker({
	    maxDate: '+0D', 
	    dateFormat: 'yyyy/mm/dd',  
	    selectedDate: "${date}"
	     
	});

});

</script>

<div id="geneDetails">
	<format:genePageSection id="nameSearch" className="whiteBox">
		<form:form commandName="query" action="${actionName}" method="GET">
            <table>
                <tr>
                    <td width=180>
                        <b>Organism:</b><br>
                        <db:simpleselect selection="${taxonNodeName}" />
                        <br><font color="red"><form:errors path="taxons" /></font>
                        <font color="red"><form:errors path="*" /></font>
                    </td>
                  <td>
                    <b>Date:</b><br>
                    <form:input id="date" path="date"/> (YYYY/MM/DD)
                  </td>
                </tr>
                <tr>
                  <td>Created:</td>
                  <td>
                    <form:checkbox id="created" path="created"/>
                  </td>
                </tr>
                <tr>
                  <td>After:</td>
                  <td>
                    <form:checkbox id="after" path="after"/>
                  </td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                  <td colspan="2">
                    <input type="submit" value="Submit" />
                  </td>
                  <td>&nbsp;</td>
                </tr>
            </table>

		</form:form>
	</format:genePageSection>

	<format:test-for-no-results />
</div>

<br><query:results />
</format:page>