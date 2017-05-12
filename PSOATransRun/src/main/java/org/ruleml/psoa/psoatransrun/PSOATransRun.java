package org.ruleml.psoa.psoatransrun;

import static org.ruleml.psoa.utils.IOUtil.println;

import java.io.InputStream;
import java.util.List;

import org.ruleml.psoa.psoa2x.common.*;
import org.ruleml.psoa.psoa2x.psoa2prolog.*;
import org.ruleml.psoa.psoa2x.psoa2tptp.*;
import org.ruleml.psoa.psoatransrun.engine.*;
import org.ruleml.psoa.psoatransrun.prolog.*;
import org.ruleml.psoa.psoatransrun.tptp.*;
import org.ruleml.psoa.psoatransrun.test.Watch;
import org.ruleml.psoa.transformer.TransformerConfig;

/**
 * PSOATransRun system for question answering in PSOA RuleML
 * 
 * */
public class PSOATransRun {
	private Translator m_translator;
	private ExecutionEngine m_engine;
	private String m_transKB;
	private Watch m_translateKBWatch, m_translateQueryWatch, m_executionWatch;
	private boolean m_printTrans;
	
	/***
	 * Construct PSOATransRun instantiation from a translator and an engine
	 * 
	 * @param t   translator from PSOA to a target language L
	 * @param e   execution engine for language L
	 * 
	 * */
	public PSOATransRun(Translator t, ExecutionEngine e)
	{
		m_translator = t;
		m_engine = e;
		
		m_translateKBWatch = new Watch("KB Translation Watch");
		m_translateQueryWatch = new Watch("Query Translation Watch");
		m_executionWatch = new Watch("Execution Watch");
	}
	
	/***
	 * Create PSOATransRun engine from the name of the target language
	 * (currently support "prolog" and "tptp")
	 * 
	 * @param targetLang   target language name
	 * */
	public static PSOATransRun getInstantiation(String targetLang)
	{
		if (targetLang.equalsIgnoreCase("prolog"))
		{
			return new PSOATransRun(new PrologTranslator(), new XSBEngine());
		}
		else if (targetLang.equalsIgnoreCase("tptp"))
		{
			return new PSOATransRun(new ASOTPTPTranslator(), new VampirePrimeEngine());
		}
		
		throw new PSOATransRunException("Unknown target language: " + targetLang);
	}

	
	/***
	 * Create PSOATransRun engine from the name of the target language
	 * (currently support "prolog" and "tptp") and a configuration object
	 * 
	 * @param targetLang   target language name
	 * @param config   configuration of the translator
	 * 
	 * */
	public static PSOATransRun getInstantiation(String targetLang, TransformerConfig config)
	{
		if (targetLang.equalsIgnoreCase("prolog"))
		{
			return new PSOATransRun(new PrologTranslator((PSOA2PrologConfig)config), new XSBEngine(new XSBEngineConfig()));
		}
		else if (targetLang.equalsIgnoreCase("tptp"))
		{
			return new PSOATransRun(new ASOTPTPTranslator((PSOA2TPTPConfig)config), new VampirePrimeEngine(new VampirePrimeEngineConfig()));
		}
		
		throw new PSOATransRunException("Unknown target language: " + targetLang);
	}
	
	/***
	 * Set whether to print translator output for KBs and queries
	 * 
	 * @param printTrans   if set to true, print translator output for KBs and queries
	 * 
	 * */
	public void setPrintTrans(boolean printTrans)
	{
		m_printTrans = printTrans;
	}
	
	/**
	 * Load an input PSOA KB into PSOATransRun. The KB is translated 
	 * into the corresponding target language and prepared in the engine.
	 * 
	 * @param in   input KB stream
	 * 
	 * */
	public void loadKB(InputStream in)
	{
		loadKB(in, false);
	}
	
	/**
	 * Load an input PSOA KB into PSOATransRun. The KB is translated 
	 * into the corresponding target language and prepared in the engine.
	 * 
	 * @param in   input KB string
	 * 
	 * */
	public void loadKB(String kb)
	{
		loadKB(kb, false);
	}
	
