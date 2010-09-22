package org.genedb.web.mvc.controller.download;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.PolypeptideRegion;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.web.mvc.model.DbXRefDTO;
import org.genedb.web.mvc.model.FeatureCvTermDTO;
import org.genedb.web.mvc.model.TranscriptDTO;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

public class TranscriptDTOAdaptor {

	private static final Logger logger = Logger.getLogger(TranscriptDTOAdaptor.class);

	private TranscriptDTO dto;

	private String fieldDelim;

	private Map<String, List<String>> mapping;

	public TranscriptDTOAdaptor(TranscriptDTO dto, String fieldDelim) {
		this.dto = dto;
		this.fieldDelim = fieldDelim;
		mapping = dto.getSynonymsByTypes();
	}


	public String getEc() {
		List<String> xrefs = Lists.newArrayList();
		for (DbXRefDTO dbxref : dto.getDbXRefDTOs()) {
			if (dbxref.getDbName().equals("EC")) {
				xrefs.add(dbxref.getDbName() + ":" + dbxref.getAccession());
			}
		}
		return StringUtils.collectionToDelimitedString(xrefs, fieldDelim);
	}

	public String getGO() {
		List<FeatureCvTermDTO> goFeatureCvTerms = Lists.newArrayList();
		goFeatureCvTerms.addAll(dto.getGoBiologicalProcesses());
		goFeatureCvTerms.addAll(dto.getGoCellularComponents());
		goFeatureCvTerms.addAll(dto.getGoMolecularFunctions());
		List<String> ids = Lists.newArrayList();
		for (FeatureCvTermDTO fctDTO : goFeatureCvTerms) {
			ids.add("GO:" + fctDTO.getTypeAccession());
		}
		return StringUtils.collectionToDelimitedString(goFeatureCvTerms, fieldDelim);
	}

	public String getInterpro() {
		List<String> accessions = Lists.newArrayList();
		List<PolypeptideRegionGroup> regions = dto.getDomainInformation();
		for (PolypeptideRegionGroup region : regions) {
			accessions.add(region.getUniqueName());
			for (PolypeptideRegion polypeptideRegion : region.getSubfeatures()) {
				accessions.add(polypeptideRegion.getUniqueName());
			}
		}
		return StringUtils.collectionToDelimitedString(accessions, fieldDelim);
	}

	public String getPfam() {
		List<String> accessions2 = Lists.newArrayList();
		List<PolypeptideRegionGroup> regions2 = dto.getDomainInformation();
		for (PolypeptideRegionGroup region : regions2) {
			accessions2.add(region.getUniqueName());
			for (PolypeptideRegion polypeptideRegion : region.getSubfeatures()) {
				accessions2.add(polypeptideRegion.getUniqueName());
			}

		}
		return StringUtils.collectionToDelimitedString(accessions2, fieldDelim);
	}

	public String getPrevIds() {
		return StringUtils.collectionToDelimitedString(mapping.get("Previous systematic id"), fieldDelim);
	}

	public String getSynonyms() {
		return StringUtils.collectionToDelimitedString(mapping.get("Synonym"), fieldDelim);
	}

	public String getOrganism() {
		return dto.getOrganismCommonName();
	}

	public String getGpiAnchor() {
		return dto.getAlgorithmData().get("DGPI").toString();
	}

	public String getType() {
		return dto.getTypeDescription();
	}

	public String getIsoelectricPoint() {
		return dto.getPolypeptideProperties().getIsoelectricPoint();
	}

	public String getLocation() {
		return dto.getLocation() ;
	}

	public String getMolWeight() {
		return dto.getPolypeptideProperties().getMass() + fieldDelim;
	}

	public String getNumTM() {
		return (String) dto.getAlgorithmData().get("TMHMM") + fieldDelim;
	}

	public String getPrimaryName() {
		return dto.getGeneName() + fieldDelim;
	}

	public String getProduct() {
		List<String> list = Lists.newArrayList();
		for (FeatureCvTermDTO fct : dto.getProducts()) {
			list.add(fct.getTypeName());
		}
		return StringUtils.collectionToDelimitedString(list, fieldDelim);
	}

	public String isSigP() {
		return dto.getAlgorithmData().containsKey("SignalP") ? "true" : "";
	}

	public String getContig() {
		return dto.getTopLevelFeatureDisplayName() + fieldDelim;
	}

	public String getId() {
		return dto.getUniqueName() + fieldDelim;
	}
}


