package org.genedb.jogra.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionZoneDao {
    
    private Map<String, ExpressionZone> map;
    private Map<String, ExpressionZone> labelMap;
    
    public ExpressionZoneDao() {
        initMap();
    }
    
    private void initMap() {
        List<ExpressionZone> zones = new ArrayList<ExpressionZone>();
        zones.add(new ExpressionZone("A-E", "Nervous System"));
        zones.add(new ExpressionZone("A", "CNS a"));
        zones.add(new ExpressionZone("A1", "Telencephalon"));
        zones.add(new ExpressionZone("A2", "Diencephalon"));
        zones.add(new ExpressionZone("A3", "Epiphysis"));
        zones.add(new ExpressionZone("B", "CNS b"));
        zones.add(new ExpressionZone("B1", "Tectum"));
        zones.add(new ExpressionZone("B2", "Tegmentum"));
        zones.add(new ExpressionZone("B3", "Midbrain-hindbrain Boundary"));
        zones.add(new ExpressionZone("C", "CNS c"));
        zones.add(new ExpressionZone("C1", "Cerebellum"));
        zones.add(new ExpressionZone("C2", "Rhombencaphalon"));
        zones.add(new ExpressionZone("D", "PNS"));
        zones.add(new ExpressionZone("E", "Other neuronal cells"));
        zones.add(new ExpressionZone("F-H", "Sensory Organs"));
        zones.add(new ExpressionZone("F", "Sensory Organs - eye"));
        zones.add(new ExpressionZone("F1", "Retina"));
        zones.add(new ExpressionZone("F2", "Lens"));
        zones.add(new ExpressionZone("G", "Sensory Organs - ear"));
        zones.add(new ExpressionZone("H", "Lateral Line"));       
        zones.add(new ExpressionZone("I", "Notochord"));
        zones.add(new ExpressionZone("J", "Muscle"));
        zones.add(new ExpressionZone("K", "Blood"));
        zones.add(new ExpressionZone("L", "Heart/Pericardium"));
        zones.add(new ExpressionZone("M-P", "Skin/EVL/Fins"));
        zones.add(new ExpressionZone("M", "Epidermis/EVL"));
        zones.add(new ExpressionZone("O-P", "Fins"));
        zones.add(new ExpressionZone("O", "Dorsal/medial/tail fin"));
        zones.add(new ExpressionZone("P", "Pectoral fin"));
        zones.add(new ExpressionZone("Q-V", "Other/Unspecified"));
        zones.add(new ExpressionZone("Q", "Tail bud"));
        zones.add(new ExpressionZone("R", "Yolk"));
        zones.add(new ExpressionZone("S", "Bet. brain and yolk"));
        zones.add(new ExpressionZone("T", "Bet. spinal cord and yolk/yolk extension"));
        zones.add(new ExpressionZone("U", "Caudal/ventral to yolk extension"));
        zones.add(new ExpressionZone("V", "Other"));
        
        
        
        map = new HashMap<String, ExpressionZone>();
        labelMap = new HashMap<String, ExpressionZone>();
        for (ExpressionZone ez : zones) {
            map.put(ez.getDescription(), ez);
            labelMap.put(ez.getLabel(), ez);
        }

    }

    public Collection<ExpressionZone> retrieveAllAnatomy() {
        return map.values();
    }
    
    public ExpressionZone retrieveAnatomyByName(String name) {
        return map.get(name);
    }
    
    public List<ExpressionZone> retrieveAnatomyByName(String[] anatomyNames) {
        List<ExpressionZone> ret = new ArrayList<ExpressionZone>();
        for (String anatomyName : anatomyNames) {
            ret.add(map.get(anatomyName));
        }
        return ret;
    }

    public ExpressionZone retrieveExpressionZoneByLabel(String category) {
        return labelMap.get(category);
    }

}