	/**
	 * Load an input PSOA KB into PSOATransRun. The KB is translated 
	 * into the corresponding target language and prepared in the engine. 
	 * 
	 * @param in   input KB stream
	 * @param keepTransKB   whether to keep translated KB in the memory
	 * 
	 * */
	public void loadKB(InputStream in, boolean keepTransKB)
	{
		String transKB;

		m_translateKBWatch.start();
		transKB = m_translator.translateKB(in);
		m_translateKBWatch.stop();
		
		loadTranslatedKB(transKB, keepTransKB);
	}
	
	/**
	 * Load an input PSOA KB into PSOATransRun. The input KB is translated 
	 * into the corresponding target language and prepared in the engine. 
	 * 
	 * @param kb   input KB string
	 * @param keepTransKB   whether to keep translated KB in the memory
	 * 
	 * */
	public void loadKB(String kb, boolean keepTransKB)
	{
		String transKB;

		m_translateKBWatch.start();
		transKB = m_translator.translateKB(kb);
		m_translateKBWatch.stop();
		
		loadTranslatedKB(transKB, keepTransKB);
	}
	
	/**
	 * Get the translated KB
	 * 
	 * @return   translated KB
	 * 
	 * */
	public String getTransKB()
	{
		return m_transKB;
	}
	
	private void loadTranslatedKB(String transKB, boolean keepTransKB)
	{
		if (m_printTrans)
		{
			System.out.println("Translated KB:");
			System.out.println(transKB);
		}
		
		if (m_engine instanceof ReusableKBEngine)
		{
			((ReusableKBEngine) m_engine).loadKB(transKB);
			if (keepTransKB)
			{
				m_transKB = transKB;
			}
		}
		else
			m_transKB = transKB;
	}
	
	/**
	 * Execute PSOA query
	 * 
	 * @param query   input stream of PSOA query
	 * 
	 * @return   query result
	 * 
	 * */
	public QueryResult executeQuery(InputStream query)
	{
		String transQuery;
		
		m_translateQueryWatch.start();
		transQuery = m_translator.translateQuery(query);
		m_translateQueryWatch.stop();
		
		return executeTransQuery(transQuery, m_translator.getQueryVars());
	}
	
	/**
	 * Execute PSOA query
	 * 
	 * @param query   PSOA query string
	 * 
	 * @return   query result
	 * 
	 * */
	public QueryResult executeQuery(String query)
	{
		String transQuery;
		
		m_translateQueryWatch.start();
		transQuery = m_translator.translateQuery(query);
		m_translateQueryWatch.stop();
		
		return executeTransQuery(transQuery, m_translator.getQueryVars());
	}
	
	private QueryResult executeTransQuery(String transQuery, List<String> queryVars)
	{
		QueryResult result;
		
		if (m_printTrans)
		{
			println("Translated Query:");
			println(transQuery, ".");
			println();
		}
		
		m_executionWatch.start();
		if (m_engine instanceof ReusableKBEngine)
			result = ((ReusableKBEngine) m_engine).executeQuery(transQuery, queryVars);
		else
			result = m_engine.executeQuery(m_transKB, transQuery, queryVars);
		m_executionWatch.stop();
		
		result.inverseTranslate(m_translator);
		return result;
	}
	
	public void dispose()
	{
		m_engine.shutdown();
	}
	
	/**
	 * Get KB translation time in milliseconds
	 * 
	 * @return   KB translation time
	 * 
	 * */
	public long kbTransTime()
	{
		return m_translateKBWatch.totalMicroSeconds();
	}
	
	/**
	 * Get query translation time in milliseconds
	 * 
	 * @return   query translation time
	 * 
	 * */
	public long queryTransTime()
	{
		return m_translateQueryWatch.totalMicroSeconds();
	}
	
	/**
	 * Get execution time in milliseconds
	 * 
	 * @return   execution time
	 * 
	 * */
	public long executionTime()
	{
		if (m_engine instanceof XSBEngine)
			return ((XSBEngine) m_engine).getTime();
		else
			return m_executionWatch.totalMicroSeconds();
	}
	
	/**
	 * Shutdown execution engine
	 * 
	 * */
	public void shutdown() {
		m_engine.shutdown();
	}
}