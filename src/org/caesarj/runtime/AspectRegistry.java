package org.caesarj.runtime;

import java.util.HashSet;

/**
 * The singleton aspect classes (the aspect registries) need to
 * implement this interface.
 * 
 * @author J�rgen Hallpap
 */
public interface AspectRegistry {

	public void $deploy(Deployable aspectToDeploy, Thread thread);

	public void $undeploy();

	public Deployable $getDeployedInstances();
	
	public Deployable $getThreadLocalDeployedInstances();
	
	

	public static ThreadLocal threadLocalRegistries = new ThreadLocal() {

		protected synchronized Object initialValue() {
			return new HashSet();
		}

	};

}
