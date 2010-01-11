package org.genedb.db.domain.hibernateImpls;

import org.genedb.db.domain.misc.Message;
import org.genedb.db.domain.services.MessageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageServiceImpl implements MessageService {

    private Map<String, List<Message>> messageMap = new HashMap<String, List<Message>>();

    public synchronized void addNotification(String clientName, String string, String string2) {
        if (!messageMap.containsKey(clientName)) {
            messageMap.put(clientName, new ArrayList<Message>());
        }
        List<Message> messages = messageMap.get(clientName);
        messages.add(new Message());
    }

    public synchronized List<Message> checkMessages(String clientName) {
        return messageMap.get(clientName);
    }

}
