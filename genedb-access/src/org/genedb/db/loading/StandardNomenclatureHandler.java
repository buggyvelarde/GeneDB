package org.genedb.db.loading;

import static org.genedb.db.loading.EmblQualifiers.QUAL_OBSOLETE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PREV_SYS_ID;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIMARY;
import static org.genedb.db.loading.EmblQualifiers.QUAL_RESERVED;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYNONYM;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;
import static org.genedb.db.loading.EmblQualifiers.QUAL_TEMP_SYS_ID;

import org.biojava.bio.Annotation;

import java.util.List;
import java.util.Map;

public class StandardNomenclatureHandler implements NomenclatureHandler {

    public Names findNames(Annotation an) {
        Names names = new Names();
        String sysId = null;
        List<String> l1 = MiningUtils.getProperties(QUAL_SYS_ID, an);
        if ( l1.size() != 0) {
            if ( l1.size() > 1 ) {
                throw new RuntimeException("StandardNomenclatureHandler: CDS has more than 1 systematic id: "+l1.get(0));
            }
            sysId = l1.get(0);
            names.setSystematicIdAndTemp(sysId, false);
        }

        String tmpId = null;
        l1 = MiningUtils.getProperties(QUAL_TEMP_SYS_ID, an);
        if ( l1.size() != 0) {
            if ( l1.size() > 1 ) {
                throw new RuntimeException("StandardNomenclatureHandler: CDS has more than 1 temporary systematic id: "+l1.get(0));
            }
            tmpId = l1.get(0);
            if ( sysId != null) {
                throw new RuntimeException("CDS has both a systematic id '"+sysId+"' and a temporary systematic id '"+tmpId+"'");
            }
            names.setSystematicIdAndTemp(tmpId, true);
        }


        if (names.getSystematicId() == null) {
            throw new RuntimeException("StandardNomenclatureHandler: No systematic id set");
        }


        l1 = MiningUtils.getProperties(QUAL_SYNONYM, an);
        if ( l1 != null) {
            names.setSynonyms(l1);
        }

        l1 = MiningUtils.getProperties(QUAL_OBSOLETE, an);
        if ( l1 != null) {
            names.setObsolete(l1);
        }

        l1 = MiningUtils.getProperties(QUAL_PREV_SYS_ID, an);
        if ( l1 != null) {
            names.setPreviousSystematicIds(l1);
        }

        l1 = MiningUtils.getProperties(QUAL_PRIMARY, an);
        if (l1.size() != 0) {
            if ( l1.size() > 1 ) {
                throw new RuntimeException("GenericCDSParser: gene has more than 1 primary name: "+names.getSystematicId());
            }
            names.setPrimary(l1.get(0));
        }

        l1 = MiningUtils.getProperties(QUAL_RESERVED, an);
        if ( l1 != null) {
            names.setReserved(l1);
        }

        return names;
    }

    /**
     * NOP as StandardNomenclatureHandler doesn't accept options
     * @see org.genedb.db.loading.NomenclatureHandler#setOptions(java.util.Map)
     */
    public void setOptions(Map<String, String> nomenclatureOptions) {
        // Deliberately empty
    }

}
