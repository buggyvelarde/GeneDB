package org.genedb.jogra.services;


import java.util.List;

public interface MessageService {

    void addNotification(String clientName, String string, String string2);

    List<Message> checkMessages(String clientName);

}
