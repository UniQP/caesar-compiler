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
 * $Id: RuleSymbol.java,v 1.1 2004-02-08 16:47:46 ostermann Exp $
 */

package org.caesarj.tools.antlr.compiler;

import org.caesarj.tools.antlr.runtime.Vector;

class RuleSymbol extends GrammarSymbol {
  RuleBlock block;	// list of alternatives
  boolean defined;	// has the rule been defined yet?
  Vector references;	// list of all nodes referencing this rule
			// not strictly needed by generic symbol table
			// but we will almost always analyze/gen code
  String access;	// access specifier for this rule
  String comment;	// A javadoc comment if any.

  public RuleSymbol(String r) {
    super(r);
    references = new Vector();
  }
  public void addReference(RuleRefElement e) {
    references.appendElement(e);
  }
  public RuleBlock getBlock() {
    return block;
  }
  public RuleRefElement getReference(int i) {
    return (RuleRefElement)references.elementAt(i);
  }
  public boolean isDefined() {
    return defined;
  }
  public int numReferences() {
    return references.size();
  }
  public void setBlock(RuleBlock rb) {
    block = rb;
  }
  public void setDefined() {
    defined = true;
  }
}