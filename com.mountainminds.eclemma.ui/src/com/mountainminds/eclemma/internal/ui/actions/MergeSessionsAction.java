/*******************************************************************************
 * Copyright (c) 2006, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 ******************************************************************************/
package com.mountainminds.eclemma.internal.ui.actions;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchWindow;

import com.mountainminds.eclemma.core.CoverageTools;
import com.mountainminds.eclemma.core.ICoverageSession;
import com.mountainminds.eclemma.core.ISessionManager;
import com.mountainminds.eclemma.internal.ui.EclEmmaUIPlugin;
import com.mountainminds.eclemma.internal.ui.UIMessages;
import com.mountainminds.eclemma.internal.ui.dialogs.MergeSessionsDialog;

/**
 * This action launches the merge sessions dialog.
 */
public class MergeSessionsAction extends Action {

  private final IWorkbenchWindow window;

  public MergeSessionsAction(IWorkbenchWindow window) {
    this.window = window;
    setText(UIMessages.MergeSessionsAction_label);
    setToolTipText(UIMessages.MergeSessionsAction_tooltip);
    setImageDescriptor(EclEmmaUIPlugin
        .getImageDescriptor(EclEmmaUIPlugin.ELCL_MERGESESSIONS));
    setDisabledImageDescriptor(EclEmmaUIPlugin
        .getImageDescriptor(EclEmmaUIPlugin.DLCL_MERGESESSIONS));
  }

  public void run() {
    ISessionManager sm = CoverageTools.getSessionManager();
    ICoverageSession[] sessions = sm.getSessions();
    String descr = UIMessages.MergeSessionsDialogDescriptionDefault_value;
    descr = MessageFormat.format(descr, new Object[] { new Date() });
    MergeSessionsDialog d = new MergeSessionsDialog(window.getShell(),
        sessions, descr);
    if (d.open() == IDialogConstants.OK_ID) {
      Object[] result = d.getResult();
      ICoverageSession merged = (ICoverageSession) result[0];
      for (final Object r : result) {
        merged = merged.merge((ICoverageSession) r, d.getDescription());
      }
      sm.addSession(merged, true, null);
      for (final Object r : result) {
        sm.removeSession((ICoverageSession) r);
      }
    }
  }

}
