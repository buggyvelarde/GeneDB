package org.genedb.web.tags.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.PolypeptideRegion;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.web.mvc.model.TranscriptDTO;

public class ProteinMapTag extends SimpleTagSupport {
	
	private transient Logger logger = Logger.getLogger(ProteinMapTag.class);
	
	private int proteinMapCount = 0;
	private float proteinMapWidth = 875;
	private float proteinMapDomainHeight = 15;
	private float proteinMapDomainHeightSpace = 5;
	private float proteinMapBaseWidth;
	private StringBuffer sb = new StringBuffer();
    
	private Map<String,String> urls = new HashMap<String,String>();
	private Map<String,String> subsections = new HashMap<String,String>();
	
	private TranscriptDTO dto;
	
	public void setDto(TranscriptDTO dto) {
		this.dto = dto;
		proteinMapBaseWidth = (dto.getMax() - dto.getMin()) / 3;
		logger.info("proteinMapBaseWidth: " + proteinMapBaseWidth);
	}
	
	public TranscriptDTO  getDto() {
		return dto;
	}
	
	
	
	@Override
    public void doTag() throws JspException, IOException {
		JspWriter out = getJspContext().getOut();
		
		try {
		
			logger.info("going through dto " + dto + " - " + dto.getClass());
			
			for (PolypeptideRegionGroup subsection : dto.getDomainInformation()) {
				
				logger.info(subsection.getUniqueName() );
				
				float top = proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace) - (proteinMapDomainHeightSpace/2);
				float height =  (subsection.getSubfeatures().size() + 1 ) * (proteinMapDomainHeight+proteinMapDomainHeightSpace);
				
				logger.info(top + " " + height);
				
				sb.append(String.format(
						"<div class='proteinMapDomainSection' id='subsectionhit-%s' " +
							"title='%s - " +
							"%s' style=\"font-size:small;text-align:right;  background: rgb(222,222,222) ;  cursor:pointer;position:absolute;" +
							"top: %spx; " +
							"height: %spx; " +
							"width: %spx; \" > " +
							"%s </div>\n",
						proteinMapCount, 
						subsection.getUniqueName(), 
						subsection.getDescription(), 
						top, 
						height, 
						proteinMapWidth, 
						subsection.getUniqueName()));
				
				if (subsection.getUrl() != null)
					urls.put( "subsectionhit-" + proteinMapCount, subsection.getUrl()); 
				
				int proteinMapCountSubsection = proteinMapCount;
				proteinMapCount++;
				
				for (PolypeptideRegion hit : subsection.getSubfeatures()) {
					
					logger.info(hit.getUniqueName() + hit.getFmax() + " " + hit.getFmin());
					
					float hitTop = proteinMapCount * (proteinMapDomainHeight + proteinMapDomainHeightSpace);
					float hitLeft = (hit.getFmin() / proteinMapBaseWidth ) * proteinMapWidth ;
					float hitWidth = ((hit.getFmax() - hit.getFmin()) / proteinMapBaseWidth ) * proteinMapWidth ;
					
					logger.info(hitTop + " " + hitLeft + " " + hitWidth);
					logger.info(hit.getFmin() +"."+  proteinMapBaseWidth +"."+ proteinMapWidth );//<94.7249.875>  (94 / 7249) * 875 

					
					sb.append(String.format(
							"<div class='proteinMapDomain' id='hit-%s' " +
								"title='%s - " +
								"%s' " +
								"style=\"background: rgb(%s," +
								"%s," +
								"%s) ;  cursor:pointer;position:absolute;" +
								"top: %spx; " +
								"left: %spx; " +
								"width: %spx; height:15px; \" >&nbsp;</div>\n",
							proteinMapCount,
							hit.getUniqueName(),
							hit.getDescription(),
							hit.getColor().getRed(),
							hit.getColor().getGreen(),
							hit.getColor().getBlue(),
							hitTop,
							hitLeft,
							hitWidth
					));
					
					
					if (hit.getUrl() != null) {
						urls.put("hit-" + proteinMapCount, hit.getUrl());
						subsections.put("hit-" + proteinMapCount, "subsectionhit-" + proteinMapCountSubsection);
					}
					
					proteinMapCount++;
					
				}
				
			}
			
			sb.append("</div>");
			
			sb.append("<script>");
			
			//sb.append("$(document).ready(function() { $('#proteinMap').css(\"height\", "+totalHeight+" );");
			
	        sb.append("</script>");
	        
	        float totalHeight = proteinMapCount * (proteinMapDomainHeight+proteinMapDomainHeightSpace);
	        String start = String.format(
					"<div id='proteinMap' style=\"height:%spx; position:relative; width:%spx;border-bottom:1px solid black;margin:30px 0px;\">", 
					totalHeight, 
					proteinMapWidth);
	        
	        String s = start + sb.toString();
	        
			logger.info(s);
			
//			String heightAdjusted = sb.toString().replace("%HEIGHT%", String.valueOf(totalHeight));
//			logger.info(heightAdjusted);
			
			out.write(s);//.replace("%HEIGHT%", String.valueOf(totalHeight))
			
		} catch (Exception e) {
			logger.error(e);
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new JspException(e);
		}
		
		
	}
	
	private double step() {
		
		int stepNumber = 10;
		
		int diff = (dto.getMax() - dto.getMin()) / 3; // divide by 3 for amino acids
		int stepsUncorrected = diff / stepNumber;
		double log = Math.log10(stepsUncorrected);
		double round = Math.ceil(log);
		double step = Math.pow(10, round);
		logger.info(String.format("%s ... %s ...  %s ... %s  ... %s", diff, stepsUncorrected, log, round, step));
		return step;
	}
	
}
