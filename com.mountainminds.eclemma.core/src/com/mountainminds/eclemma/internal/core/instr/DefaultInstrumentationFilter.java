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
package com.mountainminds.eclemma.internal.core.instr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.launching.JavaRuntime;

import com.mountainminds.eclemma.core.IClassFiles;
import com.mountainminds.eclemma.core.ICorePreferences;

/**
 * Utility to retrieve the list of {@link IClassFiles} that will be instrumented
 * by default.
 */
public class DefaultInstrumentationFilter {

  private final ICorePreferences preferences;

  /**
   * Creates a new filter based on the given preferences.
   * 
   * @param preferences
   *          call-back to retrieve current settings from.
   */
  public DefaultInstrumentationFilter(final ICorePreferences preferences) {
    this.preferences = preferences;
  }

  /**
   * Returns a filtered copy of the given {@link IClassFiles} array.
   * 
   * @param classfiles
   *          {@link IClassFiles} to filter
   * @param configuration
   *          context information
   * @return filtered list
   * @throws CoreException
   *           may occur when accessing the Java model
   */
  public IClassFiles[] filter(final IClassFiles[] classfiles,
      final ILaunchConfiguration configuration) throws CoreException {
    final List<IClassFiles> list = new ArrayList<IClassFiles>(
        Arrays.asList(classfiles));
    if (preferences.getDefaultInstrumentationSourceFoldersOnly()) {
      sourceFoldersOnly(list);
    }
    if (preferences.getDefaultInstrumentationSameProjectOnly()) {
      sameProjectOnly(list, configuration);
    }
    String filter = preferences.getDefaultInstrumentationFilter();
    if (filter != null && filter.length() > 0) {
      matchingPathsOnly(list, filter);
    }
    return list.toArray(new IClassFiles[list.size()]);
  }

  private void sourceFoldersOnly(final List<IClassFiles> list) {
    for (final Iterator<IClassFiles> i = list.iterator(); i.hasNext();) {
      final IClassFiles c = i.next();
      if (c.isBinary()) {
        i.remove();
      }
    }
  }

  private void sameProjectOnly(final List<IClassFiles> list,
      final ILaunchConfiguration configuration) throws CoreException {
    final IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
    if (javaProject != null) {
      for (final Iterator<IClassFiles> i = list.iterator(); i.hasNext();) {
        if (!isSameProject(i.next(), javaProject)) {
          i.remove();
        }
      }
    }
  }

  private boolean isSameProject(final IClassFiles classfiles,
      final IJavaProject javaProject) {
    final IPackageFragmentRoot[] roots = classfiles.getPackageFragmentRoots();
    for (final IPackageFragmentRoot root : roots) {
      if (javaProject.equals(root.getJavaProject())) {
        return true;
      }
    }
    return false;
  }

  private void matchingPathsOnly(final List<IClassFiles> list,
      final String filter) {
    final String[] matchStrings = filter.split(","); //$NON-NLS-1$
    for (final Iterator<IClassFiles> i = list.iterator(); i.hasNext();) {
      if (!isPathMatch(i.next(), matchStrings)) {
        i.remove();
      }
    }
  }

  private boolean isPathMatch(final IClassFiles classfiles,
      final String[] matchStrings) {
    final IPackageFragmentRoot[] roots = classfiles.getPackageFragmentRoots();
    for (final IPackageFragmentRoot root : roots) {
      final String path = root.getPath().toString();
      for (final String match : matchStrings) {
        if (path.indexOf(match) != -1) {
          return true;
        }
      }
    }
    return false;
  }

}
