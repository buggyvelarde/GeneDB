/*
 * Copyright (c) 2006-2007 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.web.mvc.controller;

import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * This is a very simple extension to the Spring SimpleFormController which
 * considers a form submission to have happened if the request is a POST, or if
 * there are any parameters supplied
 *
 * @author Adrian Tivey (art)
 */
public abstract class PostOrGetFormController extends SimpleFormController {

    /**
     * Behaves as superclass except a GET request with parameters is also
     * considered a form submission. This makes hyperlinking to the controller
     * easier
     *
     * @see org.springframework.web.servlet.mvc.AbstractFormController#isFormSubmission(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        boolean sup = super.isFormSubmission(request);
        return sup ? true : (request.getParameterMap().size() > 0);
    }

}
