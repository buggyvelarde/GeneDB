<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="db" uri="db" %>

<format:headerRound title="Browse By Term" onLoad="doSomething(); DWRUtil.useLoadingMessage(); createAutoCompleter()" bodyClass="genePage">
  <st:init/>
  <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
  <format:genePageSection id="browseTerm" className="whiteBox">
    <form:form action="BrowseTerm" commandName="browseTerm" method="get" onsubmit="">
      <table>
        <tr>
          <td><form:errors path="*" /></td>
        </tr>
          <tr>
              <td>Organisms:</td>
              <td>
                <select name="organism">
                  <option value="Plasmodium">Plasmodium</option>
                  <option value="Pfalciparum">P. falciparum</option>
                  <option value="Pberghei_3x">P. berghei</option>
                  <option value="Pchabaudi">P. chabaudi</option>
                  <option value="Pknowlesi">P. knowlesi</option>
                  <option value="Pvivax">P. vivax</option>
                  <option value="Pyoelii">P. yoelii</option>
                </select>
              </td>
          </tr>
          <tr>
            <td>Browse category:</td>
            <td><form:select id="category" path="category" items="${categories}" /></td>
            <td></td>
          </tr>
            <tr>
            <td>Term:</td>
            <td><form:input id="textInput" path="term" size="50"/><div style="background-color: #2C5F93;" id="suggestions"></div></td>
          </tr>
          <tr>
              <td>&nbsp;</td>
            <td colspan="2"><input type="submit" value="Submit" /> <input type="reset" value="reset" onclick="resetall()"/></td>
          </tr>
      </table>
    </form:form>
  </format:genePageSection>
</div>
<format:footer />