package unb.fga.textretrieval.test;

import org.junit.Assert;
import org.junit.Test;

import slib.sglib.model.graph.G;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import unb.fga.textretrieval.textretrieval.OntologiesGraph;

public class OntologiesGraphTest {

	@Test
	public void testPopulateGraphSuccess() {
		try {
			OntologiesGraph.loadOntologies("obo-rdf_");
			G result = OntologiesGraph.graph;
			int unexpectedResult = 0;
			
			Assert.assertNotEquals(unexpectedResult, result);
		} catch(SLIB_Ex_Critic e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = SLIB_Ex_Critic.class)
	public void testPopulateGraphFailed() {
		try {
			OntologiesGraph.loadOntologies("obo-rdf");
		} catch(SLIB_Ex_Critic e) {
		}
	}
}
