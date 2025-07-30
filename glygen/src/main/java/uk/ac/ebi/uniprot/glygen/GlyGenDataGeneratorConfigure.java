package uk.ac.ebi.uniprot.glygen;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.ArrayList;
import java.util.List;

public class GlyGenDataGeneratorConfigure {

    @Parameter(names = "-configFile", description = "Glygen configure location: glygenConfig.properties")
    private String configFile;

    @Parameter(names = "-input", description = "input file base location, (not include final path: in")
    private String inputBaseDir;

    @Parameter(names = "-output", description = "output data location")
    private String outputDir;

    @Parameter(names = "-species", splitter = CommaParameterSplitter.class, description = "Species list or all")
    private List<String> species = new ArrayList<>();

    private GlyGenDataGeneratorConfigure() {

    }

    public static final GlyGenDataGeneratorConfigure fromCommandLine(String[] args) {
        GlyGenDataGeneratorConfigure configurator = new GlyGenDataGeneratorConfigure();
        new JCommander(configurator, args);
        configurator.updateSpecies();
        return configurator;
    }

    private void updateSpecies() {
        if (species == null) {
            species = new ArrayList<>();
        }
        if (species.isEmpty()) {
            species.add("all");
        }
    }

    public String getReactomeDb() {
        return "neo4j/reactome@bolt://neo4j:7687";
    }

    public String getConfigFile() {
        return configFile;
    }

    public List<String> getSpecies() {
        return species;
    }

    public String getInputBaseDir() {
        return inputBaseDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

}
