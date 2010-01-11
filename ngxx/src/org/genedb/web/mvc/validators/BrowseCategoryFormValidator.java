package org.genedb.web.mvc.validators;

import org.genedb.web.mvc.controller.BrowseCategoryController.BrowseCategoryBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class BrowseCategoryFormValidator implements Validator {

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return true;
    }

    public void validate(Object object, Errors errors) {
        BrowseCategoryBean bean = (BrowseCategoryBean) object;

        if(bean.getCategory() == null && bean.getTaxons() == null) {
            errors.reject("no.params");
            return;
        }

        if(bean.getCategory() != null && bean.getTaxons() == null) {
            errors.reject("show.form");
            return;
        }

    }

}
