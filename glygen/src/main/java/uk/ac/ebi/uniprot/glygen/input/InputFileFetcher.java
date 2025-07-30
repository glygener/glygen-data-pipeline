package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class InputFileFetcher implements FileFetcher, Callable<List<FileFetchStatus>> {
    private final String dataDirectory;
    private final boolean force;

    public InputFileFetcher(String dataDirectory, boolean force) {
        this.dataDirectory = dataDirectory;
        this.force = force;
    }

    @Override
    public List<FileFetchStatus> call() {
        return fetch();
    }

    @Override
    public List<FileFetchStatus> fetch() {
        SupportDataRdfFetcher supportDataFetcher = new SupportDataRdfFetcher(dataDirectory, force);
        List<FileFetchStatus> result = new ArrayList<>(supportDataFetcher.fetch());

        ProteomeFastaFetcher fastaDataFetcher = new ProteomeFastaFetcher(dataDirectory, force);
        result.addAll(fastaDataFetcher.fetch());

        GenomicCoordinateFetcher gcoordinateFetcher = new GenomicCoordinateFetcher(dataDirectory, force);
        result.addAll(gcoordinateFetcher.fetch());

        EnsemblDataFetcher ensemblDataFetcher = new EnsemblDataFetcher(dataDirectory, force);
        result.addAll(ensemblDataFetcher.fetch());

        RheaRdfFetcher rheaRdfFetcher = new RheaRdfFetcher(dataDirectory, force);
        result.addAll(rheaRdfFetcher.fetch());

        ProteomeRdfFetcher proteomeRdfFetcher = new ProteomeRdfFetcher(dataDirectory, force);
        result.addAll(proteomeRdfFetcher.fetch());

        ReactomeReactionFetcher reactomeReactionFetcher = new ReactomeReactionFetcher(dataDirectory, force);
        result.addAll(reactomeReactionFetcher.fetch());

        VariationNonhumanFetcher variationFetcher = new VariationNonhumanFetcher(dataDirectory, force);
        result.addAll(variationFetcher.fetch());

        IntactDataFetcher intactFetcher = new IntactDataFetcher(dataDirectory, force);
        result.addAll(intactFetcher.fetch());

        // EnsemblGeneFetcher ensemblGeneFetcher = new EnsemblGeneFetcher(dataDirectory, force);
        // result.addAll(ensemblGeneFetcher.fetch());

        GeneDataFetcher geneDataFetcher = new GeneDataFetcher(dataDirectory, force);
        result.addAll(geneDataFetcher.fetch());

        DeletedAccessionFetcher deletedAccessionFetcher = new DeletedAccessionFetcher(dataDirectory, force);
        result.addAll(deletedAccessionFetcher.fetch());

        return result;
    }

}
