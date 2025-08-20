GlyGen Variation TSV Release 2022_05
====================================

* dbSNP-homo-sapiens.tsv.gz
* dbSNP-mus-musculus.tsv.gz
* dbSNP-rattus-norvegicus.tsv.gz
* dbSNP-sars-coronavirus.tsv.gz
* dbSNP-sars-cov-2.tsv.gz


Variation data is from UniProt using API calls:
curl -X GET --header "Accept:application/xml" "https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9606" > UP000005640_9606.dbSnp.xml
curl -X GET --header "Accept:application/xml" "https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10116" > UP000002494_10116.dbSnp.xml
curl -X GET --header "Accept:application/xml" "https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10090" > UP000000589_10090.dbSnp.xml
curl -X GET --header "Accept:application/xml" "https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=2697049" > UP000464024_2697049.dbSnp.xml
curl -X GET --header "Accept:application/xml" "https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=694009" > UP000000354_694009.dbSnp.xml


Variation XML from https://www.ebi.ac.uk/proteins/api/doc/#proteinsApi is parsed to get 30 columns:
> uniprotkb_accession	
> gene_name	
> protein_name	
> data_source  [list of comma separated data source names for given dbsnp_id]
> dbsnp_id	
> cosmic_id
> description  [variant description]
> evidence_ECO:0000269	[list of comma separated variant_evidence_database_name:variant_evidence_database_id for ECO:0000269]
> evidence_ECO:0000313  [list of comma separated variant_evidence_database_name:variant_evidence_database_id for ECO:0000313]
> cytogenic_band
> chromosome_id
> position
> ref_allele
> alt_allele
> ref_aa
> alt_aa
> begin_aa_pos
> end_aa_pos
> frequency
> mutation_type
> polyphen_score
> polyphen_prediction
> sift_score
> sift_prediction
> somatic_status [1 - somatic variant, 0 - germline]
> disease
> disease_description
> disease_xrefs [list of comma separated disease_xref_database:disease_xref_database_id (per disease)]
> disease_evidence_ECO:0000269 [list of comma separated disease_evidence_database_name:disease_evidence_database_id for ECO:0000269]
> disease_evidence_ECO:0000313 [list of comma separated disease_evidence_database_name:disease_evidence_database_id for ECO:0000313]


A variant info is included in dataset only if it has
> valid dbSNP id starting with rs OR valid cosmic id starting with COSM
> valid genomic location starting with NC

Each variant may have zero or more diseases. Each row in dataset corresponds to zero or one disease info with complete variant info.

Note:
1. From 2020_05 release the gnomAD data is obtained directly from the VCF.
2. Exclusion criteria: Not all variant types, only a subset of protein coding variant types that result in a change to the 
	protein sequence are considered. Also, from 2020_05, gnomAD variants from VCF are filtered based on their quality, 
	eg that they are above the recommended thresholds as stated in the VCFs.
3. Data sources: From Ensembl - 1000Genomes, ExAC, gnomAD (combination of both v2 and v3), TOPMed (Trans-Omics for Precision Medicine). 
	Then separately COSMIC (release 84), ClinVar & TCGA
