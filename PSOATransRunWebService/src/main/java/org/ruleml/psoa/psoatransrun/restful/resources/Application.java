package org.ruleml.psoa.psoatransrun.restful.resources;

import static org.ruleml.psoa.psoatransrun.utils.Collections.set;

import java.util.Set;

public class Application extends javax.ws.rs.core.Application {
	private Set<Object> singletons = set();
	private Set<Class<?>> empty = set();

	public Application() {
		// ADD YOUR RESTFUL RESOURCES HERE
		this.singletons.add(new TranslateResource());
		this.singletons.add(new ExecutionResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return this.empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return this.singletons;
	}
}
