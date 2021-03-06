package generated.test115;

import java.io.IOException;

import org.caesarj.runtime.CaesarThread;

import junit.framework.TestCase;

/**
 * Test crosscuts in mixins
 *
 * @author Vaidas Gasiunas
 */

public class ADTestCase extends TestCase
{
    public ADTestCase()
    {
        super("test");
    }

    public static StringBuffer result = new StringBuffer();

	public String expectedResult =
		":before cutDA:before cutBA:before cutCA:before cutAA:A.A";
	


    public void test()
    {
		System.out.println("-------> ADTest 15: Crosscuts in mixins: start");

        new DeployA().test();

        System.out.println(result);
        assertEquals(expectedResult, result.toString());

        System.out.println("-------> ADTest 15: end");
    }
}

public cclass DeployA
{
    public void test()
    {
		OuterA oa = new OuterA();

		deploy(new AspectD())
        {
        	oa.doA();
        }
    }
}

public cclass AspectA
{
	pointcut cutAA() : call(* OuterA+.doA(..));

	before() : cutAA()
	{
		ADTestCase.result.append(":before cutAA");
	}
}

public cclass AspectB extends AspectA
{
	pointcut cutBA() : call(* OuterA+.doA(..));

	before() : cutBA()
	{
		ADTestCase.result.append(":before cutBA");
	}
}

public cclass AspectC extends AspectA
{
	pointcut cutCA() : call(* OuterA+.doA(..));

	before() : cutCA()
	{
		ADTestCase.result.append(":before cutCA");
	}
}

public cclass AspectD extends AspectB & AspectC
{
	pointcut cutDA() : call(* OuterA+.doA(..));

	before() : cutDA()
	{
		ADTestCase.result.append(":before cutDA");
	}
}

public cclass OuterA
{
	public void doA()
	{
		ADTestCase.result.append(":A.A");
	}
}
