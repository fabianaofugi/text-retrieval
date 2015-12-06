package unb.fga.textretrieval.test;

import org.junit.Assert;
import org.junit.Test;

import slib.sglib.model.graph.G;
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
		} catch(SLIB_Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = SLIB_Exception.class)
	public void testPopulateGraphFailed() throws SLIB_Exception {
			OntologiesGraph.loadOntologies("obo-rdf");
	}
}
