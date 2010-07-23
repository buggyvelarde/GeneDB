package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;

/**
 * Controller for uploading a list of ids, either from a file or a textbox.
 * After validating them it stores them in the user's history
 */
@Controller
@RequestMapping("/IdList")
public class IdListController {

    private final Logger logger = Logger.getLogger(IdListController.class);

    private HistoryManagerFactory hmFactory;

    private SequenceDao sequenceDao;

    @RequestMapping(method=RequestMethod.GET)
    public String prepareForm() {
        return "analysis/idList";
    }

    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView processImageUpload(
            HttpSession session,
            @RequestParam("idList") String idList,
            //@RequestParam("oldIds") boolean useOldIds,
            //@RequestParam("historyItemName") String historyItemName,
            @RequestParam("ids") MultipartFile mf) {

        List<String> ids = Lists.newArrayList();

        if (!mf.isEmpty()) {
            try {
                addIds(ids, new String(mf.getBytes()));
            }
            catch (IOException exp) {
                exp.printStackTrace();
                return new ModelAndView("redirect:internalError");
            }
        }

        if (StringUtils.hasText(idList)) {
            addIds(ids, idList);
        }

        List<String> okIds = Lists.newArrayList();
        List<String> oldIds = Lists.newArrayList();
        List<String> ambiguousIds = Lists.newArrayList();
        List<String> badIds = Lists.newArrayList();
        validateIds(ids, okIds, oldIds, ambiguousIds, badIds);

        HistoryManager hm = hmFactory.getHistoryManager(session);

//        if (useOldIds) {
//            okIds.addAll(oldIds);
//        }

        String historyName = "Uploaded";
//        if (StringUtils.hasText(historyItemName)) {
//            historyName = historyItemName;
//        }
        for (String string : okIds) {
            logger.error("The list of ok ids includes '"+string+"'");
        }

        if (ambiguousIds.size() > 0 || badIds.size() > 0) {
        	// A problem, some of the submitted ids are wrong
        	ModelAndView mav = new ModelAndView();
        	mav.setViewName("analysis/idList");

        	StringBuilder message = new StringBuilder();

        	if (badIds.size() > 0) {
        		message.append("Unrecognized ids\n");
                for (String badId : badIds) {
                	message.append(badId);
                	message.append('\n');
                }
                message.append("\n\n\n");
        	}

        	if (ambiguousIds.size() > 0) {
        		message.append("Ambiguous ids\n");
                for (String ambiguousId : ambiguousIds) {
                	message.append(ambiguousId);
                	message.append('\n');
                }
                message.append("\n\n\n");
        	}

            for (String okId : okIds) {
            	message.append(okId);
            	message.append('\n');
            }
        	mav.addObject("idList", message.toString());
        	return mav;
        }

        hm.addHistoryItem(historyName, HistoryType.MANUAL, okIds);

        return new ModelAndView("redirect:/History");
    }


    private void validateIds(List<String> ids, List<String> okIds,
            List<String> oldIds, List<String> ambiguousIds, List<String> badIds) {

        for (String id : ids) {
            String realId;
            if ((realId = validatePrimaryId(id))!= null) {
                okIds.add(realId);
                logger.error("Found '"+realId+"'");
                continue;
            }
            if (validateSecondaryId(id, okIds, oldIds, ambiguousIds)) {
                continue;
            }
            logger.error("Missed '"+id+"'");
            badIds.add(id);
        }
    }


    private boolean validateSecondaryId(String id, List<String> okIds, List<String> oldIds, List<String> ambiguousIds) {

    	// First check previous systematic ids
        List<Feature> lf = sequenceDao.getFeaturesByPreviousSystematicId(id);
        if (lf.size() > 1) {
            ambiguousIds.add(id);
            return true;
        }
        if (lf.size() == 1) {
            okIds.add(lf.get(0).getUniqueName());
            return true;
        }

    	// If no match, try all current names
        lf = sequenceDao.getFeaturesByAnyCurrentName(id);
        if (lf.size() == 0) {
            return false;
        }
        if (lf.size() > 1) {
            ambiguousIds.add(id);
            return true;
        }
        okIds.add(id);
        return true;
    }


    private String validatePrimaryId(String id) {
        Feature f = sequenceDao.getFeatureByUniqueName(id, Feature.class);
        if (f == null) {
            return null;
        }
        if (f instanceof org.gmod.schema.feature.Gene) {
            Gene g = (org.gmod.schema.feature.Gene) f;
            Transcript t = g.getTranscripts().iterator().next();
            return t.getUniqueName();
        }
        return f.getUniqueName();
    }


    private void addIds(List<String> ids, String idList) {
        String[] lines = idList.split("[\\r]?\\n");
        ids.addAll(Arrays.asList(lines));
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setHmFactory(HistoryManagerFactory hmFactory) {
        this.hmFactory = hmFactory;
    }

}