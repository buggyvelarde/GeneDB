<%@ tag display-name="moveToBasket"
        body-content="empty" %>
<%@ attribute name="uniqueName" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="misc" %>

<script type="text/javascript">
//<![CDATA[
function addToBasket(geneid){
  $.get("<misc:url value="/Basket/"/>"+geneid, {}, function(content){
    $("#basketbutton")
        .attr('src', "<misc:url value="/includes/image/button-added-to-basket.gif" />")
        .unbind('click')
        .css({'cursor': 'default'});
  });
}
//]]>
</script>

<c:choose>
<c:when test="${inBasket eq false}">
<img id="basketbutton" src="<misc:url value="/includes/image/button-add-to-basket.gif"/>" alt="Add to Basket" onclick="addToBasket('${dto.uniqueName}')" height="46" width="144" style="cursor: pointer;">
</c:when>
<c:otherwise>
<img src="<misc:url value="/includes/image/button-added-to-basket.gif" />" alt="Added to basket">
</c:otherwise>
</c:choose>

