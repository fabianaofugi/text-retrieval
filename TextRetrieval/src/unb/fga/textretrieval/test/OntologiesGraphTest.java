package unb.fga.textretrieval.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;

import slib.sglib.algo.graph.accessor.GraphAccessor;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.utils.ex.SLIB_Exception;
import unb.fga.textretrieval.textretrieval.OntologiesGraph;

public class OntologiesGraphTest {

	@Test
	public void testPopulateGraphSuccess() {
		try {
			OntologiesGraph.loadOntologies("obo-rdf_");
			int result = OntologiesGraph.graph.getNumberVertices();
			int unexpectedResult = 0;
			
			Assert.assertNotEquals(unexpectedResult, result);
		} catch(SLIB_Exception slibe) {
			slibe.printStackTrace();
		}
	}
	
	@Test(expected = SLIB_Exception.class)
	public void testPopulateGraphFailedWithException() throws SLIB_Exception {
			OntologiesGraph.loadOntologies("obo-rdf");
	}
	
	@Test
	public void testInstantiateArticlesSuccess() {
		try {
			OntologiesGraph.loadOntologies("obo-rdf_");
			OntologiesGraph.instantiateArticles("articles.txt");
			
			Set<URI> instances = new HashSet<URI>();
	        instances = GraphAccessor.getInstances(OntologiesGraph.graph);
	        
	        int result = instances.size();
			int expectedResult = 10;
			
			Assert.assertEquals(expectedResult, result);
		} catch (SLIB_Exception slibe) {
			slibe.printStackTrace();
		}
	}
	
	@Test(expected = SLIB_Exception.class)
	public void InstatiateArticlesFailedWithException() throws SLIB_Exception {
		OntologiesGraph.loadOntologies("obo-rdf_");
		OntologiesGraph.instantiateArticles("nonexistent_file");
	}
	
	@Test
	public void testGenerationQuerySuccess() {
		URIFactory factory = URIFactoryMemory.getSingleton();
		Set<URI> expectedResult = new HashSet<URI>();

        expectedResult.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_1355"));
        expectedResult.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_6488"));
        expectedResult.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_10251"));
        expectedResult.add(factory.getURI("http://purl.obolibrary.org/obo/HP_0001263"));
        
        try {
			OntologiesGraph.loadOntologies("obo-rdf_");
			OntologiesGraph.instantiateArticles("articles.txt");
	        Set<URI> result = OntologiesGraph.generateQuery();
	        
	        Assert.assertEquals(expectedResult, result);
		} catch (SLIB_Exception slibe) {
			slibe.printStackTrace();
		}
	}
}
