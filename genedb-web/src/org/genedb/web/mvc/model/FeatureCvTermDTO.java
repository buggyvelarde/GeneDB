package org.genedb.web.mvc.model;

import org.gmod.schema.mapped.FeatureCvTerm;

import java.util.List;

public class FeatureCvTermDTO {
    String typeName;
    List<String> qualifiers;
    String evidence;
    List<String> pubs;

    public FeatureCvTermDTO(FeatureCvTerm fct) {
        // TODO Auto-generated constructor stub
    }



//  <c:if test="${fn:length(featureCvTerms) > 0}">
//    <td class="value evidence">
//        <db:filtered-loop items="${featureCvTerm.featureCvTermProps}" cvTerm="evidence" var="evidence">
//                ${evidence.value}
//        </db:filtered-loop>&nbsp;
//        <c:forEach items="${featureCvTerm.pubs}" var="pub">
//            (${pub.uniqueName})
//        </c:forEach>
//    </td>
//    <td class="value accession">
//        <c:forEach items="${featureCvTerm.featureCvTermDbXRefs}" var="fctdbx">
//            <a href="${fctdbx.dbXRef.db.urlPrefix}${fctdbx.dbXRef.accession}">${fctdbx.dbXRef.db.name}:${fctdbx.dbXRef.accession}</a>
//        </c:forEach>
//        <c:if test="${featureCvTerm.pub.uniqueName != 'null'}">
//            <a href="${PMID}${featureCvTerm.pub.uniqueName}">${featureCvTerm.pub.uniqueName}</a>
//        </c:if>
//    </td>
//    <c:if test="${featureCounts != null}">
//    <td class="value others">
//        <c:forEach items="${featureCounts}" var="nc">
//            <c:if test="${nc.name == featureCvTerm.cvTerm.name}">
//                <c:if test="${nc.count == 1}" >
//                    0 Others
//                </c:if>
//                <c:if test="${nc.count > 1}" >
//                    <c:url value="/BrowseTerm" var="othersUrl">
//                        <c:param name="organism" value="${organism}"/>
//                        <c:param name="term" value="${featureCvTerm.cvTerm.name}"/>
//                        <c:param name="category" value="${featureCvTerm.cvTerm.cv.name}"/>
//                        <c:param name="json" value="false"/>
//                    </c:url>
//                    <a href="${othersUrl}"> ${nc.count - 1} Others </a>
//                </c:if>
//            </c:if>
//        </c:forEach>
//    </td>
//    </c:if>
//    </tr>
//  </c:forEach>
//  <%-- Controlled Curation Section --%>
//  <db:filterByType items="${polypeptide.featureCvTerms}" cvPattern="CC_.*" var="controlledCurationTerms"/>
//  <c:if test="${fn:length(controlledCurationTerms) > 0}">
//      <format:genePageSection id="controlCur">
//          <div class="heading">Controlled Curation</div>
//          <table width="100%" class="go-section">
//              <format:featureCvTerm-section featureCvTerms="${controlledCurationTerms}" featureCounts="${CC}" organism="${organism}"/>
//          </table>
//      </format:genePageSection>
//  </c:if>

}
