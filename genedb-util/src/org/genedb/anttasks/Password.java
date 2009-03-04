package org.genedb.anttasks;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Console;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A simple ant task that requests a password from the user.
 *
 * If a console is available, the password will be requested on the console.
 * Otherwise a dialog box will pop up.
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

        String password = readPassword(prompt);

        if (password != null) {
            String passwordString = new String(password);
            getProject().setProperty(name, passwordString);
        }
    }

    private String readPassword(String prompt) {
        Console console = System.console();
        if (console == null) {
            return readPasswordFromDialog(prompt);
        }
        return readPasswordFromConsole(console, prompt);
    }

    private String readPasswordFromConsole(Console console, String prompt) {
        char[] password = console.readPassword("%s: ", prompt);
        if (password == null)
            return null;
        return new String(password);
    }

    private Dialog dialog;
    private String dialogPassword = "";
    private boolean dialogCancelled;

    private String readPasswordFromDialog(String prompt) {
        System.out.println("Prompting user for password");
        dialogCancelled = false;

        Frame hiddenFrame = new Frame(getClass().getName());
        dialog = new Dialog(hiddenFrame, "Password", true);
        dialog.setLocation(100, 100);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                dialogCancelled = true;
                windowEvent.getWindow().dispose();
            }
        });

        dialog.add(new Label(prompt),    BorderLayout.NORTH);
        dialog.add(passwordEntryField(), BorderLayout.CENTER);
        dialog.add(buttonPanel(),        BorderLayout.SOUTH);
        dialog.pack();

        dialog.setVisible(true);
        hiddenFrame.dispose();

        if (dialogCancelled)
            throw new BuildException("Password entry cancelled.");

        return dialogPassword;
    }

    private Component passwordEntryField() {
        final TextField textField = new TextField(16);
        textField.setEchoChar('*');

        textField.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent e) {
                if (e.getID() == TextEvent.TEXT_VALUE_CHANGED)
                    dialogPassword = textField.getText();
            }
        });

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.dispose();
            }
        });

        return textField;
    }

    private Component buttonPanel() {
        Panel buttonPanel = new Panel();

        Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialogCancelled = true;
                dialog.dispose();
            }
        });

        Button okButton = new Button("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.dispose();
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }
}
