package org.genedb.install;

class AddAndMailNewUsers {

	def templateMail = '''This is an email with your account details for external access
		to the continuing malaria reannotation process. The details below are for both 
	VPN (Zuul) access to the Sanger and logging into the GeneDB database via Artemis. Specific details 
	of how to use these, ''';
	
	
	
	Map details = ['art@sanger.ac.uk' : 'bbujkb',
	               'maa@sanger.ac.uk' : 'bbulbiu']
    
    boolean sendEmail = false

	def ant = new AntBuilder();
	
	void process() {
		for (entry in details) {
			def templateSql = "CREATE ROLE '${entry.key}' WITH LOGIN ENCRYPTED PASSWORD '${entry.value}'";
			System.out.println(templateSql)
			
			
			if (sendMail) {
				ant.mail(mailhost: 'mail.sanger.ac.uk', subject: 'Malaria reannotation external access part I') {
					from(address: 'art@sanger.ac.uk')
					to(address: entry.value)
					message(templateMail)
				}
			}
		}
	}
	
        
	static void main(args) {
    	AddAndMailNewUsers app = new AddAndMailNewUsers()
    	app.process();
    	System.err.println("Done");
	}

}