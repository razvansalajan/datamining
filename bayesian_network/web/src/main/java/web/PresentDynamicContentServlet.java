package web;

import weka.core.Instances;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresentDynamicContentServlet extends HttpServlet{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// for(Test test : getTestsBean.getTests()){
		// System.out.println(test.getDescription());
		// }
	    List<String> list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			String path = getServletContext().getRealPath("/ski.arff");
			//String path = getServletContext().getRealPath("/contact-lenses.arff");
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
			System.out.println("reader e null");
			return;
		}	
		for(int i=0; i<data.numAttributes(); ++i){
			list.add(data.attribute(i).name() );
		}
		req.setAttribute("attributeNameList", list);
		Map<String, List<String>> values = new HashMap<>();
		for(int i=0; i<data.numAttributes(); ++i){
			List<String> l = new ArrayList<>();
			for(int j=0; j<data.attribute(i).numValues(); ++j){
				l.add(data.attribute(i).value(j));
			}
			values.put(data.attribute(i).name(), l);
		}
		req.setAttribute("attributeValuesNameMap", values);
		RequestDispatcher rq = req.getRequestDispatcher("/WEB-INF/present.jsp");
		rq.forward(req, resp);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
