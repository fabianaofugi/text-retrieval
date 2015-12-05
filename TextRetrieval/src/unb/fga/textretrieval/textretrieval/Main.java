package unb.fga.textretrieval.textretrieval;
import java.util.Map;
import java.util.TreeMap;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		long initialTime = System.currentTimeMillis();
		try {
			OntologiesGraph.loadOntologies("obo-rdf");
		} catch (SLIB_Exception e) {
			e.printStackTrace();
		}

		long loadTime = System.currentTimeMillis();
		
		OntologiesGraph.instantiateArticles("pmc-tagged.txt");
		TreeMap<Double, String> similarityArticles = new TreeMap<Double, String>();
		
		long instantiateTime = System.currentTimeMillis();
		
		try {
			similarityArticles = OntologiesGraph.computeSimilarity(OntologiesGraph.generateQuery(100));
		} catch (SLIB_Ex_Critic e1) {
			e1.printStackTrace();
		}
		
		long computationTime = System.currentTimeMillis();
		
		// printing the 10 most similar articles IDs and similarity
        int count = 0;
        for(Map.Entry<Double, String> e : similarityArticles.descendingMap().entrySet()) {
        	count++;
        	System.out.println(e.getValue() + "\t" + e.getKey());
        	if(count == 10)
        		break;
        }
		
		long finalTime = System.currentTimeMillis();
		
		double totalLoadTime = (((loadTime - initialTime)/1000)/60);
		double totalInstantiateTime = (((instantiateTime - loadTime)/1000)/60);
		double totalComputationTime = (((computationTime - instantiateTime)/1000)/60);
		double totalTime = (((finalTime - initialTime)/1000)/60);
		System.out.println("---- Loading time: " + totalLoadTime + "min");
		System.out.println("---- Instatiation time: " + totalInstantiateTime + "min");
		System.out.println("---- Computation time: " + totalComputationTime + "min");
		System.out.println("---- time: " + totalTime + "min");
	}

}
