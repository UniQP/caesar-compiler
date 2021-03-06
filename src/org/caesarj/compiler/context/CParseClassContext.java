/*
 * This source file is part of CaesarJ 
 * For the latest info, see http://caesarj.org/
 * 
 * Copyright � 2003-2005 
 * Darmstadt University of Technology, Software Technology Group
 * Also see acknowledgements in readme.txt
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
 * $Id: CParseClassContext.java,v 1.3 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.compiler.context;

import java.util.ArrayList;
import java.util.Stack;

import org.caesarj.compiler.ast.phylum.JPhylum;
import org.caesarj.compiler.ast.phylum.declaration.JFieldDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JMethodDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JTypeDeclaration;
import org.caesarj.compiler.ast.phylum.statement.JClassBlock;

public class CParseClassContext {
  public static CParseClassContext getInstance() {
    return stack.size() == 0 ?
      new CParseClassContext() :
      (CParseClassContext)stack.pop();
  }

  public void release() {
    release(this);
  }

  public static void release(CParseClassContext context) {
    context.clear();
    stack.push(context);
  }

  /**
   * All creations done through getInstance().
   */
  private CParseClassContext() {
  }

  private void clear() {
    fields.clear();
    methods.clear();
    inners.clear();
    assertions.clear();
    body.clear();
  }

  // ----------------------------------------------------------------------
  // ACCESSORS (ADD)
  // ----------------------------------------------------------------------

  public void addFieldDeclaration(JFieldDeclaration decl) {
    fields.add(decl);
    body.add(decl);
  }

  public void addMethodDeclaration(JMethodDeclaration decl) {
    methods.add(decl);
  }

  public void addAssertionDeclaration(JMethodDeclaration decl) {
    assertions.add(decl);
  }

  public void addInnerDeclaration(JTypeDeclaration decl) {
    inners.add(decl);
  }

  public void addBlockInitializer(JClassBlock block) {
    body.add(block);
  }

  // ----------------------------------------------------------------------
  // ACCESSORS (GET)
  // ----------------------------------------------------------------------

  public JFieldDeclaration[] getFields() {
    return (JFieldDeclaration[])fields.toArray(new JFieldDeclaration[fields.size()]);
  }

  public JMethodDeclaration[] getMethods() {
    return (JMethodDeclaration[])methods.toArray(new JMethodDeclaration[methods.size()]);
  }

  public JMethodDeclaration[] getAssertions() {
    return (JMethodDeclaration[])assertions.toArray(new JMethodDeclaration[assertions.size()]);
  }

  public JTypeDeclaration[] getInnerClasses() {
    return (JTypeDeclaration[])inners.toArray(new JTypeDeclaration[inners.size()]);
  }

  public JPhylum[] getBody() {
    return (JPhylum[])body.toArray(new JPhylum[body.size()]);
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private ArrayList fields = new ArrayList();
  private ArrayList methods = new ArrayList();
  private ArrayList assertions = new ArrayList();
  private ArrayList inners = new ArrayList();
  private ArrayList body = new ArrayList();

  private static Stack stack = new Stack();
}
