<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:header title="Number of TM domains Search" />
<format:page>
<br>

<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="${baseUrl}Query/proteinNumTM" method="GET">
            <table>
                <tr>
                  <td>
                    <br><big><b>Protein Number TM Search:</b></big>
                  </td>
                  <td><b>Organism:</b>
                    <br><db:simpleselect selection="${taxonNodeName}" />
                  </td>
                  <td>
                      <b>Min number of domains:</b>
                      <br><form:input id="minNumTM" path="min"/>
                    <br><font color="red"><form:errors path="min" /></font>
                  </td>
                  <td>
                      <b>Max number of domains:</b>
                    <br><form:input id="maxNumTM" path="max"/>
                    <br><font color="red"><form:errors path="max" /></font>
                  </td>
                  <td>
                    <br><input type="submit" value="Submit" />
                  </td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=5><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>

        </form:form>
    </format:genePageSection>
    
    <format:test-for-no-results />
</div>

<br><query:results />
</format:page>
