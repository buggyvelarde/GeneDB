package org.genedb.anttasks;

import java.io.Console;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A simple ant task that requests a password from the user, on the console.
 *
 * If no console is available, it will simply fail. It could easily be extended
 * to pop up a dialog box in that case, perhaps using code from <code>com.jera.anttasks.Query</code>.
 * 
 * @author rh11
 */
public class Password extends Task {
    private String name;
    private String prompt = "Password";

    public void setName(String name) {
        this.name = name;
    }

    public void setPrompt(String message) {
        this.prompt = message;
    }
    
    @Override
    public void execute() throws BuildException {
        if (name == null)
                throw new BuildException("<password> task requires 'name=' attribute.");

        Console console = System.console();
        if (console == null)
                throw new BuildException("Failed to get console");

        char[] password = console.readPassword("%s: ", prompt);

        if (password != null) {
                String passwordString = new String(password);
                getProject().setProperty(name, passwordString);
        }
    }
}
