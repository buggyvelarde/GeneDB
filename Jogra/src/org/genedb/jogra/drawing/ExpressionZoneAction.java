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

import org.genedb.jogra.domain.ExpressionZone;

import org.bushe.swing.event.EventBus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ExpressionZoneAction extends AbstractAction {
    
    private ExpressionZone zone;
    
    public ExpressionZoneAction(ExpressionZone zone) {
        this.zone = zone;
    }

    public void actionPerformed(ActionEvent event) {
        System.err.println("I'm fired for '"+event.getActionCommand()+"'");
        ExpressionEvent ee = new ExpressionEvent(this, zone);
        EventBus.publish(ee);
    }

}
