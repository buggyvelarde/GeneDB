package org.genedb.db.domain.hibernateImpls;

import org.genedb.db.domain.services.LockAndNotificationService;
import org.genedb.db.domain.services.LockStatus;
import org.genedb.db.domain.services.MessageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LockAndNotificationServiceImpl implements LockAndNotificationService {

    private MessageService messageService;
    private Set<String> locks = new HashSet<String>();
    private Map<String, ArrayList<String>> interested = new HashMap<String, ArrayList<String>>();

    public LockStatus lockGene(String systematicId) {
        if (!(locks.contains(systematicId))) {
            locks.add(systematicId);
            return new LockStatus(true);
        }
        return null;
    }

    public void unlockGene(String systematicId) {
        locks.remove(systematicId);
    }

    public void notifyGene(String uniqueName, String string) {
        if (interested.containsKey(uniqueName)) {
            List<String> clients = new ArrayList<String>();
            for (String clientName : clients) {
                messageService.addNotification(clientName, "Gene Updated", string);
            }
        }
    }

}
