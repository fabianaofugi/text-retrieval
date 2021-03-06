package unb.fga.textretrieval.textretrieval;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import slib.sglib.algo.graph.accessor.GraphAccessor;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

public class OntologiesGraph {

    public static G graph;
    
    private static Map<String, Set<URI>> articles = new HashMap<String, Set<URI>>();
    private static URIFactory factory = URIFactoryMemory.getSingleton();

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    private OntologiesGraph() {
        
    }

    /**
     * Populates the graph with the ontologies and removes the instances.
     * 
     * @param dirPath
     *            Directory containing the ontologies.
     * @throws SLIB_Exception
     */
    public static void loadOntologies(String dirPath) throws SLIB_Exception {
        LOGGER.setLevel(Level.ALL);

        File ontologiesDir = new File(dirPath);
        String[] ontologies = ontologiesDir.list();

        URI graphUri = factory.getURI("http://purl.obolibrary.org/obo/");

        graph = new GraphMemory(graphUri);

        if (ontologies != null) {
            for (String ontology : ontologies) {
                try {
                    GDataConf graphconf = new GDataConf(GFormat.RDF_XML, dirPath + "/" + ontology);
                    GraphLoaderGeneric.populate(graphconf, graph);
                } catch (SLIB_Exception e) {
                    LOGGER.severe(e.getMessage());
                    throw new SLIB_Exception(e);
                }

            }
        } else
            LOGGER.severe(dirPath + " is not a directory.");

        // We create a vertex corresponding to the virtual root
        // and we add it to the graph
        URI virtualRoot = factory.getURI("http://purl.obolibrary.org/obo/virtualRoot");
        graph.addV(virtualRoot);

        // We root the graphs using the virtual root as root
        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", virtualRoot.stringValue());
        GraphActionExecutor.applyAction(factory, rooting, graph);

        Set<URI> instances = new HashSet<URI>();
        instances = GraphAccessor.getInstances(graph);

        graph.removeV(instances);
    }

    /**
     * Creates instances of the articles in the graph.
     * 
     * @param path
     *            File containing the articles IDs and tags referent to the
     *            ontologies classes.
     * @throws SLIB_Exception
     */
    public static void instantiateArticles(String path) throws SLIB_Exception {
        Scanner articlesFile = null;
        try {
            // open the file
            articlesFile = new Scanner(new File(path));
        } catch (FileNotFoundException fnfe) {
            LOGGER.severe(fnfe.getMessage());
            throw new SLIB_Exception(fnfe);
        }

        String line;
        String[] splitline, splitClass;
        String id, className, classId;
        URI idUri;
        Set<URI> classUriSet = new HashSet<URI>();

        while (articlesFile.hasNextLine()) {
            line = articlesFile.nextLine();
            if (line.isEmpty()) {
                articlesFile.close();
                LOGGER.severe("Error at reading articles file");
                return;
            }

            splitline = line.split("\t");
            id = splitline[0];
            idUri = factory.getURI("http://purl.obolibrary.org/obo/id_" + id);
            for (int i = 1; i < splitline.length; i++) {
                if (splitline[i].contains(":") && !splitline[i].contains("http")) {
                    splitClass = splitline[i].split(":");
                    className = splitClass[0];
                    classId = splitClass[1];
                    URI classUri = factory.getURI("http://purl.obolibrary.org/obo/" + className + "_" + classId);
                    if (graph.containsVertex(classUri)) {
                        classUriSet.add(classUri);
                    }
                }
            }

            for (URI f : classUriSet) {
                Edge e = new Edge(idUri, RDF.TYPE, f);
                graph.addE(e);

            }
            if (!classUriSet.isEmpty()) {
                articles.put(id, classUriSet);
            }
            classUriSet = new HashSet<URI>();
        }

        articlesFile.close();
    }

    /**
     * Generates a query with random classes from the graph.
     * 
     * @param numberOfClasses
     *            Number of classes the query must have.
     * @return a set of URIs
     */
    public static Set<URI> generateQuery() {
        Set<URI> concepts = new HashSet<URI>();
        concepts.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_1355"));
        concepts.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_6488"));
        concepts.add(factory.getURI("http://purl.obolibrary.org/obo/EHDA_10251"));
        concepts.add(factory.getURI("http://purl.obolibrary.org/obo/HP_0001263"));
                    
        Set<URI> classes = GraphAccessor.getClasses(graph);
        URI[] classesArray = new URI[classes.size()];
        classes.toArray(classesArray);
        
        LOGGER.info("------Set to be compared with the articles: ");
        for (URI u : concepts) {
            LOGGER.info(u.toString());
        }
        
        return concepts;
    }

    /**
     * Computes the similarity between the set of terms and the articles
     * 
     * @param query
     * @return a TreeMap with the similarity value as key and the article ID as
     *         value, in ascending order.
     * @throws SLIB_Ex_Critic
     */
    public static Map<Double, String> computeSimilarity(Set<URI> query) throws SLIB_Ex_Critic {
        ICconf icConf = new IC_Conf_Corpus("Resnik", SMConstants.FLAG_IC_ANNOT_RESNIK_1995);

        // Then we define the Semantic measure configuration
        SMconf smConf = new SMconf("SimGIC", SMConstants.FLAG_SIM_GROUPWISE_DAG_NTO);
        smConf.setICconf(icConf);

        SM_Engine engine = new SM_Engine(graph);

        double sim;
        Map<Double, String> similarityArticles = new TreeMap<Double, String>();
        for (Map.Entry<String, Set<URI>> e : articles.entrySet()) {
            sim = engine.computeGroupwiseStandaloneSim(smConf, query, e.getValue());
            similarityArticles.put(sim, e.getKey());
        }

        return similarityArticles;
    }
}
