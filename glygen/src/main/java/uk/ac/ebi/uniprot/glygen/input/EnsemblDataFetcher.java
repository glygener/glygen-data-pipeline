package uk.ac.ebi.uniprot.glygen.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnsemblDataFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();

    static {
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/homo_sapiens/cds/Homo_sapiens.GRCh38.cds.all.fa.gz", "homo_sapiens.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/homo_sapiens/pep/Homo_sapiens.GRCh38.pep.all.fa.gz", "homo_sapiens.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/mus_musculus/cds/Mus_musculus.GRCm39.cds.all.fa.gz", "mus_musculus.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/mus_musculus/pep/Mus_musculus.GRCm39.pep.all.fa.gz", "mus_musculus.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/rattus_norvegicus/cds/Rattus_norvegicus.GRCr8.cds.all.fa.gz", "rattus_norvegicus.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/rattus_norvegicus/pep/Rattus_norvegicus.GRCr8.pep.all.fa.gz", "rattus_norvegicus.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/drosophila_melanogaster/cds/Drosophila_melanogaster.BDGP6.54.cds.all.fa.gz", "drosophila_melanogaster.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/drosophila_melanogaster/pep/Drosophila_melanogaster.BDGP6.54.pep.all.fa.gz", "drosophila_melanogaster.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/saccharomyces_cerevisiae/cds/Saccharomyces_cerevisiae.R64-1-1.cds.all.fa.gz", "saccharomyces_cerevisiae.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/saccharomyces_cerevisiae/pep/Saccharomyces_cerevisiae.R64-1-1.pep.all.fa.gz", "saccharomyces_cerevisiae.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/sus_scrofa/cds/Sus_scrofa.Sscrofa11.1.cds.all.fa.gz", "sus_scrofa.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/sus_scrofa/pep/Sus_scrofa.Sscrofa11.1.pep.all.fa.gz", "sus_scrofa.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensemblgenomes.ebi.ac.uk/pub/protists/current/fasta/dictyostelium_discoideum/cds/Dictyostelium_discoideum.dicty_2.7.cds.all.fa.gz", "dictyostelium_discoideum.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensemblgenomes.ebi.ac.uk/pub/protists/current/fasta/dictyostelium_discoideum/pep/Dictyostelium_discoideum.dicty_2.7.pep.all.fa.gz", "dictyostelium_discoideum.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/gallus_gallus/cds/Gallus_gallus.bGalGal1.mat.broiler.GRCg7b.cds.all.fa.gz", "gallus_gallus.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/gallus_gallus/pep/Gallus_gallus.bGalGal1.mat.broiler.GRCg7b.pep.all.fa.gz", "gallus_gallus.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensemblgenomes.ebi.ac.uk/pub/plants/current/fasta/arabidopsis_thaliana/cds/Arabidopsis_thaliana.TAIR10.cds.all.fa.gz", "arabidopsis_thaliana.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensemblgenomes.ebi.ac.uk/pub/plants/current/fasta/arabidopsis_thaliana/pep/Arabidopsis_thaliana.TAIR10.pep.all.fa.gz", "arabidopsis_thaliana.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/bos_taurus/cds/Bos_taurus.ARS-UCD2.0.cds.all.fa.gz", "bos_taurus.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/bos_taurus/pep/Bos_taurus.ARS-UCD2.0.pep.all.fa.gz", "bos_taurus.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/danio_rerio/cds/Danio_rerio.GRCz11.cds.all.fa.gz", "danio_rerio.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/danio_rerio/pep/Danio_rerio.GRCz11.pep.all.fa.gz", "danio_rerio.pep.all.fa.gz");

        URL_TO_FILES.put("https://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/latest_assembly_versions/GCF_049306965.1_GRCz12tu/GCF_049306965.1_GRCz12tu_translated_cds.faa.gz", "dGCF_049306965.cds.faa.gz");

        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/cricetulus_griseus_crigri/cds/Cricetulus_griseus_crigri.CriGri_1.0.cds.all.fa.gz", "cricetulus_griseus.cds.all.fa.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/current_fasta/cricetulus_griseus_crigri/pep/Cricetulus_griseus_crigri.CriGri_1.0.pep.all.fa.gz", "cricetulus_griseus.pep.all.fa.gz");

    }

    ;


    public EnsemblDataFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
    }


    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch Ensembl data files");
        return super.fetchAndUncompress(URL_TO_FILES, new DefaultFileDownload(), "gz");
    }

}
