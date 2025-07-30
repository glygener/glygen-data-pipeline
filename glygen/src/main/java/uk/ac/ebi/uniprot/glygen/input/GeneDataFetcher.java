package uk.ac.ebi.uniprot.glygen.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class GeneDataFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Map<String, String> URL_TO_FILES = new HashMap<>();

    static {
        URL_TO_FILES.put("https://ftp.ncbi.nlm.nih.gov/genomes/all/GCA/000/004/695/GCA_000004695.1_dicty_2.7/GCA_000004695.1_dicty_2.7_genomic.gff.gz", "44689.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ncbi.nlm.nih.gov/genomes/all/GCA/000/001/735/GCA_000001735.2_TAIR10.1/GCA_000001735.2_TAIR10.1_genomic.gff.gz", "3702.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/009/858/895/GCF_009858895.2_ASM985889v3/GCF_009858895.2_ASM985889v3_genomic.gff.gz", "2697049.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/bos_taurus/Bos_taurus.ARS-UCD1.3.113.chr.gff3.gz", "9913.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/drosophila_melanogaster/Drosophila_melanogaster.BDGP6.46.113.chr.gff3.gz", "7227.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/mus_musculus/Mus_musculus.GRCm39.113.chr.gff3.gz", "10090.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/sus_scrofa/Sus_scrofa.Sscrofa11.1.113.chr.gff3.gz", "9823.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/rattus_norvegicus/Rattus_norvegicus.mRatBN7.2.113.chr.gff3.gz", "10116.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/gallus_gallus/Gallus_gallus.bGalGal1.mat.broiler.GRCg7b.113.chr.gff3.gz", "9031.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/saccharomyces_cerevisiae/Saccharomyces_cerevisiae.R64-1-1.113.gff3.gz", "559292.chr.gff3.gz");
        URL_TO_FILES.put("https://ftp.ensembl.org/pub/release-113/gff3/homo_sapiens/Homo_sapiens.GRCh38.113.chr.gff3.gz", "9606.chr.gff3.gz");
    }

    public GeneDataFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
    }

    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch gff3 data files");
        return fetchAndParse(URL_TO_FILES, new GenericFileDownload("application/x-gzip"), dataDirectory);
    }

    public FileFetchStatus parse(String file, String outputFile) {
        try {
            List<GFFFeature> features = GFFParser.parse(file);
            List<GFFFeature> genes = features.stream().filter(f -> f.getType().equals("gene")).toList();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (GFFFeature gene : genes) {
                    String geneName = gene.getAttribute("Name");
                    String ensgId = gene.getAttribute("gene_id");
                    String chromosome = gene.getSeqname();

                    geneName = geneName != null ? geneName : "";
                    ensgId = ensgId != null ? ensgId : gene.getAttribute("ID") != null ? gene.getAttribute("ID").replace("gene-", "") : "";
                    chromosome = chromosome != null ? chromosome : "";

                    String end = String.valueOf(Math.abs(gene.getLocationEnd()));
                    String start = String.valueOf(Math.abs(gene.getLocationStart()));
                    String strand = gene.getStrand() + "";
                    strand = strand.equals("+") ? "F"  :"R";
                    writer.write(String.join("\t", geneName, ensgId, chromosome, strand, start, end));
                    writer.newLine();
                }
            }
            logger.info("Finished parsing gff3 data files " + outputFile);
            return new FileFetchStatus("", outputFile, FileFetchStatus.FetchStatus.SUCCEEDED);
        } catch (Exception e) {
            logger.error("Fetch failed" + outputFile);
            logger.error(e.getMessage(), e);
            return new FileFetchStatus("", outputFile, FileFetchStatus.FetchStatus.FAILED);
        }
    }

    public List<FileFetchStatus> fetchAndParse(Map<String, String> urlToFiles, GenericFileDownload genericFileDownload, String dataDirectory) {
        List<FileFetchStatus> fileFetchStatuses = super.fetchAndUncompress(URL_TO_FILES, new GenericFileDownload("application/x-gzip"), "gz");
        for (String file : URL_TO_FILES.values()) {
            String gffFile = dataDirectory.concat(File.separator).concat(file.replace(".gz", ""));
            String outputFile = dataDirectory.concat(File.separator).concat(file.split("\\.")[0])//
                    .concat("_gene")//
                    .concat(".tsv");
            File outFile = new File(outputFile);
            if (outFile.exists()) {
                fileFetchStatuses.add(new FileFetchStatus("", outputFile, FileFetchStatus.FetchStatus.SUCCEEDED));
            } else {
                fileFetchStatuses.add(parse(gffFile, outputFile));
            }
        }
        return fileFetchStatuses;
    }
}
