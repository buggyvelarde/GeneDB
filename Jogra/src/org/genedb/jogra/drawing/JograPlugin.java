/*
 * Copyright (c) 2006-2009 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */

package org.genedb.jogra.drawing;

import java.util.List;

import javax.swing.JPanel;

/**
 * Core application interface. Define the entry and manipulation points of a component
 * that want to be used by Jogra.
 *
 */
public interface JograPlugin {

    /**
     * Supply a JPanel which is displayed in the main Jogra application panel,
     * used for launching a plug-in, or displaying status
     *
     * @return a JPanel, ready for displaying
     */
    JPanel getMainWindowPlugin();

    /**
     * The name of the plug-in, maybe this should be set in the config
     *
     * @return the name
     */
    String getName();

    /**
     * Is there only one instance of the plug-in, by default
     *
     * @return true if there should only be one copy of the plug-in
     */
    boolean isSingletonByDefault();

    /**
     * Allow the plug-in to indicate whether it has unsaved changes
     *
     * @return true if there are changes to be saved
     */
    boolean isUnsaved();

    /**
     * @param newArgs
     */
    void process(List<String> newArgs);

}
