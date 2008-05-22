package org.genedb.web.mvc.controller;


import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping;

public class CaseSensitiveControllerClassNameHandlerMapping extends ControllerClassNameHandlerMapping {

        /**
         * Implementation of {@link HandlerMapping} that follows a simple convention for generating
         * URL path mappings from the class names of registered {@link Controller} beans.
         *
         * <p>For simple {@link Controller} implementations (those that handle a single request type),
         * the convention is to take the {@link ClassUtils#getShortName short name} of the <code>Class</code>,
         * remove the 'Controller' suffix if it exists and return the remaining text as
         * the mapping, with a leading <code>/</code>. For example:
         * <ul>
         *  <li><code>WelcomeController</code> -> <code>/welcome</code></li>
         *  <li><code>HomeController</code> -> <code>/home</code></li>
         * </ul>
         *
         * <p>For {@link MultiActionController MultiActionControllers} then a similar mapping is registed
         * except that all sub-paths are registed using the trailing wildcard pattern <code>/*</code>.
         * For example:
         * <ul>
         *  <li><code>AdminController</code> -> <code>/welcome/*</code></li>
         *  <li><code>CatalogController</code> -> <code>/catalog/*</code></li>
         * </ul>
         *
         * <p>For {@link MultiActionController} it is often useful to use
         * this mapping strategy in conjunction with the
         * {@link org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver}.
         *
         * @author Rob Harrop
         * @since 2.0
         * @see org.springframework.web.servlet.mvc.Controller
         * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController
         */

        /**
         * Common suffix at the end of {@link Controller} implementation classes.
         * Removed when generating the URL path.
         */
        private static final String CONTROLLER_SUFFIX = "Controller";


        /**
         *  (non-Javadoc)
         * @see org.springframework.web.servlet.mvc.ControllerClassNameHandlerMapping#generatePathMapping(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected String[] generatePathMappings(Class controllerClass) {
                StringBuffer pathMapping = new StringBuffer("/");

                String className = ClassUtils.getShortName(controllerClass.getName());
                String path = className.endsWith(CONTROLLER_SUFFIX) 
                                ? className.substring(0, className.indexOf(CONTROLLER_SUFFIX)) 
                                : className;
                pathMapping.append(path);

                if (MultiActionController.class.isAssignableFrom(controllerClass)) {
                        pathMapping.append("/*");
                }

                System.err.println("ART:: "+pathMapping.toString());
                return new String[] { pathMapping.toString() };
        }

}
