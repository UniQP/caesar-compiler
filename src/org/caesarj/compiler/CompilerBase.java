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
 * $Id: CompilerBase.java,v 1.5 2005-01-24 16:52:58 aracic Exp $
 */

package org.caesarj.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Vector;

import org.caesarj.util.Messages;
import org.caesarj.util.PositionedError;
import org.caesarj.util.UnpositionedError;
import org.caesarj.util.Utils;

/**
 * This class defines the common behaviour of all KOPI compilers.
 */
public abstract class CompilerBase {

    /**
     * Creates a new compiler instance.
     * 
     * @param workingDirectory
     *            the working directory
     * @param diagnosticOutput
     *            the diagnostic output stream
     */
    protected CompilerBase(String workingDirectory, PrintWriter diagnosticOutput) {

        if(workingDirectory == null) {
            try {
                File currentDir = new File(".");
                workingDirectory = currentDir.getCanonicalPath();
            }
            catch (Exception e) {
                throw new RuntimeException("working Directory can not be set");
            }
        }
        
        this.workingDirectory = workingDirectory;
        
        if(diagnosticOutput != null)
            Log.setOutput(diagnosticOutput);
    }

    // --------------------------------------------------------------------
    // ACCESSORS
    // --------------------------------------------------------------------

    /**
     * Returns the directory where to search source files.
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }   

    // --------------------------------------------------------------------
    // Language
    // --------------------------------------------------------------------

    /**
     * Returns the version of the source code
     * 
     * @return version of the code
     */
    public abstract int getSourceVersion();

    // --------------------------------------------------------------------
    // INPUT FILE HANDLING
    // --------------------------------------------------------------------

    /**
     * Creates an array of files from the specified array of file names.
     * 
     * @param names
     *            an array of file names
     * @return an array of files
     * @exception UnpositionedError
     *                at least one file does not exist
     */
    public File[] verifyFiles(String[] names) throws UnpositionedError {
        Vector temp = new Vector(names.length);

        // replace "@file" by content of "file"
        for (int i = 0; i < names.length; i++) {
            if (!names[i].startsWith("@")) {
                temp.addElement(names[i]);
            }
            else {
                try {
                    readList(temp, workingDirectory, names[i]);
                }
                catch (IOException e) {
                    throw new UnpositionedError(
                        Messages.INVALID_LIST_FILE,
                        names[i],
                        e.getMessage());
                }
            }
        }

        File[] files = new File[temp.size()];

        for (int i = 0; i < temp.size(); i++) {
            String name;
            File file;

            if (workingDirectory == null) {
                name = (String) temp.elementAt(i);
            }
            else {
                name = workingDirectory + File.separatorChar
                    + (String) temp.elementAt(i);
            }
            file = new File(name);

            if (!file.exists()) {
                file = new File((String) temp.elementAt(i));
                if (!file.exists() || !file.isAbsolute()) {
                    throw new UnpositionedError(Messages.FILE_NOT_FOUND, temp
                        .elementAt(i), null);
                }
            }
            files[i] = file;
        }

        return files;
    }

    /**
     * Takes a vector of file names an checks that each exists.
     * 
     * @param files
     *            a vector of names
     * @return a vector of files known to exist
     * @exception UnpositionedError
     *                at least one file does not exist
     */
    public Vector verifyFiles(Vector names) throws UnpositionedError {
        return Utils.toVector(verifyFiles((String[]) Utils.toArray(
            names,
            String.class)));
    }

    /**
     * Reads a file containing file names and adds them to the specified
     * container.
     */
    private void readList(Vector list, String workingDirectory, String name)
        throws IOException, UnpositionedError {
        File file = new File((workingDirectory == null ? "" : workingDirectory
            + File.separatorChar)
            + name.substring(1));
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            readLine(list, name, line);
        }
    }

    private void readLine(Vector list, String filename, String line)
        throws UnpositionedError {
        final char[] lineChars = line.toCharArray();
        int index = 0;
        int startIndex = 0;

        while (lineChars.length > index) {
            switch (lineChars[index]) {
            case ' ':
            case '\t':
                index++;
                break;
            case '\'':
                index++;
                startIndex = index;
                while (index < lineChars.length && lineChars[index] != '\'') {
                    index++;
                }
                if (lineChars[index] != '\'') {
                    // missing end '
                    throw new UnpositionedError(
                        Messages.INVALID_LIST_FILE,
                        filename,
                        line);
                }
                else {
                    list.add(new String(lineChars, startIndex, index
                        - startIndex));
                }
                index++;
                break;
            case '\"':
                index++;
                startIndex = index;
                while (index < lineChars.length && lineChars[index] != '\"') {
                    index++;
                }
                if (lineChars[index] != '\"') {
                    // missing end "
                    throw new UnpositionedError(
                        Messages.INVALID_LIST_FILE,
                        filename,
                        line);
                }
                else {
                    list.add(new String(lineChars, startIndex, index
                        - startIndex));
                }
                index++;
                break;
            default:
                startIndex = index;
                while (index < lineChars.length && lineChars[index] != ' '
                    && lineChars[index] != '\t') {
                    index++;
                }
                list.add(new String(lineChars, startIndex, index - startIndex));
                break;
            }
        }
    }

    /**
     * Checks if destination is absolute or relative to working directory.
     */
    protected String checkDestination(String destination) {
        if (workingDirectory == null) {
            return destination;
        }
        else if (destination == null || destination.equals("")) {
            return workingDirectory;
        }
        else if (new File(destination).isAbsolute()) {
            return destination;
        }
        else {
            return workingDirectory + File.separatorChar + destination;
        }
    }

    // --------------------------------------------------------------------
    // METHODS TO BE IMPLEMENTED BY SUBCLASSES
    // --------------------------------------------------------------------

    /**
     * Runs a compilation session
     * 
     * @param args
     *            the arguments to the compiler
     * @return true iff the compilation succeeded
     */
    public abstract boolean run(String[] args);

    /**
     * Reports a trouble (error or warning).
     * 
     * @param trouble
     *            a description of the trouble to report.
     */
    public abstract void reportTrouble(PositionedError trouble);

    /**
     * Returns true iff comments should be parsed (false if to be skipped).
     */
    public abstract boolean parseComments();

    /**
     * Returns true iff compilation runs in verbose mode.
     */
    public abstract boolean verboseMode();

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    // the directory where to search source files
    private final String workingDirectory; 
}