package org.genedb.web.mvc.controller.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.querying.tmpquery.IdsToGeneDetailQuery;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * The <DownloadProcess> can either be invoked on the command line or via the <DownloadController>. As the <DownloadProcess> is a main() entry
 * point, it can't be spring configured (or at least I don't know how). This class acts as a utility, which is spring configured, and is 
 * accessible to both of the other classes at runtime. 
 * 
 * @author gv1
 *
 */
public class DownloadProcessUtil {
	
	private SequenceDao sequenceDao;
    private JavaMailSender mailSender;
    private BerkeleyMapFactory bmf;
    
    private static final String DATE_FORMAT_NOW = "yyyy.MM.dd.HH.mm.ss";
    
    private TransactionTemplate transactionTemplate;
    
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
    	this.transactionTemplate = transactionTemplate;
    }
    
    public TransactionTemplate getTransactionTemplate() {
    	return transactionTemplate;
    }
    
    public void setMailSender(JavaMailSender mailSender) {
    	this.mailSender = mailSender;
    }
    
    
    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }
    
    public BerkeleyMapFactory getBmf() {
    	return bmf;
    }
    
    
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
    
    public SequenceDao getSequenceDao() {
    	return sequenceDao;
    }
    
    
    @SuppressWarnings("unchecked")
	private QueryFactory queryFactory;
    
    @SuppressWarnings("unchecked")
    public void setQueryFactory(QueryFactory queryFactory) {
    	this.queryFactory = queryFactory;
    }
    
    
    
    private File downloadTmpFolder;
    
    public void setDownloadTmpFolder(String downloadTmpFolder) throws Exception {
    	this.downloadTmpFolder = new File (downloadTmpFolder);
    	
    	if (this.downloadTmpFolder.isFile()) {
    		throw new Exception("Can't use the path to a file as a folder");
    	}
    	
    	if (! this.downloadTmpFolder.isDirectory()) {
    		this.downloadTmpFolder.mkdirs();
    	}
    }
    
    public File gettDownloadTmpFolder() {
    	return downloadTmpFolder;
    }
    
    private String baseDownloadUrl; 
    
    public void setBaseDownloadUrl(String baseDownloadUrl) {
    	this.baseDownloadUrl = baseDownloadUrl;
    }
    
    
    private long maxAtachmentSize = 5242880;
    
    public void sendEmail(String to, final String subject, String text, File attachment) throws javax.mail.MessagingException {
    	
    	MimeMessage message = mailSender.createMimeMessage();
    	
    	MimeMessageHelper helper = new MimeMessageHelper(message, true);
    	helper.setTo(to);
    	helper.setFrom(new InternetAddress("webmaster@genedb.org"));
    	helper.setSubject("Your GeneDB query results - " + subject);
    	
    	if (attachment != null) {
    		if (attachment.length() > maxAtachmentSize) {
    			text += "<p>The results are too large to attach. Please find them temporarily hosted here: ";
    			text += String.format("<a href='%s?file=%s'>%s</a>.</p>", baseDownloadUrl , attachment.getName(), subject);
    		} else {
    			FileSystemResource file = new FileSystemResource(attachment);
            	helper.addAttachment(file.getFilename(), file);
    		}
    		
    	}
    	
    	helper.setText(text, true);
    	
    	mailSender.send(message);
    	
    }
    
    public List<GeneDetail> getResults(List<String> uniqueNames) throws QueryException {
    	
    	@SuppressWarnings("unchecked")
    	IdsToGeneDetailQuery query = (IdsToGeneDetailQuery) queryFactory.retrieveQuery("idsToGeneDetail",  NumericQueryVisibility.PRIVATE);
        query.setIds(uniqueNames);
        
        @SuppressWarnings("unchecked")
        List<GeneDetail> results = query.getResults();
        
        return results;
    }
    
	public String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public File zip(File file) throws IOException {
		
		byte[] buf = new byte[1024]; 
		
		String zipFileName = file.getAbsolutePath() +".zip";
		
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		out.setLevel(Deflater.BEST_COMPRESSION);
    	
		FileInputStream in = new FileInputStream(file);
    	out.putNextEntry(new ZipEntry(file.getName()));
    	
    	int len; 
    	while ((len = in.read(buf)) > 0) { 
    		out.write(buf, 0, len); 
    	} 
    	
    	out.closeEntry(); 
    	in.close();
    	out.close();
    	
    	return new File(zipFileName);
    	
	}
	
}
