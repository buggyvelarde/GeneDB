<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="featureCvTerms" type="java.util.Collection" required="true" %>
<%@ attribute name="featureCounts" type="java.util.Collection" required="false" %>
<%@ attribute name="organism" type="java.lang.String" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="db" uri="db" %>

<c:if test="${fn:length(featureCvTerms) > 0}">
      <c:forEach items="${featureCvTerms}" var="featureCvTerm" varStatus="status">
        <tr>
        <td class="label"><c:if test="${status.first}">${title}</c:if></th>
        <td class="value name">${featureCvTerm.type.name}</td>
        <td class="value qualifiers">
        	<db:filtered-loop items="${featureCvTerm.featureCvTermProps}" cvTerm="qualifier" var="qualifier" varStatus="st">
        		<c:if test="${!st.first}"> | </c:if>
        		${qualifier.value}
            </db:filtered-loop>
        </td>
        <td class="value evidence">
            <db:filtered-loop items="${featureCvTerm.featureCvTermProps}" cvTerm="evidence" var="evidence">
            		${evidence.value}
            </db:filtered-loop>&nbsp;
            <c:forEach items="${featureCvTerm.pubs}" var="pub">
            	(${pub.uniqueName})
            </c:forEach>
        </td>
        <td class="value accession">
        	<c:forEach items="${featureCvTerm.featureCvTermDbXRefs}" var="fctdbx">
        		<a href="${fctdbx.dbXRef.db.urlPrefix}${fctdbx.dbXRef.accession}">${fctdbx.dbXRef.db.name}:${fctdbx.dbXRef.accession}</a>
        	</c:forEach>
        	<c:if test="${featureCvTerm.pub.uniqueName != 'null'}">
        		<a href="${PMID}${featureCvTerm.pub.uniqueName}">${featureCvTerm.pub.uniqueName}</a>
        	</c:if>
        </td>
        <c:if test="${featureCounts != null}">
        <td class="value others">
        	<c:forEach items="${featureCounts}" var="nc">
        		<c:if test="${nc.name == featureCvTerm.cvTerm.name}">
        			<c:if test="${nc.count == 1}" >
        				0 Others
        			</c:if>
        			<c:if test="${nc.count > 1}" >
                        <c:url value="/BrowseTerm" var="othersUrl">
                            <c:param name="organism" value="${organism}"/>
                            <c:param name="term" value="${featureCvTerm.cvTerm.name}"/>
                            <c:param name="category" value="${featureCvTerm.cvTerm.cv.name}"/>
                            <c:param name="json" value="false"/>
                        </c:url>
        				<a href="${othersUrl}"> ${nc.count - 1} Others </a>
        			</c:if>
        		</c:if>
        	</c:forEach>
        </td>
        </c:if>
        </tr>
      </c:forEach>
 </c:if>