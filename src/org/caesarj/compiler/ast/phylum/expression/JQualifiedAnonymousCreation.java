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
 * $Id: JQualifiedAnonymousCreation.java,v 1.11 2005-11-10 15:06:03 gasiunas Exp $
 */

package org.caesarj.compiler.ast.phylum.expression;

import org.caesarj.compiler.ast.CMethodNotFoundError;
import org.caesarj.compiler.ast.phylum.declaration.CjClassDeclaration;
import org.caesarj.compiler.ast.phylum.declaration.JConstructorDeclaration;
import org.caesarj.compiler.ast.phylum.statement.JConstructorBlock;
import org.caesarj.compiler.ast.phylum.statement.JStatement;
import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
import org.caesarj.compiler.ast.phylum.variable.JLocalVariable;
import org.caesarj.compiler.ast.visitor.IVisitor;
import org.caesarj.compiler.codegen.CodeLabel;
import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.CaesarMessages;
import org.caesarj.compiler.constants.KjcMessages;
import org.caesarj.compiler.context.CExpressionContext;
import org.caesarj.compiler.context.GenerationContext;
import org.caesarj.compiler.export.CClass;
import org.caesarj.compiler.export.CMethod;
import org.caesarj.compiler.types.CReferenceType;
import org.caesarj.compiler.types.CThrowableInfo;
import org.caesarj.compiler.types.CType;
import org.caesarj.compiler.types.TypeFactory;
import org.caesarj.util.PositionedError;
import org.caesarj.util.TokenReference;
import org.caesarj.util.UnpositionedError;

/**
 * !!! This class represents a new allocation expression 'new Toto(1){}'
 */
public class JQualifiedAnonymousCreation extends JExpression {

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    /**
     * Construct a node in the parsing tree
     * This method is directly called by the parser
     * @param	where		the line of this node in the source code
     * @param	prefix		the prefix denoting the object to search
     * @param	ident		the simple name of the class
     * @param	params		parameters to be passed to constructor
     */
    public JQualifiedAnonymousCreation(
        TokenReference where,
        JExpression prefix,
        String ident,
        JExpression[] params,
        CjClassDeclaration decl) {
        super(where);

        this.prefix = prefix;
        this.ident = ident;
        this.params = params;
        this.decl = decl;
    }

    // ----------------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------------

    /**
     * Compute the type of this expression (called after parsing)
     * @return the type of this expression
     */
    public CType getType(TypeFactory factory) {
        return type;
    }

    /**
     * Returns true iff this expression can be used as a statement (JLS 14.8)
     */
    public boolean isStatementExpression() {
        return true;
    }

    // ----------------------------------------------------------------------
    // SEMANTIC ANALYSIS
    // ----------------------------------------------------------------------

