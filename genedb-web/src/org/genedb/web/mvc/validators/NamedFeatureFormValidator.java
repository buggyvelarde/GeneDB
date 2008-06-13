package org.genedb.web.mvc.validators;

import org.genedb.web.mvc.controller.NamedFeatureController.NameLookupBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class NamedFeatureFormValidator implements Validator{

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(Class clazz) {
        return true;
    }

    @Override
    public void validate(Object object, Errors errors) {
        NameLookupBean bean = (NameLookupBean) object;
        
        if(bean.getName() == null && bean.getOrganism() == null) {
            errors.reject("no.params");
            return;
        }
        
        if(bean.getName() == null && bean.getOrganism() != null) {
            errors.reject("show.form");
            return;
        }

    }

}
