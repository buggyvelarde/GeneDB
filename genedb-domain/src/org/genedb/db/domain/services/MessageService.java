package org.genedb.db.domain.services;

import java.util.Deque;

import org.genedb.db.domain.misc.Message;

public interface MessageService {

	void addNotification(String clientName, String string, String string2);
	
	Deque<Message> checkMessages(String clientName);

}
