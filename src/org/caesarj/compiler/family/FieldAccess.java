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
 * $Id: FieldAccess.java,v 1.13 2005-03-03 12:18:56 aracic Exp $
 */

package org.caesarj.compiler.family;

import org.caesarj.compiler.types.CReferenceType;


/**
 * ...
 * 
 * @author Ivica Aracic
 */
public class FieldAccess extends MemberAccess {

    public FieldAccess(boolean finalPath, Path prefix, String field, CReferenceType type) {
        super(finalPath, prefix, field, type);
    }
    
    public Path clonePath() {
        return new FieldAccess(finalPath, prefix==null ? null : prefix.clonePath(), name, type);
    }
}
