/*
 * Copyright (c) 2006 Genome Research Limited.
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

package org.genedb.jogra.drawing;

import org.bushe.swing.event.EventServiceEvent;

import javax.swing.JPanel;

public class OpenWindowEvent implements EventServiceEvent {

    private JograPlugin source;
    private JPanel panel;
    
    public OpenWindowEvent(JograPlugin source, JPanel panel) {
        this.source = source;
        this.panel = panel;
    }

    public Object getSource() {
        return source;
    }

    public JPanel getPanel() {
        return this.panel;
    }

}
