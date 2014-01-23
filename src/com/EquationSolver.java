package com;

import com.wolfram.alpha.*;

public class EquationSolver {
	private String appid = "TPA3TJ-WHYYGPH2L8";
	public void solveEquation(String equation) {
		WAEngine engine = new WAEngine();
		engine.setAppID(appid);
		engine.addFormat("plaintext");
		WAQuery query = engine.createQuery();
		query.setInput(equation);
		try {
			// For educational purposes, print out the URL we are about to send:
            System.out.println("Query URL:");
            System.out.println(engine.toURL(query));
            System.out.println("");
            
            WAQueryResult queryResult = engine.performQuery(query);
            
            if (queryResult.isError()) {
                System.out.println("Query error");
                System.out.println("  error code: " + queryResult.getErrorCode());
                System.out.println("  error message: " + queryResult.getErrorMessage());
            } else if (!queryResult.isSuccess()) {
                System.out.println("Query was not understood; no results available.");
            } else {
                // Got a result.
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                    	String title = pod.getTitle();
                    	if (title.equals("Result")) {
	                        System.out.println(title);
	                        System.out.println("------------");
	                        for (WASubpod subpod : pod.getSubpods()) {
	                            for (Object element : subpod.getContents()) {
	                                if (element instanceof WAPlainText) {
	                                    System.out.println(((WAPlainText) element).getText());
	                                }
	                            }
	                        }
                    	}
                    }
                }
            }
		} catch (WAException e) {
			e.printStackTrace();
		}
	}
}
