package org.genedb.web.mvc.controller.download;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class OutputFormatterUtils {

	public static String prepareExpression(List<OutputOption> outputOptions,
			String rowStart, String rowEnd, String entryStart, String entryEnd) {
		StringBuilder ret = new StringBuilder();
		ret.append(rowStart);
		for (OutputOption outputOption : outputOptions) {
			ret.append(entryStart);
			switch(outputOption) {
			case CHROMOSOME:
				ret.append("${contig}");
				break;
			case EC_NUMBERS:
				ret.append("${ec}");
				break;
			case GENE_TYPE:
				ret.append("${type}");
				break;
			case GO_IDS:
				ret.append("${go}");
				break;
			case GPI_ANCHOR:
				ret.append("${gpiAnchor}");
				break;
			case INTERPRO_IDS:
				ret.append("${interpro}");
				break;
			case ISOELECTRIC_POINT:
				ret.append("${isoelectricPoint}");
				break;
			case LOCATION:
				ret.append("${location}");
				break;
			case MOL_WEIGHT:
				ret.append("${molWeight}");
				break;
			case NUM_TM_DOMAINS:
				ret.append("${numTM}");
				break;
			case ORGANISM:
				ret.append("${organism}");
				break;
			case PFAM_IDS:
				ret.append("${pfam}");
				break;
			case PREV_SYS_ID:
				ret.append("${prevIds}");
				break;
			case PRIMARY_NAME:
				ret.append("${primaryName}");
				break;
			case PRODUCT:
				ret.append("${product}");
				break;
			case SIG_P:
				ret.append("${sigP}");
				break;
			case SYNONYMS:
				ret.append("${synonyms}");
				break;
			case SYS_ID:
				ret.append("${id}");
				break;
			}
			ret.append(entryEnd);
		}
		ret.append(rowEnd);
		ret.append('\n');
		return ret.toString();
	}

}
