package unb.fga.textretrieval.textretrieval;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;


public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws SLIB_Exception {
	    LOGGER.setLevel(Level.SEVERE);
	    
		long initialTime = System.currentTimeMillis();
		try {
			OntologiesGraph.loadOntologies("obo-rdf_");
		} catch (SLIB_Exception e) {
			LOGGER.severe(e.getMessage());
			throw new SLIB_Exception(e);
		}

		long loadTime = System.currentTimeMillis();
		
		OntologiesGraph.instantiateArticles("articles.txt");
		TreeMap<Double, String> similarityArticles = new TreeMap<Double, String>();
		
		long instantiateTime = System.currentTimeMillis();
		
		try {
			similarityArticles = OntologiesGraph.computeSimilarity(OntologiesGraph.generateQuery(100));
		} catch (SLIB_Ex_Critic e1) {
		    LOGGER.severe(e1.getMessage());
		    throw new SLIB_Exception(e1);
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
		
		double totalLoadTime = loadTime - initialTime;
		double totalInstantiateTime = instantiateTime - loadTime;
		double totalComputationTime = computationTime - instantiateTime;
		double totalTime = finalTime - initialTime;
		System.out.println("---- Loading time: " + totalLoadTime + "ms");
		System.out.println("---- Instatiation time: " + totalInstantiateTime + "ms");
		System.out.println("---- Computation time: " + totalComputationTime + "ms");
		System.out.println("---- time: " + totalTime + "min");
	}

}
