<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="featureCvTerms" type="java.util.Collection" required="true" %>
<%@ attribute name="organism" type="java.lang.String" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="db" uri="db" %>

<c:if test="${fn:length(featureCvTerms) > 0}">
      <c:forEach items="${featureCvTerms}" var="fctDTO" varStatus="status">
        <tr>
        <th class="label"><c:if test="${status.first}">${title}</c:if></th>
        <td class="value name">${fctDTO.typeName}</td>
        <td class="value qualifiers">
            <c:forEach items="${fctDTO.props.qualifier}" var="qualifier" varStatus="st">
                <c:if test="${!st.first}"> | </c:if>
                ${qualifier}
            </c:forEach>
        </td>
        <td class="value evidence">
            <c:forEach items="${fctDTO.props.evidence}" var="evidence">
                    ${evidence}
            </c:forEach>&nbsp;
            <c:forEach items="${fctDTO.pubs}" var="pub">
                (${pub.uniqueName})
            </c:forEach>
        </td>
        <td class="value accession">
            <c:forEach items="${fctDTO.dbXRefDtoList}" var="fctdbx">
                <a href="${fctdbx.urlPrefix}${fctdbx.accession}">${fctdbx.dbName}:${fctdbx.accession}</a>
            </c:forEach>
            <c:if test="${fctDTO.withFrom != 'null'}">
                <!-- <a href="${PMID}${fctDTO.withFrom}">-->${fctDTO.withFrom}<!-- </a> -->
            </c:if>
        </td>
        </tr>
      </c:forEach>
 </c:if>