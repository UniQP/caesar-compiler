package generated.test18;

import junit.framework.*;
import java.util.*;

/**
 * Test multiple inheritance of methods.
 *
 * @author Vaidas Gasiunas
 */
public class VCTestCase extends TestCase
{

	public VCTestCase()
	{
		super("test");
	}

	public static final String expectedResult = "A.a; A.b, B.b; A.c, C.c; A.d, C.d, B.d; B.e; B.f, D.f; A.g, C.g, B.g, D.g";

	public void test() {

		System.out.println("-------> VCTest 18: Multiple Inheritance of Methods: start");

		OuterD d = new OuterD_Impl(null); // !!! remove parameter

		String resA = d.queryA();
		String resB = d.queryB();
		String resC = d.queryC();
		String resD = d.queryD();
		String resE = d.queryE();
		String resF = d.queryF();
		String resG = d.queryG();
		String result = resA + "; " + resB + "; " + resC + "; " + resD + "; " + resE + "; " + resF + "; " + resG;

		System.out.println(result);
		assertEquals(result, expectedResult);

        System.out.println("-------> VCTest 18: end");
	}
}

public cclass OuterA
{
	String _a = "A.a";

	String _b = "A.b";

	String _c = "A.c";

	String _d = "A.d";

	String _g = "A.g";

	public String queryA()
	{
		return _a;
	}

	public String queryB()
	{
		return _b;
	}

	public String queryC()
	{
		return _c;
	}

	public String queryD()
	{
		return _d;
	}

	public String queryG()
	{
		return _g;
	}
}

public cclass OuterB extends OuterA
{
	String _b = "B.b";

	String _d = "B.d";

	String _e = "B.e";

	String _f = "B.f";

	String _g = "B.g";

	public String queryB()
	{
		return super.queryB() + ", " + _b;
	}

	public String queryD()
	{
		return super.queryD() + ", " + _d;
	}

	public String queryE()
	{
		return _e;
	}

	public String queryF()
	{
		return _f;
	}

	public String queryG()
	{
		return super.queryG() + ", " + _g;
	}
}

public cclass OuterC extends OuterA
{
	String _c = "C.c";

	String _d = "C.d";

	String _e = "C.e";

	String _f = "C.f";

	String _g = "C.g";

	public String queryC()
	{
		return super.queryC() + ", " + _c;
	}

	public String queryD()
	{
		return super.queryD() + ", " + _d;
	}

	public String queryE()
	{
		return _e;
	}

	public String queryF()
	{
		return _f;
	}

	public String queryG()
	{
		return super.queryG() + ", " + _g;
	}
}

public cclass OuterD extends OuterB & OuterC
{
	String _f = "D.f";

	String _g = "D.g";

	public String queryF()
	{
		return super.queryF() + ", " + _f;
	}

	public String queryG()
	{
		return super.queryG() + ", " + _g;
	}
}
