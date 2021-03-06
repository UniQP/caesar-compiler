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
 * $Id: CompileTest.java,v 1.8 2005-12-16 16:29:43 klose Exp $
 */

package org.caesarj.test.suite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.caesarj.compiler.CaesarParser;
import org.caesarj.compiler.KjcClassReader;
import org.caesarj.compiler.KjcEnvironment;
import org.caesarj.compiler.KjcOptions;
import org.caesarj.compiler.Main;
import org.caesarj.compiler.ast.phylum.JCompilationUnit;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CSourceClass;
import org.caesarj.compiler.types.KjcSignatureParser;
import org.caesarj.compiler.types.KjcTypeFactory;
import org.caesarj.compiler.types.SignatureParser;
import org.caesarj.tools.antlr.runtime.ParserException;
import org.caesarj.util.CWarning;
import org.caesarj.util.PositionedError;
import org.caesarj.util.UnpositionedError;

/**
 * ...
 * 
 * @author Ivica Aracic
 */
public class CompileTest extends CaesarTest {

    protected List<PositionedError> positionedErrorList = new LinkedList<PositionedError>();

    protected List<UnpositionedError> unpositionedErrorList = new LinkedList<UnpositionedError>();

    protected String compilerErrors = "";

    public CompileTest(CaesarTestSuite testSuite, String id,
            String description, String codeBlock) {
        super(testSuite, id, description, codeBlock);
    }

    protected String getPackageName() {
        return getTestSuite().getPackagePrefix() + ".p" + getId();
    }

    protected String getJavaFileName() {
        return getTestSuite().getOutputPath() + "." + getId() + ".java";
    }

    protected StringBuffer genJavaCodeBlock() {
        StringBuffer res = new StringBuffer();
        res.append("package " + getPackageName() + ";\n");
        res.append(getCodeBlock());
        return res;
    }

    private String formatMemory(long bytes){
        String unit = " byte";
        float fBytes = bytes;
        if (fBytes > 2*1024){
            unit = " kB";
            fBytes /= 1024;
        } 
        if (fBytes > 2*1024){
            unit = " MB";
            fBytes /= 1024;
        }
        return String.format("%f %s", new Object[]{fBytes, unit}); 
    }
    
    public void doTest() throws Throwable {
        // Create compiler
        File f = new File(getJavaFileName());
        f.getParentFile().mkdirs();

        PrintWriter pw = new PrintWriter(new FileOutputStream(getJavaFileName()
                + ".log"));
        CompilerMock compiler = new CompilerMock("tests", pw);

        StringBuffer genJavaCodeBlock = genJavaCodeBlock();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(genJavaCodeBlock.toString().getBytes());
        fos.close();

        // Compile test
        String[] args = { "-v", "-d", "bin", f.getAbsolutePath() };               
               
//        System.out.println("used memory: "+((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024.0*1024.0)));
        boolean ok = compiler.run(args);               

        long usedMem = Runtime.getRuntime().totalMemory()-
            Runtime.getRuntime().freeMemory();
        
        System.out.println(
                "Used memory: "+
                formatMemory(usedMem)
        );
        
        pw.close();

        try {
	        if (ok)
	            compilerSuccess();
	        else {
	            PositionedError[] errors = compiler.getPositionedErrors();
	            compilerErrors = errors.length == 0 ? "" : errors[0].getMessage();
	            compilerFailed();
	        }
        }
        catch(Exception e) {
        	throw e;
        }
        finally {
        	compiler = null;
        	positionedErrorList.clear();
        	unpositionedErrorList.clear();
//        	System.gc();        	
        }
      
        
        //f.delete();
    }

    public void compilerFailed() {
    	failure("(" + getId() + ") Compiler failed: " + compilerErrors +": "
                + getDescription());
    }

    public void compilerSuccess() {        
    }

    class CompilerMock extends Main {
        protected Vector<JCompilationUnit> allUnits;

        protected ClassReaderMock classReader;

        public CompilerMock(String workingDir, PrintWriter p) {
            super(workingDir, p);
            allUnits = new Vector<JCompilationUnit>();
        }

        /**
         * Returns an array containing all PositionedErrors without CWarnings
         */
        public PositionedError[] getPositionedErrors() {
            Vector<PositionedError> errors = new Vector<PositionedError>();

            for (Iterator iter = positionedErrorList.iterator(); iter.hasNext();) {
                PositionedError element = (PositionedError) iter.next();
                // Filter warnings out of errors
                if (!(element instanceof CWarning))
                    errors.add(element);
            }

            return (PositionedError[]) errors.toArray(new PositionedError[0]);
        }

        private KjcEnvironment cachedEnvironment;

        protected JCompilationUnit getJCompilationUnit(CaesarParser parser)
                throws ParserException {
            JCompilationUnit compilationUnit = super
                    .getJCompilationUnit(parser);
            allUnits.add(compilationUnit);
            return compilationUnit;
        }

        public void reportTrouble(UnpositionedError trouble) {
            unpositionedErrorList.add(trouble);
            super.reportTrouble(trouble);
        }

        public void reportTrouble(PositionedError trouble) {
            positionedErrorList.add(trouble);
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
            allLoadedClasses = new Hashtable<String, CClass>();
        }

        Hashtable<String, CClass> allLoadedClasses;

        Hashtable<String, CClass> getAllLoadedClasses() {
            return allLoadedClasses;
        }

        public boolean addSourceClass(CSourceClass cl) {
            allLoadedClasses.put(cl.getQualifiedName(), cl);
            return super.addSourceClass(cl);
        }
    }
}