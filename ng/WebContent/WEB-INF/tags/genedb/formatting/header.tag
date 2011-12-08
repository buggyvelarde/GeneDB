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
    <link rel="stylesheet" href="<misc:url value="/includes/scripts/jquery/jquery-ui-1.8.16.custom/css/smoothness/jquery-ui-1.8.16.custom.css"/>" type="text/css"/>
    
    <script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-ui-1.8.16.custom/js/jquery-1.6.2.min.js"/>" ></script>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-ui-1.8.16.custom/js/jquery-ui-1.8.16.custom.min.js"/>" ></script>
    
    <script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/util.js"/>"></script>
    
    <link rel="SHORTCUT ICON" href="<misc:url value="/includes/image/favicon.png"/>" >
    
    <script type="text/javascript">
	    
        $(function(){
          $("#nav > li")
            .mouseover(function(){$(this).addClass("over");})
            .mouseout (function(){$(this).removeClass("over");});
          
        });

        /*
            Returns the base URL. 
        */
        function getBaseURL() {
            return "<misc:url value="/"/>";
        }
        
        
    </script>

    <jsp:doBody />
</head>



