package generated.test23;

import junit.framework.*;
import java.util.*;

/**
 * Test join inner classes.
 *
 * @author Vaidas Gasiunas
 */
public class VCTestCase extends TestCase
{

	public VCTestCase()
	{
		super("test");
	}

	public static final String expectedResult =
		"A.A, C.A, B.A; " +								  										// A
		"A.A, C.A, B.A, A.B, C.B; "	+					  										// B < A
		"A.A, C.A, B.A, A.C, C.C, B.C; "	+ 					  								// C < A
		"A.A, C.A, B.A, A.C, C.C, B.C, A.B, C.B, A.D, C.D, B.D; " +			  					// D < B & C
		"A.A, C.A, B.A, A.E; " +						  										// E < A
		"A.A, C.A, B.A, A.E, A.C, C.C, B.C, A.B, C.B, A.D, C.D, B.D, A.F, B.F; " +				// F < D & A
		"A.A, C.A, B.A, A.E, A.C, C.C, B.C, A.B, C.B, A.D, C.D, B.D, A.F, B.F, B.G; " + 		// G < C & F
		"A.A, C.A, B.A, A.C, C.C, B.C, A.B, C.B, C.H; " +	  									// H < B & C
		"A.A, C.A, B.A, A.E, A.C, C.C, B.C, A.B, C.B, A.D, C.D, B.D, A.F, B.F, D.I";			// I < F & D

	public void test() {

		System.out.println("-------> VCTest 23: Test joined polymorphism: start");

		OuterD od = new OuterD_Impl(null);

		String resA = od.$newInnerA().queryA();
		String resB = od.$newInnerB().queryA();
		String resC = od.$newInnerC().queryA();
		String resD = od.$newInnerD().queryA();
		String resE = od.$newInnerE().queryA();
		String resF = od.$newInnerF().queryA();
		String resG = od.$newInnerG().queryA();
		String resH = od.$newInnerH().queryA();
		String resI = od.$newInnerI().queryA();

		String result = resA + "; " + resB + "; " + resC + "; " + resD + "; " + resE + "; " + resF + "; " + resG +
						"; " + resH + "; " + resI;

		System.out.println(result);
		assertEquals(result, expectedResult);

        System.out.println("-------> VCTest 23: end");
	}
}

public cclass OuterA
{
	public cclass InnerA
	{
		public String queryA()
		{
			return "A.A";
		}
	}

	public cclass InnerB extends InnerA
	{
		public String queryA()
		{
			return super.queryA() + ", A.B";
		}
	}

	public cclass InnerC extends InnerA
	{
		public String queryA()
		{
			return super.queryA() + ", A.C";
		}
	}

	public cclass InnerD extends InnerB & InnerC
	{
		public String queryA()
		{
			return super.queryA() + ", A.D";
		}
	}

	public cclass InnerE extends InnerA
	{
		public String queryA()
		{
			return super.queryA() + ", A.E";
		}
	}

	public cclass InnerF extends InnerD & InnerE
	{
		public String queryA()
		{
			return super.queryA() + ", A.F";
		}
	}
}

public cclass OuterB extends OuterA
{
	public cclass InnerA
	{
		public String queryA()
		{
			return super.queryA() + ", B.A";
		}
	}

	public cclass InnerC
	{
		public String queryA()
		{
			return super.queryA() + ", B.C";
		}
	}

	public cclass InnerD
	{
		public String queryA()
		{
			return super.queryA() + ", B.D";
		}
	}

	// scoping workaround, see InnerC & InnerF bellow
	public cclass InnerF {
		public String queryA()
		{
			return super.queryA() + ", B.F";
		}
	}

	public cclass InnerG extends InnerC & InnerF
	{
		public String queryA()
		{
			return super.queryA() + ", B.G";
		}
	}
}

public cclass OuterC extends OuterA
{
	public cclass InnerA
	{
		public String queryA()
		{
			return super.queryA() + ", C.A";
		}
	}

	public cclass InnerB
	{
		public String queryA()
		{
			return super.queryA() + ", C.B";
		}
	}

	// scoping workaround, see InnerB & InnerC bellow
	public cclass InnerC {
		public String queryA()
		{
			return super.queryA() + ", C.C";
		}
	}

	public cclass InnerD
	{
		public String queryA()
		{
			return super.queryA() + ", C.D";
		}
	}

	public cclass InnerH extends InnerB & InnerC
	{
		public String queryA()
		{
			return super.queryA() + ", C.H";
		}
	}
}

public cclass OuterD extends OuterB & OuterC
{
	// scoping workaround, see InnerF & InnerD bellow
	public cclass InnerF
	{ }

	// scoping workaround, see InnerF & InnerD bellow
	public cclass InnerD
	{ }

	public cclass InnerI extends InnerF & InnerD
	{
		public String queryA()
		{
			return super.queryA() + ", D.I";
		}
	}
}