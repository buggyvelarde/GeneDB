<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="pepProps" type="org.gmod.schema.utils.PeptideProperties" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri=http://java.sun.com/jsp/jstl/functions %>
<%@ taglib prefix="db" uri="db" %>

<h2>&nbsp;&nbsp;Predicted Peptide Data</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
  <c:if test="${pepProps.isoelectricPoint != null}">
  <tr>
    <th>Isoelectric Point</th>
    <td>pH ${pepProps.isoelectricPoint}</td>
  </tr>
  </c:if>
  <c:if test="${pepProps.hasMass}">
  <tr>
    <th>Mass</th>
    <td>${pepProps.mass}</td>
  </tr>
  </c:if>
  <tr>
    <th>Charge</th>
    <td>${pepProps.charge}</td>
  </tr>
  <tr>
    <th>Amino Acids</th>
    <td>${pepProps.aminoAcids}</td>
  </tr>
</table>

