<html>
<head>
    <title>JavaScript Menu - Demo #1</title>
    <meta name="description" content="Free Cross Browser Javascript DHTML Menu Navigation">
    <meta name="keywords" content="JavaScript menu, DHTML menu, client side menu, dropdown menu, pulldown menu, popup menu, web authoring, scripting, freeware, download, shareware, free software, DHTML, Free Menu, site, navigation, html, web, netscape, explorer, IE, opera, DOM, control, cross browser, support, frames, target, download">
    <link rel="shortcut icon" href="http://www.softcomplex.com/products/tigra_menu/favicon.ico">
    <meta name="robots" content="index,follow">
<style>
    a, A:link, a:visited, a:active
        {color: #0000aa; text-decoration: none; font-family: Tahoma, Verdana; font-size: 11px;}
    A:hover
        {color: #ff0000; text-decoration: none; font-family: Tahoma, Verdana; font-size: 11px;}
    p, tr, td, ul, li
        {color: #000000; font-family: Tahoma, Verdana; font-size: 11px;}
    .header1, h1
        {color: #ffffff; background: #4682B4; font-weight: bold; font-family: Tahoma, Verdana; font-size: 13px; margin: 0px; padding: 2px;}
    .header2, h2
        {color: #000000; background: #DBEAF5; font-weight: bold; font-family: Tahoma, Verdana; font-size: 12px;}
    .intd
        {color: #000000; font-family: Tahoma, Verdana; font-size: 11px; padding-left: 15px;}

</style>
<!-- styles for demo menu #1-->
<link rel="stylesheet" href="/genedb-web/includes/style/menu.css">
</head>

<body bottommargin="15" topmargin="15" leftmargin="15" rightmargin="15" marginheight="15" marginwidth="15" bgcolor="white">



<!-- menu script itself. you should not modify this file -->
<script language="JavaScript" src="/genedb-web/includes/scripts/menu.js"></script>
<!-- items structure. menu hierarchy and links are stored there -->
<script language="JavaScript" src="/genedb-web/includes/scripts/menu_items.js"></script>
<!-- files with geometry and styles structures -->
<script language="JavaScript" src="/genedb-web/includes/scripts/menu_tpl.js"></script>
<script language="JavaScript">
    <!--//
    // Note where menu initialization block is located in HTML document.
    // Don't try to position menu locating menu initialization block in
    // some table cell or other HTML element. Always put it before </body>

    // each menu gets two parameters (see demo files)
    // 1. items structure
    // 2. geometry structure

    new menu (MENU_ITEMS, MENU_POS);
    // make sure files containing definitions for these variables are linked to the document
    // if you got some javascript error like "MENU_POS is not defined", then you've made syntax
    // error in menu_tpl.js file or that file isn't linked properly.
    
    // also take a look at stylesheets loaded in header in order to set styles
    //-->
</script>

</body>
</html>