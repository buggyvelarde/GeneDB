package org.genedb.db.domain.services;

import org.genedb.db.domain.misc.Message;

import java.util.Deque;

public interface MessageService {

	void addNotification(String clientName, String string, String string2);
	
	Deque<Message> checkMessages(String clientName);

}
