package org.ruleml.psoa.psoatransrun.prolog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.ruleml.psoa.psoatransrun.utils.IOUtil.*;

import org.ruleml.psoa.psoatransrun.PSOATransRunException;
import org.ruleml.psoa.psoatransrun.QueryResult;
import org.ruleml.psoa.psoatransrun.engine.ReusableKBEngine;
import org.ruleml.psoa.psoatransrun.engine.EngineConfig;

public class ProvaEngine extends ReusableKBEngine {
	private File m_transKBFile;
	
	/**
	 * prova engine configuration
	 * */
	public static class Config extends EngineConfig {
	}

	public ProvaEngine() {
		this(new Config());
	}
	
	public ProvaEngine(Config config) {
		this(config, false);
	}
	
	/**
	 * Initialize ProvaEngine
	 * 
	 * @param config        the configuration
	 * @param delayStart    if true, start the engine at initialization time; otherwise, start the engine when KB is loaded
	 * */
	public ProvaEngine(Config config, boolean delayStart) {
		System.out.println("Prova demo.");
		System.out.println("Every query will be answered with No!");
		System.out.println("Prova is not used yet, try to enter KB and queries manually into prova.");
		
		// Set translated KB
		String transKBPath = config.transKBPath;
		try
		{
			if (transKBPath != null)
			{
				if (!transKBPath.endsWith(".prova"))
					throw new PSOATransRunException("Prova translation output file name must end with .prova: " + transKBPath);
				m_transKBFile = new File(transKBPath);
				m_transKBFile.createNewFile();
			}
			else
				m_transKBFile = tmpFile("tmp-", ".prova");
		}
		catch (IOException e)
		{
			throw new PSOATransRunException(e);
		}
	}

	@Override
	public String language() {
		return "prolog";
		// return "prova";
	}

	@Override
	public void loadKB(String kb) {
		try(PrintWriter writer = new PrintWriter(m_transKBFile))
		{
			writer.print(kb);
		}
		catch (FileNotFoundException e)
		{
			throw new PSOATransRunException(e);
		}
		// TODO: call prova
	}

	@Override
	public QueryResult executeQuery(String query, List<String> queryVars, boolean getAllAnswers) {
		// Answer: "No"
		QueryResult r = new QueryResult(false);
		return r;
	}



}
