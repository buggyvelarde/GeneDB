<%@ tag display-name="header" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="title"  required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>${title} - GeneDB</title>

    <script type="text/javascript" src="http://js.sanger.ac.uk/urchin.js"></script>

    <link rel="stylesheet" href="<misc:url value="/includes/style/genedb/main.css"/>" type="text/css" />
    <script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>

    <script type="text/javascript">
        $(function(){
          $("#nav > li")
            .mouseover(function(){$(this).addClass("over");})
            .mouseout (function(){$(this).removeClass("over");});
        
          var logos = [
            '<misc:url value="/includes/image/GeneDB-logo.png"/>',
            '<misc:url value="/includes/image/GeneDB-logo-mirrored.png"/>',
            '<misc:url value="/includes/image/GeneDB-logo-alt1.png"/>',
            '<misc:url value="/includes/image/GeneDB-logo-alt2.png"/>',
            '<misc:url value="/includes/image/GeneDB-logo-alt3.png"/>'
          ];
          var logo_index = 0;
          $("#logo").click(function() {
            logo_index = (logo_index + 1) % logos.length;
            console.log("Logo index is now %d", logo_index);
            $(this).attr('src', logos[logo_index]);
            return false;
          });
        });
    </script>

    <jsp:doBody />
</head>

