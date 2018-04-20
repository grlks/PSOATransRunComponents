package org.ruleml.psoa.psoatransrun.prova;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import static org.ruleml.psoa.psoatransrun.utils.IOUtil.*;

import org.ruleml.psoa.psoatransrun.AnswerIterator;
import org.ruleml.psoa.psoatransrun.PSOATransRunException;
import org.ruleml.psoa.psoatransrun.QueryResult;
import org.ruleml.psoa.psoatransrun.Substitution;
import org.ruleml.psoa.psoatransrun.engine.ReusableKBEngine;
import org.ruleml.psoa.psoatransrun.engine.EngineConfig;

import ws.prova.api2.ProvaCommunicator;
import ws.prova.api2.ProvaCommunicatorImpl;
import ws.prova.exchange.ProvaSolution;

public class ProvaEngine extends ReusableKBEngine {
	private File m_transKBFile;
	private BufferedReader m_kbBuffer;
	private ProvaCommunicator m_communicator;
	static final String kAgent = "prova";
	static final String kPort = null;
	
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
		// call prova
		m_kbBuffer = new BufferedReader( new StringReader(kb));
		// m_kbBuffer = new StringBuffer(kb);
		try {
			m_communicator = new ProvaCommunicatorImpl(kAgent, kPort, m_kbBuffer, ProvaCommunicatorImpl.SYNC);
		} catch (Exception e) {
			// TODO: process exceptions
		}
	}
	
	public static String getQueryHead(List<String> queryVars) {
		StringBuilder sb = new StringBuilder("query(");
		String prefix = "";
		for (String queryVar : queryVars) {
			sb.append(prefix);
			prefix = ",";
			sb.append(queryVar);
		}
		sb.append(")");
		return sb.toString();
	}

	/*
	private static class PrologAnswerIterator extends AnswerIterator {
		private List<String> m_vars;
		
		public PrologAnswerIterator(ProvaSolution[] solution, List<String> vars) {
			m_solution = solution;
			m_vars = vars;
		}
		
		@Override
		public boolean hasNext() {
			return hasNext();
		}

		@Override
		public Substitution next() {
			return createSubstitution(m_vars, (TermModel)m_iter.next()[0]);
		}

		@Override
		public void dispose() {
			m_iter.cancel();
		}
	}*/
	
	@Override
	public QueryResult executeQuery(String query, List<String> queryVars, boolean getAllAnswers) {
		String queryHead = getQueryHead(queryVars);
		List<ProvaSolution[]> solutions;
		
		StringBuilder sb = new StringBuilder();
		sb.append(queryHead).append(":-").append(query);
		sb.append("\nsolve(").append(queryHead).append(").");
		
		System.out.println(sb);

		BufferedReader provaQuery = new BufferedReader( new StringReader(sb.toString()));
		
		try {
			solutions = m_communicator.consultSync(provaQuery, "dissemination‐rules", new Object[]{});
			// org.junit.Assert.assertEquals(solutions.size(),1);
			
		} catch (Exception e) {
			// TODO: process exceptions
			System.out.println("exception!");
		}
		// TODO: Process solutions
		// Answer: "No"
		QueryResult r = new QueryResult(false);
		return r;
	}



}
