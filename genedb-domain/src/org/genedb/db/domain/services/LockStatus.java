package org.genedb.db.domain.services;

import java.io.Serializable;

public class LockStatus implements Serializable {

    public static final LockStatus UNLOCKED = new LockStatus(false);

    private boolean locked;

    public LockStatus(boolean locked) {
        super();
        this.locked = locked;
    }

}
