package uk.ac.ebi.uniprot.glygen.util;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.uniprot.glygen.appender.ClassificationAppender;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UP_TYPE_PROTEIN;

public class TestUtility {

    public static final String TEST_RESOURCE_PATH =
            TestUtility.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    public static String getTestResourcePath(String fileName) {
        return TEST_RESOURCE_PATH + fileName;
    }

    public static Model getTestRdfModel() {
        String rdfFile = getTestResourcePath(("in/testRdf.rdf"));
        Model rdfModel = ModelFactory.createDefaultModel();
        InputStream in = RDFDataMgr.open(rdfFile);
        if (in == null) {
            throw new IllegalArgumentException("File: " + rdfFile + " not found");
        }
        rdfModel.read(in, null);
        return rdfModel;
    }

    public static  void writeOutputModel(Model outModel) {
        try (FileOutputStream out = new FileOutputStream(getTestResourcePath("out/tempOutput.n3"))) {
            RDFDataMgr.write(out, outModel, Lang.N3);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void addProteinToOutModel(Model outModel, String url) {
        Resource protein = outModel.createResource(url);
        protein.addProperty(RDF.type, outModel.createResource(UP_TYPE_PROTEIN));
    }

    public static Model getDefaultTestOutModel() {
        Model outModel = ModelFactory.createDefaultModel();
        addProteinToOutModel(outModel, "http://purl.uniprot.org/uniprot/P05067");
        addProteinToOutModel(outModel, "http://purl.uniprot.org/uniprot/A0A0A0MRG2");
        addProteinToOutModel(outModel, "http://purl.uniprot.org/uniprot/B3KU38");
        addProteinToOutModel(outModel, "http://purl.uniprot.org/uniprot/A0A0J9YW22");
        addProteinToOutModel(outModel, "http://purl.uniprot.org/uniprot/Q3LFU0");
        return outModel;
    }

    public static Set<String> getDefaultTestAccessionSet() {
        Set<String> accSet = new HashSet<>();
        accSet.add("http://purl.uniprot.org/uniprot/P05067");
        accSet.add("http://purl.uniprot.org/uniprot/A0A0A0MRG2");
        accSet.add("http://purl.uniprot.org/uniprot/B3KU38");
        accSet.add("http://purl.uniprot.org/uniprot/A0A0J9YW22");
        accSet.add("http://purl.uniprot.org/uniprot/Q3LFU0");
        return accSet;
    }

    public static ClassificationAppender getDefaultClassificationAppender() {
        GlygenConfig.setKeywords("in/testKeywords.rdf");
        GlygenConfig.setGeneOntologies("in/testGo.owl");
        return new ClassificationAppender();
    }

    public static void cleanUp(String name) throws IOException {
        File dir = new File(name);
        if (dir.exists() && dir.isDirectory()) {
            FileUtils.cleanDirectory(dir);
        }
        if (dir.exists()) {
            dir.delete();
        }
    }
}
