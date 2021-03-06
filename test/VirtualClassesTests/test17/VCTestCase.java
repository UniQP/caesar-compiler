package generated.test17;

import junit.framework.*;
import java.util.*;

/**
 * Test extending deep classes relationships.
 *
 * @author Vaidas Gasiunas
 */
public class VCTestCase extends TestCase
{

	public VCTestCase()
	{
		super("test");
	}

	public static final String expectedResult = "A.A.A, B.A.A, A.B.A" +
												", A.A.B, child: A.A.A, B.A.A, A.B.A, A.A.C, B.A.C, A.B.C" +
												", B.A.B, child3: A.A.A, B.A.A, A.B.A, A.A.C, B.A.C, A.B.C, B.A.E" +
												", A.B.A, child2: A.A.A, B.A.A, A.B.A, A.A.C, B.A.C, A.B.C, A.B.D";

	public void test() {

		System.out.println("-------> VCTest 17: Extending Deep Classes: start");

		final OuterB ob = new OuterB();
		ob.InnerB b = ob.new InnerB();

		String result = b.defaultObject().queryA();

		System.out.println(result);
		assertEquals(result, expectedResult);

        System.out.println("-------> VCTest 17: end");
	}
}

public cclass OuterA
{
	public cclass InnerA
	{
		public cclass DeepestA
		{
			public String queryA()
			{
				return "A.A.A";
			}
		}

		public cclass DeepestB extends DeepestA
		{
		    protected DeepestA _child = null;

			public void setChild(DeepestA child)
			{
				_child = child;
			}

			public void setDefaultChild()
			{
				setChild(InnerA.this.new DeepestC());
			}

			public String queryA()
			{
				return super.queryA() + ", A.A.B, child: " + _child.queryA();
			}
		}

		public cclass DeepestC extends DeepestA
		{
			public String queryA()
			{
				return super.queryA() + ", A.A.C";
			}
		}

		public DeepestB defaultObject()
		{
			DeepestB b = new DeepestB();
			b.setDefaultChild();
			return b;
		}
	}

	public cclass InnerB extends InnerA
	{
		public cclass DeepestA
		{
			public String queryA()
			{
				return super.queryA() + ", A.B.A";
			}
		}

		public cclass DeepestB
		{
			protected DeepestA _child2 = null;

			public void setChild2(DeepestA child)
			{
				_child2 = child;
			}

			public void setDefaultChild2()
			{
				setChild2(InnerB.this.new DeepestD());
			}

			public String queryA()
			{
				return super.queryA() + ", A.B.A, child2: " + _child2.queryA();
			}
		}

		public cclass DeepestC
		{
			public String queryA()
			{
				return super.queryA() + ", A.B.C";
			}
		}

		public cclass DeepestD extends DeepestC
		{
			public String queryA()
			{
				return super.queryA() + ", A.B.D";
			}
		}

		public DeepestB defaultObject()
		{
			DeepestB b = super.defaultObject();
			b.setDefaultChild2();
			return b;
		}
	}
}

public cclass OuterB extends OuterA
{
	public cclass InnerA
	{
		public cclass DeepestA
		{
			public String queryA()
			{
				return super.queryA() + ", B.A.A";
			}
		}

		public cclass DeepestB
		{
		    protected DeepestA _child3 = null;

			public void setChild3(DeepestA child)
			{
				_child3 = child;
			}

			public void setDefaultChild3()
			{
				setChild3(InnerA.this.new DeepestE());
			}

			public String queryA()
			{
				return super.queryA() + ", B.A.B, child3: " + _child3.queryA();
			}
		}

		public cclass DeepestC
		{
			public String queryA()
			{
				return super.queryA() + ", B.A.C";
			}
		}

		public cclass DeepestE extends DeepestC
		{
			public String queryA()
			{
				return super.queryA() + ", B.A.E";
			}
		}

		public DeepestB defaultObject()
		{
			DeepestB b = super.defaultObject();
			b.setDefaultChild3();
			return b;
		}
	}
}