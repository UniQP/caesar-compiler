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
 * $Id: JClassImport.java,v 1.3 2005-01-24 16:52:59 aracic Exp $
 */

package org.caesarj.compiler.ast.phylum;

import org.caesarj.compiler.CompilerBase;
import org.caesarj.compiler.ast.JavaStyleComment;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.util.CWarning;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;

/**
 * JLS 7.5.1 Single-Type-Import Declaration.
 *
 * This class represents a single-type-import declaration
 * in the syntax tree.
 */
public class JClassImport extends JPhylum {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Constructs a single-type-import declaration node in the syntax tree.
   *
   * @param	where		the line of this node in the source code
   * @param	name		the canonical name of the type
   * @param	comments	other comments in the source code
   */
  public JClassImport(TokenReference where,
		      String name,
		      JavaStyleComment[] comments)
  {
    super(where);

    this.name = name;
    this.comments = comments;

    this.used = false;

    int		index = name.lastIndexOf('/');

    this.ident = index == -1 ? name : name.substring(index + 1).intern();
  }

  // ----------------------------------------------------------------------
  // ACCESSORS & MUTATORS
  // ----------------------------------------------------------------------

  /**
   * Returns the fully qualified name of the imported type.
   */
  public String getQualifiedName() {
    return name;
  }

  /**
   * Returns the simple qualified name of the imported type.
   */
  public String getSimpleName() {
    return ident;
  }

  /**
   * States that specified class is used.
   */
  public void setUsed() {
    used = true;
  }

  // ----------------------------------------------------------------------
  // SEMANTIC ANALYSIS
  // ----------------------------------------------------------------------

  /**
   * Analyses the statement (semantically).
   * @param	context		the analysis context
   * @exception	PositionedError	the analysis detected an error
   */
  public void analyse(CompilerBase compiler) {
    if (!used && getTokenReference() != TokenReference.NO_REF) {
      compiler.reportTrouble(new CWarning(getTokenReference(),
					  KjcMessages.UNUSED_CLASS_IMPORT,
					  name.replace('/', '.'),
					  null));
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private final String			name;
  private final String			ident;
  private final JavaStyleComment[]	comments;
  private boolean			used;
}
