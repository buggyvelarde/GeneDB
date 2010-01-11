package org.genedb.web.tags.misc;

import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;
import org.springframework.web.servlet.tags.Param;
import org.springframework.web.servlet.tags.ParamAware;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.TagUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

public class UrlTag extends HtmlEscapingAwareTag implements ParamAware {

    private static final String URL_TEMPLATE_DELIMITER_PREFIX = "{";

    private static final String URL_TEMPLATE_DELIMITER_SUFFIX = "}";

    private static final String URL_TYPE_ABSOLUTE = "://";


    private List<Param> params;

    private Set<String> templateParams;

    private UrlType type;

    private String value;

    private String context;

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private boolean javaScriptEscape = false;

    private boolean includeSessionInUrl = false;


    /**
     * Sets the value of the URL
     */
    public void setValue(String value) {
        if (value.contains(URL_TYPE_ABSOLUTE)) {
            this.type = UrlType.ABSOLUTE;
            this.value = value;
        }
        else if (value.startsWith("/")) {
            this.type = UrlType.CONTEXT_RELATIVE;
            this.value = value;
        }
        else {
            this.type = UrlType.RELATIVE;
            this.value = value;
        }
    }

    /**
     * Set the context path for the URL. Defaults to the current context
     */
    public void setContext(String context) {
        if (context.startsWith("/")) {
            this.context = context;
        }
        else {
            this.context = "/" + context;
        }
    }

    /**
     * Set the variable name to expose the URL under. Defaults to rendering the
     * URL to the current JspWriter
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Set the scope to export the URL variable to. This attribute has no
     * meaning unless var is also defined.
     */
    public void setScope(String scope) {
        this.scope = TagUtils.getScope(scope);
    }

    /**
     * Set JavaScript escaping for this tag, as boolean value.
     * Default is "false".
     */
    public void setJavaScriptEscape(String javaScriptEscape) throws JspException {
        this.javaScriptEscape =
                ExpressionEvaluationUtils.evaluateBoolean("javaScriptEscape", javaScriptEscape, pageContext);
    }

    public void addParam(Param param) {
        this.params.add(param);
    }


    @Override
    public int doStartTagInternal() throws JspException {
        this.params = new LinkedList<Param>();
        this.templateParams = new HashSet<String>();
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        String url = createUrl();
        if (this.var == null) {
            // print the url to the writer
            try {
                pageContext.getOut().print(url);
            }
            catch (IOException e) {
                throw new JspException(e);
            }
        }
        else {
            // store the url as a variable
            pageContext.setAttribute(var, url, scope);
        }
        return EVAL_PAGE;
    }


    /**
     * Build the URL for the tag from the tag attributes and parameters.
     * @return the URL value as a String
     * @throws JspException
     */
    private String createUrl() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        StringBuilder url = new StringBuilder();
        if (this.type == UrlType.CONTEXT_RELATIVE) {
            // add application context to url
            if (this.context == null) {
                url.append(request.getContextPath());
            }
            else {
                url.append(this.context);
            }
        }
        if (this.type != UrlType.RELATIVE && this.type != UrlType.ABSOLUTE && !this.value.startsWith("/")) {
            url.append("/");
        }
        url.append(replaceUriTemplateParams(this.value, this.params, this.templateParams));
        url.append(createQueryString(this.params, this.templateParams, (url.indexOf("?") == -1)));

        String urlStr = url.toString();
        if (this.type != UrlType.ABSOLUTE) {
            // Add the session identifier if needed, and desired
            // (Do not embed the session identifier in a remote link!)
            if (includeSessionInUrl) {
                urlStr = response.encodeURL(urlStr);
            }
        }

        // HTML and/or JavaScript escape, if demanded.
        urlStr = isHtmlEscape() ? HtmlUtils.htmlEscape(urlStr) : urlStr;
        urlStr = this.javaScriptEscape ? JavaScriptUtils.javaScriptEscape(urlStr) : urlStr;

        return urlStr;
    }

    /**
     * Build the query string from available parameters that have not already
     * been applied as template params.
     * <p>The names and values of parameters are URL encoded.
     * @param params the parameters to build the query string from
     * @param usedParams set of parameter names that have been applied as
     * template params
     * @param includeQueryStringDelimiter true if the query string should start
     * with a '?' instead of '&'
     * @return the query string
     * @throws JspException
     */
    protected String createQueryString(
            List<Param> params, Set<String> usedParams, boolean includeQueryStringDelimiter)
            throws JspException {

        StringBuilder qs = new StringBuilder();
        for (Param param : params) {
            if (!usedParams.contains(param.getName()) && param.getName() != null && !"".equals(param.getName())) {
                if (includeQueryStringDelimiter && qs.length() == 0) {
                    qs.append("?");
                }
                else {
                    qs.append("&");
                }
                qs.append(urlEncode(param.getName()));
                if (param.getValue() != null) {
                    qs.append("=");
                    qs.append(urlEncode(param.getValue()));
                }
            }
        }
        return qs.toString();
    }

    /**
     * Replace template markers in the URL matching available parameters. The
     * name of matched parameters are added to the used parameters set.
     * <p>Parameter values are URL encoded.
     * @param uri the URL with template parameters to replace
     * @param params parameters used to replace template markers
     * @param usedParams set of template parameter names that have been replaced
     * @return the URL with template parameters replaced
     * @throws JspException
     */
    protected String replaceUriTemplateParams(String uri, List<Param> params, Set<String> usedParams)
            throws JspException {

        for (Param param : params) {
            String template = URL_TEMPLATE_DELIMITER_PREFIX + param.getName() + URL_TEMPLATE_DELIMITER_SUFFIX;
            if (uri.contains(template)) {
                usedParams.add(param.getName());
                uri = uri.replace(template, urlEncode(param.getValue()));
            }
        }
        return uri;
    }

    /**
     * URL-encode the providedSstring using the character encoding for the response.
     * @param value the value to encode
     * @return the URL encoded value
     * @throws JspException if the character encoding is invalid
     */
    protected String urlEncode(String value) throws JspException {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, pageContext.getResponse().getCharacterEncoding());
        }
        catch (UnsupportedEncodingException ex) {
            throw new JspException(ex);
        }
    }


    /**
     * Internal enum that classifies URLs by type.
     */
    private enum UrlType {
        CONTEXT_RELATIVE, RELATIVE, ABSOLUTE
    }




}
