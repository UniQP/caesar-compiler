package org.caesarj.compiler.ast;

import org.caesarj.kjc.CBodyContext;
import org.caesarj.kjc.CExpressionContext;
import org.caesarj.kjc.CType;
import org.caesarj.kjc.JExpression;
import org.caesarj.kjc.JVariableDefinition;
import org.caesarj.compiler.PositionedError;
import org.caesarj.compiler.TokenReference;
import org.caesarj.compiler.UnpositionedError;

/**
 * This class is temporary, it is here just because we don't know yet
 * what we have to do when introducing a down cast in the methods
 * that are defined in the bindings or providing classes. Once defined, in the
 * FjFormalParamenterDefinition, in the method introduceDownCastVariable this
 * class must be changed to FjFamilyVariableDefinition
 * @author Walter Augusto Werner
 */
public class CciFamilyVariableDefinition 
	extends JVariableDefinition
{

	private JExpression cachedInitializer;
	public CciFamilyVariableDefinition(
		TokenReference where,
		int modifiers,
		CType type,
		String ident,
		JExpression initializer,
		FjFamily family)
	{
		super(where, modifiers, type, ident, initializer);
		if (family != null)
			FjFamilyContext.getInstance().setFamilyOf(this, family);
		cachedInitializer = initializer;
	}
	/**
	 * Method copied from FjVariableDefinition.
	 */
	public void analyse(CBodyContext context) throws PositionedError
	{

		FjTypeSystem fjts = new FjTypeSystem();
		try
		{
			//Walter: inserted the sencod param in this method call
			FjFamily family =
				fjts.resolveFamily(
					context,
					context.getClassContext().getCClass(),
					type);
			// if the typename is qualified by a variable
			// the qualifier has to be resolved to its type
			if (family != null)
			{
				FjFamilyContext.getInstance().setFamilyOf(this, family);
				//The line below is the only different statement 
				//comparing with the method defined in FjVariableDefinition
				//type = family.getInnerType();

				// if there is an initializer check its family
				if (cachedInitializer != null)
				{
					fjts.checkFamilies(
						new CExpressionContext(
							context,
							context.getEnvironment()),
						family,
						cachedInitializer);
				}
			}
		}
		catch (UnpositionedError e)
		{
			throw e.addPosition(getTokenReference());
		}
		super.analyse(context);
	}

}