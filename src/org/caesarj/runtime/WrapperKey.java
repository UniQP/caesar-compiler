package org.caesarj.runtime;

/**
 * The key used for identify wrappers which are not explicitily defined.
 * 
 * @author Walter Augusto Werner
 */
public class WrapperKey
{
	public Object[] params;

	public WrapperKey(Object[] params)
	{
		this.params = params;
	}

	/**
	 * Returns true if all objects are equals
	 * it is compared with the equals(Object) method.
	 */
	public boolean equals(Object obj)
	{
		if (! (obj instanceof WrapperKey))
			return false;

		WrapperKey other = (WrapperKey) obj;
		if (params.length != other.params.length)
			return false;

		for (int i = 0; i < params.length; i++)
			if (! params[i].equals(other.params[i]))
				return false;
		
		return true;
	}

	/**
	 * The hash function is the same used by List
	 */
	public int hashCode()
	{
		int hashCode = 1;
		for (int i = 0; i < params.length; i++)
		{
			hashCode = 
				31 * hashCode 
				+ (params[i] == null ? 0 : params[i].hashCode());
		}
		return hashCode;
	}

	public static Boolean transform(boolean toTransform)
	{
		return new Boolean(toTransform);
	}
	public static Integer transform(int toTransform)
	{
		return new Integer(toTransform);
	}
	public static Double transform(double toTransform)
	{
		return new Double(toTransform);
	}
	public static Long transform(long toTransform)
	{
		return new Long(toTransform);
	}
	public static Short transform(short toTransform)
	{
		return new Short(toTransform);
	}
	public static Byte transform(byte toTransform)
	{
		return new Byte(toTransform);
	}
	public static Float transform(float toTransform)
	{
		return new Float(toTransform);
	}
	public static Character transform(char toTransform)
	{
		return new Character(toTransform);
	}
}