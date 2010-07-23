package org.genedb.install;

class AddAndMailNewUsers {
    
    boolean sendMail = false
	def ant = new AntBuilder();
	
	void process() {
		for (entry in details) {
			def templateSql = "CREATE ROLE \"${entry.key}\" WITH LOGIN ENCRYPTED PASSWORD '${entry.value}';";
			System.out.println(templateSql)
			
			if (sendMail) {
				def templateMail = "This is an email with your account details for external access\nto the continuing malaria reannotation process. The details below are for both \nVPN (Zuul) access to the Sanger and logging into the GeneDB database via Artemis. Specific details \nof how to use these will be in a follow-up message.\n\nUsername: ${entry.key}\nPassword: ${entry.value}\n\nWe plan to contact a few users later today for testing external access, and then send a \nfurther email to everybody \n\nThanks,\nAdrian\n";
				ant.mail(mailhost: 'mail.sanger.ac.uk', subject: 'Malaria reannotation external access part I') {
					from(address: 'art@sanger.ac.uk')
					to(address: entry.key)
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

	
	Map details = ['art@sanger.ac.uk' : 'bbujkb',
	               'maa@sanger.ac.uk' : 'bbulbiu']

}