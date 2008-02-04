<%@ tag display-name="go-section"
        body-content="empty" %>
<%@ attribute name="cvName" required="true" %>
<%@ attribute name="feature" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>

<tr><td colspan="6">${title}</td></tr>
<db:filtered-loop items="${polypeptide.featureCvTerms}" cv="${cvName}" var="featCvTerm" varStatus="status">
  <tr>
    <td>GO:${featCvTerm.cvTerm.dbXRef.accession}</td>
    <td>${featCvTerm.cvTerm.name}</td>
    <td><db:propByName collection="${featCvTerm.featureCvTermProps}" name="qualifier" var="qualifiers">
    <c:forEach items="${qualifiers}" var="qualifier" varStatus="st"><c:if test="${st.count > 1}"> | </c:if>${qualifier.value}</c:forEach></db:propByName></td>
    <td><db:propByName collection="${featCvTerm.featureCvTermProps}" name="evidence" var="evidence">
    <c:forEach items="${evidence}" var="ev">${ev.value}</c:forEach></db:propByName>&nbsp;
    <c:forEach items="${featCvTerm.featureCvTermPubs}" var="fctp">(${fctp.pub.uniqueName})</c:forEach></td>
    <td><c:forEach items="${featCvTerm.featureCvTermDbXRefs}" var="fctdbx">${fctdbx.dbXRef.db.name}${fctdbx.dbXRef.accession}</c:forEach></td>
  </tr>
</db:filtered-loop>