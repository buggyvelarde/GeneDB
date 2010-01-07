
var iisIE = false; //global flag;
var ireq;  //global request and XML document objects;

window.onload = init;


function init() {

	initProcessReqChange();

}


function initProcessReqChange() {

	createFlashTab();

}

function createFlashTab(){
		//var sel = document.getElementById('geneoptions');
		//var annotationfile = sel.options[sel.selectedIndex].value;
		//if (annotationfile == null) {
			annotationfile = "GViewer/data/annotation.xml";
		//}
		var src = 'GViewer/GViewer2.swf?titleBarText='; //removed 'http://rgd.mcw.edu/diseases/';
		var flashVars = '&lcId=1234567890&longestChromosomeLength=3291871&baseMapURL=GViewer/data/base.xml';
		var browseURL = '&browserURL=http://mcnally.hmgc.mcw.edu/gb/gbrowse/rgd_903/?name=Chr&';
		var annotationXML = '&annotationXML=';
		var thisName = 'Sample Data';
		var url = src + thisName + flashVars + browseURL + annotationXML + annotationfile;
		// + '&dimmedChromosomeAlpha=40&bandDisplayColor=0x0099FF&wedgeDisplayColor=0xCC0000'
	
		var embed = document.createElement('embed');
		embed.setAttribute('width', 800);
		embed.setAttribute('height', 240);
		embed.setAttribute('src', url);
	
		var div = document.getElementById('gviewer');
		div.innerHTML = ' ';	//clear this div first;
		div.appendChild(embed);	//div.appendChild(newTable);
	
		//print 'view large' button;
		//var largebutton = document.getElementById('large-button');
		//var largeLink = " <a href=\"javascript:createFlashLarge('" + thisIDs + "','" + thisName + "','" + datafile + "');\" class=\"asubtitle\" onClick=\"setBgColorRight('large-button');\"> Large View... </a> ";
		//largebutton.innerHTML = largeLink;
	
		//openClose('GViewerSpace'); //make all browsers expanded and shrunk easily;
	
	}	



