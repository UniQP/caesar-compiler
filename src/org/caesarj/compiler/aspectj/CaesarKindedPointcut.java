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
 */
package org.caesarj.compiler.aspectj;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.Bindings;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;


/**
 * This code is almost completely copied from AspectJ's KindedPointcut.
 * 
 * Our PatternParser generates CaesarKindedPointcuts instead of the original
 * KindedPointcuts, because we provide more methods for manipulating the
 * internal attributes (kind and signature)
 * 
 * @author Thiago Tonelli Bartolomei <thiagobart@gmail.com>
 *
 */
public class CaesarKindedPointcut extends Pointcut {
	
	protected Shadow.Kind kind;
	protected SignaturePattern signature;
	
    public CaesarKindedPointcut(Shadow.Kind kind, SignaturePattern signature) {
    	//super(kind, signature);
		this.kind = kind;
		this.signature = signature;
	}
    
    public Shadow.Kind getKind() {
    	return kind;
    }
    
    public void setKind(Shadow.Kind kind) {
    	this.kind = kind;
    }
    
    public SignaturePattern getSignature() {
    	return signature;
    }
    
    public void setSignature(SignaturePattern signature) {
    	this.signature = signature;
    }
    
    // ----------------------------------------------------------------------
    // FROM HERE, CODE COPIED FROM ASPECTJ'S KindedPointcut 
    // ----------------------------------------------------------------------

    private ShadowMunger munger = null; // only set after concretization
	
    public FuzzyBoolean fastMatch(FastMatchInfo info) {
    	if (info.getKind() != null) {
			if (info.getKind() != kind) return FuzzyBoolean.NO;
    	}

		return FuzzyBoolean.MAYBE;
	}	
	
	public FuzzyBoolean match(Shadow shadow) {
		if (shadow.getKind() != kind) return FuzzyBoolean.NO;
		
		if (!signature.matches(shadow.getSignature(), shadow.getIWorld())){
            if(kind == Shadow.MethodCall) {
                warnOnConfusingSig(shadow);
            }
            return FuzzyBoolean.NO; 
        }

		return FuzzyBoolean.YES;
	}
	
	public FuzzyBoolean match(JoinPoint.StaticPart jpsp) {
		if (jpsp.getKind().equals(kind.getName())) {
			if (signature.matches(jpsp)) {
				return FuzzyBoolean.YES;
			}
		}
		return FuzzyBoolean.NO;
	}
	
	private void warnOnConfusingSig(Shadow shadow) {
        // no warnings for declare error/warning
        if (munger instanceof Checker) return;
        
        World world = shadow.getIWorld();
        
		// warning never needed if the declaring type is any
		TypeX exactDeclaringType = signature.getDeclaringType().getExactType();
        
		ResolvedTypeX shadowDeclaringType =
			shadow.getSignature().getDeclaringType().resolve(world);
        
		if (signature.getDeclaringType().isStar()
			|| exactDeclaringType== ResolvedTypeX.MISSING)
			return;

        // warning not needed if match type couldn't ever be the declaring type
		if (!shadowDeclaringType.isAssignableFrom(exactDeclaringType)) {
            return;
		}

		// if the method in the declaring type is *not* visible to the
		// exact declaring type then warning not needed.
		int shadowModifiers = shadow.getSignature().getModifiers(world);
		if (!ResolvedTypeX
			.isVisible(
				shadowModifiers,
				shadowDeclaringType,
				exactDeclaringType.resolve(world))) {
			return;
		}
		
		// PR60015 - Don't report the warning if the declaring type is object and 'this' is an interface
		if (exactDeclaringType.isInterface(world) && shadowDeclaringType.equals(world.resolve("java.lang.Object"))) {
			return;
		}

		SignaturePattern nonConfusingPattern =
			new SignaturePattern(
				signature.getKind(),
				signature.getModifiers(),
				signature.getReturnType(),
				TypePattern.ANY,
				signature.getName(), 
				signature.getParameterTypes(),
				signature.getThrowsPattern());

		if (nonConfusingPattern
			.matches(shadow.getSignature(), shadow.getIWorld())) {
                shadow.getIWorld().getLint().unmatchedSuperTypeInCall.signal(
                    new String[] {
                        shadow.getSignature().getDeclaringType().toString(),
                        signature.getDeclaringType().toString()
                    },
                    this.getSourceLocation(),
                    new ISourceLocation[] {shadow.getSourceLocation()} );               
		}
	}

	public boolean equals(Object other) {
		if (!(other instanceof CaesarKindedPointcut)) return false;
		CaesarKindedPointcut o = (CaesarKindedPointcut)other;
		return o.kind == this.kind && o.signature.equals(this.signature);
	}
    
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + signature.hashCode();
        return result;
    }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(kind.getSimpleName());
		buf.append("(");
		buf.append(signature.toString());
		buf.append(")");
		return buf.toString();
	}
	
	
	public void postRead(ResolvedTypeX enclosingType) {
		signature.postRead(enclosingType);
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.KINDED);
		kind.write(s);
		signature.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		Shadow.Kind kind = Shadow.Kind.read(s);
		SignaturePattern sig = SignaturePattern.read(s, context);
		KindedPointcut ret = new KindedPointcut(kind, sig);
		ret.readLocation(context, s);
		return ret;
	}

	// XXX note: there is no namebinding in any kinded pointcut.
	// still might want to do something for better error messages
	// We want to do something here to make sure we don't sidestep the parameter
	// list in capturing type identifiers.
	public void resolveBindings(IScope scope, Bindings bindings) {
		if (kind == Shadow.Initialization) {
//			scope.getMessageHandler().handleMessage(
//				MessageUtil.error(
//					"initialization unimplemented in 1.1beta1",
//					this.getSourceLocation()));
		}
		signature = signature.resolveBindings(scope, bindings);
		
		
		if (kind == Shadow.ConstructorExecution) { 		// Bug fix 60936
		  if (signature.getDeclaringType() != null) {
			World world = scope.getWorld();
			TypeX exactType = signature.getDeclaringType().getExactType();
			if (signature.getKind() == Member.CONSTRUCTOR &&
				!exactType.equals(ResolvedTypeX.MISSING) &&
				exactType.isInterface(world) &&
				!signature.getDeclaringType().isIncludeSubtypes()) {
					world.getLint().noInterfaceCtorJoinpoint.signal(exactType.toString(), getSourceLocation());
				}
		  }
		}
	}
	
	public void resolveBindingsFromRTTI() {
		signature = signature.resolveBindingsFromRTTI();
	}
	
	public Test findResidue(Shadow shadow, ExposedState state) {
		return match(shadow).alwaysTrue() ? Literal.TRUE : Literal.FALSE;
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		Pointcut ret = new KindedPointcut(kind, signature, bindings.getEnclosingAdvice());
        ret.copyLocationFrom(this);
        return ret;
	}
}