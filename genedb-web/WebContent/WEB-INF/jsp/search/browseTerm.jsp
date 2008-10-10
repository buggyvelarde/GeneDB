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
                  <option value="Pfalciparum">Pfalciparum</option>
                  <option value="Pberghei">Pberghei</option>
                  <option value="Pchabaudi">Pchabaudi</option>
                  <option value="Pknowlesi">Pknowlesi</option>
                  <option value="Pvivax">Pvivax</option>
                  <option value="Pyoelii">Pyoelii</option>
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