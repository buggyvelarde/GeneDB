package org.genedb.web.mvc.controller.download;

import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.PolypeptideRegion;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.DbXRefDTO;
import org.genedb.web.mvc.model.FeatureCvTermDTO;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class DtoDataFetcher implements DataFetcher<Integer> {

    private Logger logger = Logger.getLogger(DtoDataFetcher.class);

    private BerkeleyMapFactory bmf;

    public TroubleTrackingIterator<DataRow> iterator(List<Integer> ids, String fieldDelim) {
        return new DtoDataRowIterator(ids, bmf, fieldDelim); // FIXME convert to transcript ids
    }

    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

}


class DtoDataRowIterator implements TroubleTrackingIterator<DataRow> {

    private Logger logger = Logger.getLogger(DtoDataRowIterator.class);

    private BerkeleyMapFactory bmf;

    private Iterator<Integer> it;

    private String fieldDelim;

    private TranscriptDTO nextDTO;

    private List<Integer> problems = Lists.newArrayList();

    public DtoDataRowIterator(List<Integer> ids, BerkeleyMapFactory bmf, String fieldDelim) {
        this.it = ids.iterator();
        this.bmf = bmf;
        this.fieldDelim = fieldDelim;
    }

    @Override
    public boolean hasNext() {
        while (it.hasNext()) {
            Integer next = it.next();
            nextDTO = bmf.getDtoMap().get(next);
            if (nextDTO != null) {
                return true;
            } else {
                problems.add(next);
            }
        }
        return false;
    }

    @Override
    public DataRow next() {
        //Integer id = it.next();
        // Need to convert name to featureId
        //TranscriptDTO dto = bmf.getDtoMap().get(id);
        return new DtoDataRow(nextDTO, fieldDelim);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public List<Integer> getProblems() {
        return problems;
    }

}


class DtoDataRow implements DataRow {

    private static final Logger logger = Logger.getLogger(DtoDataRow.class);

    private TranscriptDTO dto;

    private String fieldDelim;

    public DtoDataRow(TranscriptDTO dto, String fieldDelim) {
        this.dto = dto;
        this.fieldDelim = fieldDelim;
    }

    @Override
    public String getValue(OutputOption oo) {

        Map<String, List<String>> mapping = dto.getSynonymsByTypes();
        logger.error("The mapping is '" + mapping + "'");

        // Get the data from the DTO
        switch (oo) {
            case EC_NUMBERS:
                List<String> xrefs = Lists.newArrayList();
                for (DbXRefDTO dbxref : dto.getDbXRefDTOs()) {
                    xrefs.add(dbxref.getDbName() + ":" + dbxref.getAccession());
                }
                return StringUtils.collectionToDelimitedString(xrefs, fieldDelim);

            case GO_IDS:
                List<FeatureCvTermDTO> goFeatureCvTerms = Lists.newArrayList();
                goFeatureCvTerms.addAll(dto.getGoBiologicalProcesses());
                goFeatureCvTerms.addAll(dto.getGoCellularComponents());
                goFeatureCvTerms.addAll(dto.getGoMolecularFunctions());
                List<String> ids = Lists.newArrayList();
                for (FeatureCvTermDTO fctDTO : goFeatureCvTerms) {
                    ids.add("GO:" + fctDTO.getTypeAccession());
                }
                return StringUtils.collectionToDelimitedString(goFeatureCvTerms, fieldDelim);

            case INTERPRO_IDS:
                List<String> accessions = Lists.newArrayList();
                List<PolypeptideRegionGroup> regions = dto.getDomainInformation();
                for (PolypeptideRegionGroup region : regions) {
                    accessions.add(region.getUniqueName());
                    for (PolypeptideRegion polypeptideRegion : region.getSubfeatures()) {
                        accessions.add(polypeptideRegion.getUniqueName());
                    }

                }
                return StringUtils.collectionToDelimitedString(accessions, fieldDelim);

            case PFAM_IDS:
                List<String> accessions2 = Lists.newArrayList();
                List<PolypeptideRegionGroup> regions2 = dto.getDomainInformation();
                for (PolypeptideRegionGroup region : regions2) {
                    accessions2.add(region.getUniqueName());
                    for (PolypeptideRegion polypeptideRegion : region.getSubfeatures()) {
                        accessions2.add(polypeptideRegion.getUniqueName());
                    }

                }
                return StringUtils.collectionToDelimitedString(accessions2, fieldDelim);

            case PREV_SYS_ID:
                return StringUtils.collectionToDelimitedString(mapping.get("Previous systematic id"), fieldDelim);

            case SYNONYMS:
                return StringUtils.collectionToDelimitedString(mapping.get("Synonym"), fieldDelim);

            case ORGANISM:
                return dto.getOrganismCommonName();

            case GPI_ANCHOR:
                return dto.getAlgorithmData().get("DGPI").toString();

            case GENE_TYPE:
                return dto.getTypeDescription();

            case ISOELECTRIC_POINT:
                return dto.getPolypeptideProperties().getIsoelectricPoint();

            case LOCATION:
                return dto.getLocation();

            case MOL_WEIGHT:
                return dto.getPolypeptideProperties().getMass();

            case NUM_TM_DOMAINS:
                return (String) dto.getAlgorithmData().get("TMHMM");

            case PRIMARY_NAME:
                return dto.getGeneName();

            case PRODUCT:
                List<String> list = Lists.newArrayList();
                for (FeatureCvTermDTO fct : dto.getProducts()) {
                    list.add(fct.getTypeName());
                }
                return StringUtils.collectionToDelimitedString(list, fieldDelim);

            case SIG_P:
                return dto.getAlgorithmData().containsKey("SignalP") ? "true" : "";

            case CHROMOSOME:
                return dto.getTopLevelFeatureDisplayName();

            case SYS_ID:
                return dto.getUniqueName();
        }
        return "";
    }


}
