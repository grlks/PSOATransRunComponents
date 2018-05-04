/*
 * Prolog translator.
 * */

package org.ruleml.psoa.psoa2x.psoa2prolog;

import org.antlr.runtime.tree.TreeNodeStream;
import org.ruleml.psoa.PSOAInput;
import org.ruleml.psoa.parser.ParserConfig;
import org.ruleml.psoa.psoa2x.common.*;

public class PrologTranslator extends ANTLRBasedTranslator {
	Config m_config;
	
	public static class Config extends RelationalTranslatorConfig {
		private boolean m_prova_tabling;
		
		public boolean provaTablingEnabled() {
			return m_prova_tabling;
		}
		
		public Config() {
			/* m_prova_tabling must be false when the language is not Prova! */
			this(false);
		}
		
		/**
		 * Constructor for the translator targeting Prova prolog
		 * @param prova_tabling experimental tabling with the cache/1 predicate
		 */
		public Config(boolean prova_tabling) {
			m_prova_tabling = prova_tabling;
		}
	}
	
	public PrologTranslator()
	{
		this(new Config());
	}
	
	public PrologTranslator(Config config)
	{
		m_config = config;
	}

	@Override
	protected <T extends PSOAInput<T>> T normalize(T input) {
		return input.LPnormalize(m_config.getRelationalTransformerConfig());
	}
	
	@Override
	protected Converter createConverter(TreeNodeStream astNodes) {
		return new PrologConverter(astNodes, m_config);
	}

	@Override
	protected ParserConfig getParserConfig() {
		return m_config.getParserConfig();
	}
}
