<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<format:header title="Location Search" />
<format:page>
<br />

<div id="geneDetails">
    <format:genePageSection id="nameSearch" className="whiteBox">
        <form:form commandName="query" action="geneLocation" method="GET">
            <table border=0>
                <tr>
                    <td>
                        <br><big><b>Gene Location Search:</b></big>
                    </td>
                    <td>
                        <b>Parent feature:&nbsp;</b>
                        <br><form:input id="topLevelFeatureName" path="topLevelFeatureName"/>
                        <br><font color="red"><form:errors path="topLevelFeatureName" /></font>
                    </td>
                    <td>
                        <b>Start:</b>
                        <br><form:input id="minLocation" path="min"/>
                        <br><font color="red"><form:errors path="min" /></font>
                    </td>
                    <td>
                        <b>End:</b>
                        <br><form:input id="maxLocation" path="max"/>
                        <br><font color="red"><form:errors path="max" /></font>
                    </td>

                     <td>
                         <b>Pseudogene:</b>
                          <br><form:checkbox id="pseudogenes" path="pseudogenes" />
                      </td>
                      <td>&nbsp;&nbsp;&nbsp;</td>
                    <td>
                        <br><input type="submit" value="Submit" />
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td colspan=7><font color="red"><form:errors  /></td>
                    <td></td>
                </tr>
            </table>
        </form:form>

        <table>
        <tr><th><b>Organism</b></th><th><b>Example parent features (chromosomes and/or contigs)</b></th></tr>
        <tr><td><i>T. brucei</i></td><td>Tb927_01_v4,  Tb927_02_v4</td></tr>
        <tr><td><i>L. major</i></td><td>Lmjchr1, Lmjchr2</td></tr>
        <tr><td><i>P. falciparum</i></td><td>Pf3D7_01, Pf3D7_02</td></tr>
        </table>




    </format:genePageSection>
    
    <format:test-for-no-results />
</div>


<br><query:results />
</format:page>