package org.genedb.web.mvc.controller.download;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.gmod.schema.mapped.Feature;

/**
 * @author gv1
 */
public class FormatExcel extends FormatBase {
	
	HSSFWorkbook workbook;
	HSSFSheet sheet;
	int rcount = 0;
	OutputStream outputStream;
	
	public FormatExcel() {
		super();		
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet();
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	@Override
	public void formatBody(List<Feature> features) throws IOException {
		
		for (Feature feature : features) {
			
			HSSFRow row = sheet.createRow(rcount);
			
			short count = 0;
			for (String fieldValue : getFieldValues(feature, outputOptions)) {
				
				HSSFCell cell = row.createCell(count);
				HSSFRichTextString richVal = new HSSFRichTextString(fieldValue);
				cell.setCellValue(richVal);
				
				count++;
				
			}
			
			rcount++;
		}
		
	}

	@Override
	public void formatFooter() throws IOException {
		workbook.write(outputStream);
	}
	
	@Override
	public void formatHeader() throws IOException {
		
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
		
		HSSFRow heading = sheet.createRow(rcount);
		short count = 0;
		for (OutputOption outputOption : outputOptions) {
			
			HSSFCell cell = heading.createCell(count);
			cell.setCellStyle(style);
			
			String value = outputOption.name();
			HSSFRichTextString richVal = new HSSFRichTextString(value);
			cell.setCellValue(richVal);
			count++;
		}
		rcount++;

	}

}
