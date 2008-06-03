<%@ tag display-name="go-section" body-content="empty"%>
<%@ attribute name="featureCvTerms" type="java.util.Collection" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="db" uri="db" %>

<c:if test="${fn:length(featureCvTerms) > 0}">
      <c:forEach items="${featureCvTerms}" var="featureCvTerm" varStatus="status">
        <tr>
        <td class="label"><c:if test="${status.first}">${title}</c:if></th>
        <td class="value name">${featureCvTerm.cvTerm.name}</td>
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
            <c:forEach items="${featureCvTerm.featureCvTermPubs}" var="fctp">
            	(${fctp.pub.uniqueName})
            </c:forEach>
        </td>
        <td class="value accession">
        	<c:forEach items="${featureCvTerm.featureCvTermDbXRefs}" var="fctdbx">
        		${fctdbx.dbXRef.db.name}${fctdbx.dbXRef.accession}
        	</c:forEach>
        </td>
        <td class="value others">
        	<c:if test="${fn:length(featureCvTerm.cvTerm.featureCvTerms) == 1}" >
        		0 Others
        	</c:if>
        	<c:if test="${fn:length(featureCvTerm.cvTerm.featureCvTerms) > 1}" >
        		<c:set var="cnt" value="0"/>
        		<c:forEach items="${featureCvTerm.cvTerm.featureCvTerms}" var="fct">
        			<db:filtered-loop items="${fct.featureCvTermProps}" cvTerm="qualifier" var="qualifier" varStatus="st">
        				<c:if test="${qualifier.value == 'NOT'}"> 
        					${cnt++}
        				</c:if>
        			</db:filtered-loop>
        		</c:forEach>
        		<a href="<c:url value="/"/>GenesByCvTermAndCv?cvTermName=${featureCvTerm.cvTerm.name}&cvName=${featureCvTerm.cvTerm.cv.name}"> ${fn:length(featureCvTerm.cvTerm.featureCvTerms)-cnt} Others </a>
        	</c:if>
        </td>
        </tr>
      </c:forEach>
 </c:if>