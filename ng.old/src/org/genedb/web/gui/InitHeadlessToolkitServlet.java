package org.genedb.web.gui;

import java.awt.Toolkit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitHeadlessToolkitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        System.setProperty("java.awt.headless", "true");
        Toolkit.getDefaultToolkit();
        super.init();
    }

}
