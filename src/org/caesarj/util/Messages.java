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
 * $Id: Messages.java,v 1.6 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.util;


public interface Messages {
  MessageDescription	FORMATTED_ERROR = new MessageDescription("{0}", null, 0);
  MessageDescription	FORMATTED_CAUTION = new MessageDescription("{0}", null, 1);
  MessageDescription	FORMATTED_WARNING = new MessageDescription("{0}", null, 2);
  MessageDescription	FORMATTED_NOTICE = new MessageDescription("{0}", null, 3);
  MessageDescription	FORMATTED_INFO = new MessageDescription("{0}", null, 4);
  MessageDescription	NO_INPUT_FILE = new MessageDescription("No input file given", null, 0);
  MessageDescription	FILE_NOT_FOUND = new MessageDescription("File \"{0}\" not found", null, 0);
  MessageDescription	IO_EXCEPTION = new MessageDescription("I/O Exception on file \"{0}\": {1}", null, 0);
  MessageDescription	UNSUPPORTED_ENCODING = new MessageDescription("Character encoding \"{0}\" is not supported on this platform", null, 0);
  MessageDescription	CANNOT_CREATE = new MessageDescription("Cannot create file \"{0}\"", null, 0);
  MessageDescription	INVALID_LIST_FILE = new MessageDescription("Invalid list file \"{0}\" : {1}", null, 0);
  MessageDescription	NO_VIABLE_ALT_FOR_CHAR = new MessageDescription("Unexpected char \"{0}\"", null, 0);
  MessageDescription	UNEXPECTED_EOF = new MessageDescription("Unexpected end of file", null, 0);
  MessageDescription	EOF_IN_TRADITIONAL_COMMENT = new MessageDescription("End of file in comment", null, 0);
  MessageDescription	EOF_IN_ENDOFLINE_COMMENT = new MessageDescription("End of file in comment", null, 1);
  MessageDescription	ILLEGAL_CHAR = new MessageDescription("Unexpected char \"{0}\"", null, 0);
  MessageDescription	BAD_ESCAPE_SEQUENCE = new MessageDescription("Illegal escape sequence \"{0}\"", null, 0);
  MessageDescription	BAD_END_OF_LINE = new MessageDescription("Unexpected end of line in {0}", null, 0);
  MessageDescription	SYNTAX_ERROR = new MessageDescription("Syntax error: {0}", null, 0);
}
