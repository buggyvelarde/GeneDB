package org.displaytag.sample;

import org.displaytag.decorator.TableDecorator;
import org.gmod.schema.cv.CvTerm;

public class Wrapper extends TableDecorator{
	public String getLink1()
	{
	        CvTerm cvTerm = (CvTerm)getCurrentRowObject();
	        String name = cvTerm.getName();
	        String cv = cvTerm.getCv().getName();
	        
	        return "<a href=\"./CvTermByCvName?cvTermName=" + name + "&cvName=" + cv + "\">" + name + "</a>";
	}


	public String getLink2()
	{
		CvTerm cvTerm = (CvTerm)getCurrentRowObject();
        String name = cvTerm.getName();
        String cv = cvTerm.getCv().getName();
        
        return "<a href=\"./GenesByCvTermAndCvName?cvTermName=" + name + "&cvName=" + cv + "\">Get genes which contain this CvTerm</a>";
	}
}
