/*
 * This source file is part of CaesarJ For the latest info, see
 * http://caesarj.org/
 * 
 * Copyright � 2003-2005 Darmstadt University of Technology, Software Technology
 * Group Also see acknowledgements in readme.txt
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * $Id: FjTestCase.java,v 1.19 2005-01-28 17:57:31 klose Exp $
 */

package org.caesarj.test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.caesarj.compiler.CaesarParser;
import org.caesarj.compiler.CompilerBase;
import org.caesarj.compiler.KjcClassReader;
import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.KjcOptions;
import org.caesarj.compiler.Main;
import org.caesarj.compiler.ast.CLineError;
import org.caesarj.compiler.ast.phylum.JCompilationUnit;
import org.caesarj.compiler.export.CSourceClass;
import org.caesarj.compiler.types.KjcSignatureParser;
import org.caesarj.compiler.types.KjcTypeFactory;
import org.caesarj.compiler.types.SignatureParser;
import org.caesarj.tools.antlr.runtime.ParserException;
import org.caesarj.util.MessageDescription;
import org.caesarj.util.PositionedError;
import org.caesarj.util.UnpositionedError;

public class FjTestCase extends TestCase {
    protected CompilerBase compiler;

    protected List positionedErrorList = new LinkedList();

    protected List unpositionedErrorList = new LinkedList();

    public FjTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @returns a String: "./test/$CLASS", meaning the subdirectory of "./test"
     *          with the name of the executing test-file.
     */
    protected String getWorkingDirectory() {
        String res = "."
                + File.separator
                + "test"
                + File.separator
                + getClass().getName().substring(
                        getClass().getName().lastIndexOf(".") + 1);

        return res;
    }

    protected void warn(String message) {
        System.out
                .println("warning - " + getClass().getName() + ": " + message);
    }

    /*
     * Compiles and runs given test file in a separate package
     */
    protected void compileAndRun(String pckgName, String testCaseName)
            throws Throwable {
        boolean success = compile(pckgName);

        assertTrue(success);

        Object generatedTest = Class.forName(
                "generated." + pckgName + "." + testCaseName).newInstance();
        ((TestCase) generatedTest).runBare();
    }

    protected void compileAndCheckErrors(String pckgName,
            MessageDescription compilerErrorsToCheck[]) throws Throwable {
        // Clean up message list
        positionedErrorList.clear();
        unpositionedErrorList.clear();

        boolean success = compile(pckgName);
        assertTrue(!success);
        
        checkErrors(compilerErrorsToCheck);
    }

    protected void compileAndCheckErrors(String pckgName,
            String compilerErrorsToCheck[]) throws Throwable {
        // Clean up message list
        positionedErrorList.clear();
        unpositionedErrorList.clear();

        boolean success = compile(pckgName);
        assertTrue(!success);
        
        checkErrors(compilerErrorsToCheck);
    }

    protected void compileDontRun(String pckgName) throws Throwable {
        assertTrue(compile(pckgName));
    }

    protected boolean compile(String pckgName) throws Throwable {
        // Clean up output folder
        removeClassFiles(pckgName);

        // Create compiler
        compiler = new CompilerMock(this, new PrintWriter(System.out) {
            public void println() {
            }

            public void write(String s) {
                System.err.println(s);
            }
        });

        // Retrieve input files
        File workingDir = new File(getWorkingDirectory() + File.separator
                + pckgName);

        List files = new LinkedList();
        findAllFiles(workingDir, files);

        List argList = new LinkedList();

        argList.add("-v"); // verbose

        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            String fileName = file.getName();
            if (fileName.endsWith(".java")) {
                argList.add(file.getAbsolutePath());
            }
        }

        String[] args = (String[]) argList.toArray(new String[0]);

