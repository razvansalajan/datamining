package web;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import web.com.data_mining.core.BayesianNet;
import weka.core.Instances;

public class BayesianServlet extends HttpServlet{
//	@EJB
//	IBayesianNetworkEjbLocal bayesianNetwork;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// for(Test test : getTestsBean.getTests()){
		// System.out.println(test.getDescription());
		// }
		PrintWriter printWriter = resp.getWriter();
	    Enumeration<String> parameterNames = req.getParameterNames();
	    Map<String, String> initParam = new HashMap<>();
		while (parameterNames.hasMoreElements()) {

			String paramName = parameterNames.nextElement();
			System.out.println(paramName);

			String[] paramValues = req.getParameterValues(paramName);
			initParam.put(paramName, paramValues[0]);
		}
		BufferedReader reader = null;
		try {
			//String path = getServletContext().getRealPath("/ski.arff");
			String path = getServletContext().getRealPath("/contact-lenses.arff");
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (reader == null){
			System.out.println("reader e null");
			return;
		}
		Instances data = null;
		
		try {
			data = new Instances(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (data == null){
			System.out.println("data e null");
			return;
		}
		BayesianNet bayesianNet = new BayesianNet(data);
		bayesianNet.learn();

		List<String> values = new ArrayList<>();
		for(int i=0; i<data.numAttributes(); ++i){
			String currAttributeName = data.attribute(i).name();
			values.add(initParam.get(currAttributeName));
		}
		
		Map<Integer, String> map = new HashMap<>();
		for(int i=0; i<values.size(); ++i){
			map.put(i, values.get(i));
			printWriter.println(i + " " + values.get(i));
		}
		
		 

		printWriter.println(bayesianNet.structureToString());
		double totalProb = 1.0;
		for(int i=0; i<bayesianNet.getNodes().size(); ++i){
			int currNode =  bayesianNet.getNodes().get(i);
			List<Integer> dads =  bayesianNet.getParents(currNode);
			double currProb = bayesianNet.getProb(currNode, values.get(i), map);
			printWriter.println("node:" + i + " " + currProb);
			totalProb *= currProb;
		}
		
		double sum = totalProb;
		for(int i=0; i<data.attribute(bayesianNet.getIndexClass()).numValues(); ++i){
			String currValue = data.attribute(bayesianNet.getIndexClass()).value(i);
			if (currValue.equals (map.get(bayesianNet.getIndexClass()))){
				continue;
			}
			String temp = values.get(bayesianNet.getIndexClass());
			values.set(bayesianNet.getIndexClass(), currValue);
			Double currProbTotal = 1.0;

			for(int j=0; j<bayesianNet.getNodes().size(); ++j){
				int currNode =  bayesianNet.getNodes().get(j);
				List<Integer> dads =  bayesianNet.getParents(currNode);
				double currProb = bayesianNet.getProb(currNode, values.get(j), map);
//				printWriter.println("node:" + j + " " + currProb);
				currProbTotal *= currProb;
			}
			sum += currProbTotal;
			printWriter.println(currValue + " " + currProbTotal);
			values.set(bayesianNet.getIndexClass(), temp);
		}
		
//		for(String s : values){
//			printWriter.println(s);
//		}
		printWriter.println(values.get(bayesianNet.getIndexClass()) + " " + totalProb);
		double converted = (totalProb / sum) * 100.0;
		printWriter.println("converted: " + converted);
	}
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