    /**
     * Analyses the expression (semantically).
     * @param	context		the analysis context
     * @return	an equivalent, analysed expression
     * @exception	PositionedError	the analysis detected an error
     */
    public JExpression analyse(CExpressionContext context)
        throws PositionedError {
    	
    	if (context.getClassContext().getCClass().isMixin()) {
    		throw new PositionedError(getTokenReference(), CaesarMessages.ANONYMOUS_CLASS_IN_CCLASS);
    	}
    	
        TypeFactory factory = context.getTypeFactory();

        context = new CExpressionContext(context, context.getEnvironment());

        prefix = prefix.analyse(context);
        check(
            context,
            prefix.getType(factory).isClassType(),
            KjcMessages.FIELD_BADACCESS,
            prefix.getType(factory));
        CClass newClass;

        try {
            newClass =
                prefix.getType(factory).getCClass().lookupClass(
                    context.getClassContext().getCClass(),
                    ident);
        }
        catch (UnpositionedError e) {
            throw e.addPosition(getTokenReference());
        }

        check(
            context,
            newClass != null,
            KjcMessages.TYPE_UNKNOWN,
            prefix.getType(factory) + "." + ident);

        /* 15.9.1 must be an accessible, non-final inner class*/
        check(
            context,
            !newClass.isFinal(),
            KjcMessages.CLASS_PARENT_FINAL,
            prefix.getType(factory) + "." + ident);
        check(
            context,
            !newClass.isStatic() && !newClass.getOwner().isInterface(),
            KjcMessages.QUALIFIED_STATIC,
            prefix.getType(factory) + "." + ident);
        type = newClass.getAbstractType(); //!!FIXIFGEN

        CType[] argsType;
        CClass owner = context.getClassContext().getCClass();
        CReferenceType superClass;

        try {
            type = (CReferenceType)type.checkType(context);
        }
        catch (UnpositionedError e) {
            throw e.addPosition(getTokenReference());
        }

        check(
            context,
            true,
            KjcMessages.NEW_TVPE_VARIABLE,
            type);

        argsType = new CType[params.length];

        for (int i = 0; i < argsType.length; i++) {
            params[i] = params[i].analyse(context);
            argsType[i] = params[i].getType(factory);
            verify(argsType[i] != null);
        }

        decl.generateInterface(
            context.getClassReader(),
            owner,
            owner.getQualifiedName()
                + "$"
                + context.getClassContext().getNextSyntheticIndex());
        decl.setContext(context);

        CClass superCClass = type.getCClass();

        if (superCClass.isInterface()) {
            superClass =
                context.getTypeFactory().createReferenceType(
                    TypeFactory.RFT_OBJECT);
            decl.setInterfaces(new CReferenceType[] { type });
        }
        else {
            superClass = type;
        }
        decl.setSuperClass(superClass);

        // The class of the super class must be set explicitly
        // before lookup of the constructor of the superclass
        // because it will be set in decl only in checkInterface.
        // On the other hand checkInterface needs the constructor
        // to be created.
        // graf 010422 :
        // But, why not analyse the constructor later ? Perhaps
        // to be able to signal an error ?

        decl.getSourceClass().setSuperClass(superClass);

        // add implicit constructor
        JConstructorDeclaration cstr;
        CMethod superCstr;

        try {
            superCstr =
                superClass.getCClass().lookupMethod(
                    context,
                    decl.getSourceClass(),
                    null,
                    JAV_CONSTRUCTOR,
                    argsType);
        }
        catch (UnpositionedError cue) {
            throw cue.addPosition(getTokenReference());
        }
        if (superCstr == null) {
            throw new CMethodNotFoundError(
                getTokenReference(),
                null,
                superClass.toString(),
                argsType);
        }

        CType[] parameters = superCstr.getParameters();
        JFormalParameter[] fparams = new JFormalParameter[parameters.length];
        CReferenceType[] throwables = superCstr.getThrowables();

        for (int i = 0; i < parameters.length; i++) {
            fparams[i] =
                new JFormalParameter(
                    getTokenReference(),
                    JLocalVariable.DES_GENERATED,
                    parameters[i],
                    "dummy" + i,
                    true);
        }

        JExpression[] checkedParams = new JExpression[params.length];

        for (int i = 0; i < params.length; i++) {
            checkedParams[i] =
                new JLocalVariableExpression(getTokenReference(), fparams[i]);

        }

        // add parameter for qualified instance creation
        JFormalParameter[] qualfparams =
            new JFormalParameter[fparams.length + 1];

        System.arraycopy(fparams, 0, qualfparams, 1, fparams.length);
        qualfparams[0] =
            new JFormalParameter(
                getTokenReference(),
                JLocalVariable.DES_GENERATED,
                prefix.getType(factory),
                "dummySuper",
                true);

        cstr =
            new JConstructorDeclaration(
                getTokenReference(),
                ACC_PUBLIC,
                decl.getSourceClass().getIdent(),
                qualfparams,
                throwables,
                new JConstructorBlock(
                    getTokenReference(),
                    new JConstructorCall(
                        getTokenReference(),
                        false,
                        new JTypeNameExpression(
                            getTokenReference(),
                            (CReferenceType)prefix.getType(factory)),
                        checkedParams),
                    new JStatement[0]),
                null,
                null,
                factory);
        decl.setDefaultConstructor(cstr);
        decl.join(context.getClassContext());
        decl.checkInterface(context.getClassContext());
        if (context.isStaticContext()) {
            decl.getSourceClass().setModifiers(
                decl.getSourceClass().getModifiers() | ACC_STATIC);
        }
        else {
            decl.addOuterThis(context);
        }
        decl.checkInitializers(context);
        decl.checkTypeBody(context);
        context.getClassContext().getTypeDeclaration().addLocalTypeDeclaration(
            decl);

        type = decl.getSourceClass().getAbstractType(); //!! FIXIFGEN
        decl.getSourceClass().setQualifiedAndAnonymous(true);

        //!!! review and create test cases

        check(
            context,
            !type.getCClass().isAbstract(),
            KjcMessages.NEW_ABSTRACT,
            type);
        check(
            context,
            !type.getCClass().isInterface(),
            KjcMessages.NEW_INTERFACE,
            type);

        constructor = cstr.getMethod();
        /* JLS 15.9.1 It is a compile-time error if Identifier is 
           not the simple name (�6.2) of an accessible (�6.6) non-abstract 
           inner class (�8.1.2) */
        check(
            context,
            constructor.getOwner().isNested()
                && !constructor.getOwner().isStatic()
                && constructor.getOwner().hasOuterThis(),
            KjcMessages.NOT_INNER_CLASS,
            type);

        // check access
        local = context.getClassContext().getCClass();
        check(
            context,
            constructor.isAccessible(local),
            KjcMessages.CONSTRUCTOR_NOACCESS,
            type);

        if (constructor.getOwner().isNested()) {
            check(
                context,
                !constructor.getOwner().hasOuterThis()
                    || prefix.getType(factory).getCClass().descendsFrom(
                        constructor.getOwner().getSuperClass().getOwner()),
                KjcMessages.INNER_INHERITENCE,
                constructor.getOwnerType(),
                local.getAbstractType());
        }

        CReferenceType[] exceptions = constructor.getThrowables();
        for (int i = 0; i < exceptions.length; i++) {
            context.getBodyContext().addThrowable(
                new CThrowableInfo(exceptions[i], this));
        }

        argsType = constructor.getParameters();

        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].convertType(context, argsType[i + 1]);
        }

        return this;
    }

    // ----------------------------------------------------------------------
    // CODE GENERATION
    // ----------------------------------------------------------------------

    /**
     * Generates JVM bytecode to evaluate this expression.
     *
     * @param	code		the bytecode sequence
     * @param	discardValue	discard the result of the evaluation ?
     */
    public void genCode(GenerationContext context, boolean discardValue) {
        CodeSequence code = context.getCodeSequence();

        setLineNumber(code);

        code.plantClassRefInstruction(
            opc_new,
            type.getCClass().getQualifiedName());
        if (!discardValue) {
            code.plantNoArgInstruction(opc_dup);
        }

        code.plantLoadThis();
        prefix.genCode(context, false); // Qualified !!!!!
        /* JLS 15.9.4 First, if the class instance creation expression is 
           a qualified class instance creation expression, the qualifying 
           primary expression is evaluated. If the qualifying expression 
           evaluates to null, a NullPointerException is raised  */
        CodeLabel ok = new CodeLabel();

        code.plantNoArgInstruction(opc_dup);
        code.plantJumpInstruction(opc_ifnonnull, ok);
        code.plantNoArgInstruction(opc_aconst_null);
        code.plantNoArgInstruction(opc_athrow);
        code.plantLabel(ok);

        for (int i = 0; i < params.length; i++) {
            params[i].genCode(context, false);
        }
        constructor.getOwner().genOuterSyntheticParams(context);

        constructor.genCode(context, true);
    }

    public void recurse(IVisitor s) {
        prefix.accept(s);
        for (int i = 0; i < params.length; i++) {
            params[i].accept(s);
        }
    }
    
    // ----------------------------------------------------------------------
    // DATA MEMBERS
    // ----------------------------------------------------------------------

    private JExpression prefix;
    private String ident;
    private JExpression[] params;
    private CReferenceType type;
    private CClass local;
    private CMethod constructor;
    private CjClassDeclaration decl;
}
