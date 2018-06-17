package org.ruleml.psoa.psoatransrun.engine;

/**
 * Engines that allow to extend the KB by importing new
 * KB files between queries.
 * 
 */
public abstract class ExtendableKBEngine extends ReusableKBEngine {
	public abstract void loadKB(String kb, String key);
	public abstract void unloadKB(String key);
	
	@Override
	public void loadKB(String kb) {
		loadKB(kb, "");
	}
}