        // Compile test
        return compiler.run(args);
    }

    /**
     * Tests whether each compiler error has the description that is expected.
     * @param expected
     */
    private void checkErrors(MessageDescription[] expected){
        List errors = new LinkedList();
        for (Iterator iter = positionedErrorList.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof CLineError){
                errors.add(o);
            }
        }
        PositionedError[] found = (PositionedError[]) errors.toArray( new PositionedError[0] );
        
        assertTrue("Less/more errors than expected", expected.length != found.length);
        for (int i = 0; i < found.length; i++) {
            PositionedError error = found[i];
            assertEquals( expected[i], error.getFormattedMessage().getDescription() );
        }
    }
    
    /**
     * Tests the string array with error messages passed by the compiler
     */
    protected void checkErrors(String[] errors) {
        boolean passed = true;

        /*
         * for (Iterator it = positionedErrorList.iterator(); it.hasNext() &&
         * passed;) { PositionedError error = (PositionedError) it.next();
         * 
         * boolean stop = true; }
         */
        passed = positionedErrorList.size() > 0
                && unpositionedErrorList.size() == 0;

        if (!passed) {
            boolean stop = true;
        }

        assertTrue(passed);
    }

    /*
     * Compiles and runs given test file in the root directory
     */
    protected void compileAndRun(String testCaseName) throws Throwable {
        // Clean up message list
        positionedErrorList.clear();

        // Clean up output folder
        removeClassFiles(null);

        // Compile test
        compileFile(testCaseName);

        // Execute test
        doGeneratedTest(testCaseName);
    }

    /*
     * Runs given compiled test file in the root directory
     */
    protected void doGeneratedTest(String testCaseName) throws Throwable {
        // Execute test
        System.out.println("Test " + testCaseName + " starts");

        Object generatedTest = Class.forName("generated." + testCaseName)
                .newInstance();
        ((TestCase) generatedTest).runBare();
    }

    /*
     * Compiles given test file in the root directory
     */
    protected void compileFile(String testCaseName) throws Throwable {
        // Create compiler
        compiler = new CompilerMock(this, new PrintWriter(System.out) {
            public void println() {
            }

            public void write(String s) {
                System.err.println(s);
            }
        });

        // Compile test
        String[] args = { testCaseName + ".java" };
        compiler.run(args);
    }

    /*
     * Removes all generated class files
     */
    public void removeClassFiles(String pckgName) {
        File workingDir = new File(getWorkingDirectory() + File.separator
                + "generated"
                + (pckgName == null ? "" : File.separator + pckgName));
        List files = new LinkedList();
        findAllFiles(workingDir, files);

        int deleteCount = 0;
        try {
            Iterator it = files.iterator();
            while (it.hasNext()) {
                File file = (File) it.next();
                if (file.getName().endsWith(".class")) {
                    if (file.delete())
                        deleteCount++;
                }
            }
        } catch (Throwable t) {
        }

        if (deleteCount == 0)
            warn("no class files were deleted");
    }

    private void findAllFiles(File dir, List lst) {
        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                findAllFiles(files[i], lst);
            } else {
                lst.add(files[i]);
            }
        }
    }

    public void addMessage(PositionedError trouble) {
        positionedErrorList.add(trouble);
    }

    public void addMessage(UnpositionedError trouble) {
        unpositionedErrorList.add(trouble);
    }
}

/*
 * Compiler adapter
 */

class CompilerMock extends Main {
    protected Vector allUnits;

    protected ClassReaderMock classReader;

    protected FjTestCase testCase;

    public CompilerMock(FjTestCase test, PrintWriter p) {
        super(test.getWorkingDirectory(), p);
        this.testCase = test;
        allUnits = new Vector();
    }

    private KjcEnvironment cachedEnvironment;

    protected JCompilationUnit getJCompilationUnit(CaesarParser parser)
            throws ParserException {
        JCompilationUnit compilationUnit = super.getJCompilationUnit(parser);
        allUnits.add(compilationUnit);
        return compilationUnit;
    }

    public void reportTrouble(UnpositionedError trouble) {
        testCase.addMessage(trouble);
        super.reportTrouble(trouble);
    }

    public void reportTrouble(PositionedError trouble) {
        testCase.addMessage(trouble);
        super.reportTrouble(trouble);
    }

    protected KjcEnvironment createEnvironment(KjcOptions options) {
        if (cachedEnvironment == null) {
            classReader = new ClassReaderMock(options.classpath,
                    options.extdirs, new KjcSignatureParser());
            cachedEnvironment = new KjcEnvironment(this, classReader,
                    new KjcTypeFactory(classReader), options);
        }
        return cachedEnvironment;
    }
};

/*
 * Class reader adapter
 */
class ClassReaderMock extends KjcClassReader {
    public ClassReaderMock(String classp, String extdirs,
            SignatureParser signatureParser) {
        super(classp, extdirs, signatureParser);
        allLoadedClasses = new Hashtable();
    }

    Hashtable allLoadedClasses;

    Hashtable getAllLoadedClasses() {
        return allLoadedClasses;
    }

    public boolean addSourceClass(CSourceClass cl) {
        allLoadedClasses.put(cl.getQualifiedName(), cl);
        return super.addSourceClass(cl);
    }
}