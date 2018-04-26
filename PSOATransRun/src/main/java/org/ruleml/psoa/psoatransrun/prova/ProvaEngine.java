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
import org.ruleml.psoa.psoatransrun.SubstitutionSet;
import org.ruleml.psoa.psoatransrun.engine.ReusableKBEngine;
import org.ruleml.psoa.psoatransrun.engine.EngineConfig;

import ws.prova.api2.ProvaCommunicator;
import ws.prova.api2.ProvaCommunicatorImpl;
import ws.prova.exchange.ProvaSolution;
import ws.prova.parser2.ProvaParsingException;

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
		System.out.println("Experimental Prova support");
		
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
	
	@Override
	public QueryResult executeQuery(String query, List<String> queryVars, boolean getAllAnswers) {
		String queryHead = getQueryHead(queryVars);
		List<ProvaSolution[]> solutions;
		QueryResult r;

		// query already ends with "."
		String queryDef = queryHead + " :- " + query;
		String queryGoal = ":- solve(" + queryHead + ").";
		
		try {
			m_communicator.consultSync(new BufferedReader( new StringReader(queryDef)), "queryDef", new Object[]{});

			solutions = m_communicator.consultSync(new BufferedReader( new StringReader(queryGoal)), "queryGoal", new Object[]{});
			// org.junit.Assert.assertEquals(solutions.size(),1);
			r =  new QueryResult(substitutionsFromSolutions(queryVars, solutions.get(0)));
			
			m_communicator.unconsultSync("queryGoal");
			m_communicator.unconsultSync("queryDef");
		} catch (Exception e) {
			// TODO: process exceptions
			System.out.println("exception!");
			if (e.getCause() != null )
				System.out.println(e.getCause().getLocalizedMessage());
			if (e.getCause() instanceof ProvaParsingException)
				System.out.println(((ProvaParsingException) e.getCause()).getSource());
			r = new QueryResult(false);
		}
		return r;
	}

	private static SubstitutionSet substitutionsFromSolutions(List<String> queryVars, ProvaSolution[] solutions) {
		SubstitutionSet answers = new SubstitutionSet();

		for (ProvaSolution solution : solutions) {
			Substitution answer = substitutionFromSolution(queryVars, solution);
			answers.add(answer);
		}
		return answers;
	}

	private static Substitution substitutionFromSolution(List<String> queryVars, ProvaSolution solution) {
		Substitution answer = new Substitution();
		for(String queryVar : queryVars) {
			String value = solution.getNv(queryVar).toString();
			answer.addPair(queryVar, value);
		}
		return answer;
	}
}
