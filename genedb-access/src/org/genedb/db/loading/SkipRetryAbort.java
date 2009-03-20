package org.genedb.db.loading;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Console;

/**
 * Prompt the user whether to skip or retry a file that failed to load,
 * or to abort the entire run. In many cases, loading errors result from
 * minor infelicities in the input files that are easy to correct by hand.
 * The user can correct the problem using a text editor, and then click
 * 'Retry'. On the other hand, loading errors can also (God forbid) be the
 * result of bugs in the loading code, in which case the only reasonable
 * response is to abort.
 * <p>
 * If a console is available, the prompt is textual. Otherwise, a dialog
 * box is used. In practice it is rare that a console is available, since
 * LoadEmbl is usually invoked using an ant target.
 *
 * @author rh11
 *
 */
class SkipRetryAbort {
    public enum Response { SKIP, RETRY, ABORT };

    /**
     * On a parsing error, prompt the user whether to skip the failed file,
     * retry (after fixing the problem), or abort the whole loading
     * run.
     *
     * @param e the parsing error
     * @return a response code indicating the user's choice
     */
    public Response getResponse(Throwable e) {
        Console console = System.console();
        if (console == null) {
            return new SkipRetryAbort().promptUsingDialog(e);
        } else {
            return promptUsingConsole(console, e);
        }
    }

    private Response promptUsingConsole(Console console, Throwable e) {
        console.printf("%s\n", e.getMessage());
        while (true) {
            String response = console.readLine("Would you like to retry, skip the file, or abort the load?");
            if (response.equals("retry")) {
                return Response.RETRY;
            }
            else if (response.equals("skip")) {
                return Response.SKIP;
            }
            else if (response.equals("abort")) {
                return Response.ABORT;
            }
        }
    }

    Dialog dialog;
    Response dialogResponse;

    private Response promptUsingDialog(Throwable e) {
        String prompt = e.getMessage() + ". \nWhat would you like to do?";

        Frame hiddenFrame = new Frame(getClass().getName());
        dialog = new Dialog(hiddenFrame, "Skip, retry, or abort?", true);
        dialog.setAlwaysOnTop(true);
        dialog.setLocation(100, 100);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                windowEvent.getWindow().dispose();
            }
        });

        dialog.add(new Label(prompt),    BorderLayout.NORTH);
        dialog.add(buttonPanel(),        BorderLayout.SOUTH);
        dialog.pack();

        dialog.setVisible(true);
        hiddenFrame.dispose();

        if (dialogResponse == null)
            throw new RuntimeException("Skip/retry/abort dialog cancelled.");

        return dialogResponse;
    }

    private class ResponseButton extends Button {
        ResponseButton(String label, final Response response) {
            super(label);
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    dialogResponse = response;
                    dialog.dispose();
                }
            });
        }
    }
    private Component buttonPanel() {
        Panel buttonPanel = new Panel();

        buttonPanel.add(new ResponseButton("Skip",  Response.SKIP));
        buttonPanel.add(new ResponseButton("Retry", Response.RETRY));
        buttonPanel.add(new ResponseButton("Abort", Response.ABORT));

        return buttonPanel;
    }
}

class AlwaysSkip extends SkipRetryAbort {
    @Override
    public Response getResponse(Throwable e) {
        return Response.SKIP;
    }
}