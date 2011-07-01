<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="algData" type="java.util.Map" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="db" uri="db" %>

<div id="col-4-1">
<div class="main-grey-3-4-top"></div>
<div class="light-grey">
    <h2>Algorithmic Predictions</h2>
    <table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
    <c:if test="${algData.SignalP != null}">
      <tr>
      <th>SignalP</td>
      <td>Predicted ${algData.SignalP.prediction}
                        (Signal peptide probability ${algData.SignalP.peptideProb},
                        signal anchor probability ${algData.SignalP.anchorProb}).
      <c:if test="${algData.SignalP.cleavageSite != null}">
                        Predicted cleavage site at ${algData.SignalP.cleavageSite}
                        with probability ${algData.SignalP.cleavageSiteProb}.
      </c:if>
      </td>
      </tr>
    </c:if>
    <c:if test="${algData.TMHMM != null}">
      <tr>
        <th>TMHMM</td>
        <td>Predicted ${fn:length(algData.TMHMM)}
                          transmembrane region<c:if test="${fn:length(algData.TMHMM) > 1}">s</c:if>
                          at locations
          <c:forEach var="helix" varStatus="status" items="${algData.TMHMM}">
            <c:if test="${!status.first && !status.last}">,</c:if>
            <c:if test="${status.last && !status.first}">and </c:if>
            ${helix}
          </c:forEach>.
        </td>
      </tr>
    </c:if>
    <c:if test="${algData.DGPI != null && algData.DGPI.anchored}">
      <tr>
        <th>DGPI</th>
        <td>
          <c:if test="${algData.DGPI.anchored}">This protein is GPI-anchored.</c:if>
          <c:if test="${!algData.DGPI.anchored}">This protein is <b>not</b> GPI-anchored.</c:if>
          <c:if test="${algData.DGPI.location != null}">Predicted cleavage site at ${algData.DGPI.location} with score ${algData.DGPI.score}.</c:if>
        </td>
      </tr>
    </c:if>
    <c:if test="${algData.PlasmoAP != null}">
      <tr>
        <th>PlasmoAP</th>
        <td>${algData.PlasmoAP.description} apicoplast-targeting protein (score ${algData.PlasmoAP.score}).</td>
      </tr>
    </c:if>
  </table>
  </div>
<div class="main-grey-3-4-bot"></div>
</div>