/*
 * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: CCatchContext.java,v 1.1 2004-02-08 16:47:37 ostermann Exp $
 */

package org.caesarj.compiler.context;

import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.util.TokenReference;

/**
 * This class holds the contextual information for the semantic
 * analysis of a catch clause.
 */
public class CCatchContext extends CBodyContext {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Construct a context for the semantic analysis of a catch clause.
   *
   * @param	parent		...
   */
  public CCatchContext(CBodyContext parent, KjcEnvironment environment) {
    super(parent, environment);
    setReachable(true);
    clearThrowables();
  }

  public void close(TokenReference ref) {
  }

  // ----------------------------------------------------------------------
  // ACCESSORS
  // ----------------------------------------------------------------------

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------
}