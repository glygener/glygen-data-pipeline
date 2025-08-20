MAKEFLAGS += --warn-undefined-variables
SHELL := bash
ROOT_DIR := $(shell pwd)

# Define variables for paths
GLYGEN_DIRECTORY := ./glygen
GLYGEN_JAR := glygen-2024.6-SNAPSHOT.jar
INPUT_DIRECTORY := ./glygen/in
OUTPUT_DIRECTORY := ./releases/
NEO4J_DIR := ./neo4j/neo4j-community
REACTOME_DIR := reactome
JAVA_XMS ?= 8g
JAVA_XMX ?= 16g

.PHONY: generate-glygenjar

.PRECIOUS: $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR)
generate-glygenjar: $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR)

$(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR):
	cd $(GLYGEN_DIRECTORY) && mvn clean package -DskipTests -U

.PHONY: download-files

download-files: $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR)
	mkdir -p $(INPUT_DIRECTORY)
	java -cp $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) uk.ac.ebi.uniprot.glygen.GlyGenToolMain $(INPUT_DIRECTORY)

.PHONY: import-triplets

.PRECIOUS: import-triplets
.PRECIOUS: $(INPUT_DIRECTORY)/%.rdf

import-triplets:  $(INPUT_DIRECTORY)/dbSars/dbSars.lock  $(INPUT_DIRECTORY)/dbSarsV2/dbSarsV2.lock  $(INPUT_DIRECTORY)/dbCricetulus/dbCricetulus.lock  $(INPUT_DIRECTORY)/dbHomo/dbHomo.lock  $(INPUT_DIRECTORY)/dbMus/dbMus.lock  $(INPUT_DIRECTORY)/dbRat/dbRat.lock  $(INPUT_DIRECTORY)/dbDroso/dbDroso.lock  $(INPUT_DIRECTORY)/dbSacc/dbSacc.lock  $(INPUT_DIRECTORY)/dbDicty/dbDicty.lock  $(INPUT_DIRECTORY)/dbSus/dbSus.lock  $(INPUT_DIRECTORY)/dbGal/dbGal.lock  $(INPUT_DIRECTORY)/dbAra/dbAra.lock  $(INPUT_DIRECTORY)/dbHcv1a/dbHcv1a.lock  $(INPUT_DIRECTORY)/dbHcv1b/dbHcv1b.lock  $(INPUT_DIRECTORY)/dbBos/dbBos.lock  $(INPUT_DIRECTORY)/dbDanio/dbDanio.lock 


$(INPUT_DIRECTORY)/UP000000354_694009_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbSars/dbSars.lock: $(INPUT_DIRECTORY)/UP000000354_694009_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbSars
	tdbloader --loc $(INPUT_DIRECTORY)/dbSars $<
	touch $@

$(INPUT_DIRECTORY)/UP000464024_2697049_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbSarsV2/dbSarsV2.lock: $(INPUT_DIRECTORY)/UP000464024_2697049_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbSarsV2
	tdbloader --loc $(INPUT_DIRECTORY)/dbSarsV2 $<
	touch $@

$(INPUT_DIRECTORY)/UP000001075_10029_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbCricetulus/dbCricetulus.lock: $(INPUT_DIRECTORY)/UP000001075_10029_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbCricetulus
	tdbloader --loc $(INPUT_DIRECTORY)/dbCricetulus $<
	touch $@

$(INPUT_DIRECTORY)/UP000005640_9606_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbHomo/dbHomo.lock: $(INPUT_DIRECTORY)/UP000005640_9606_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbHomo
	tdbloader --loc $(INPUT_DIRECTORY)/dbHomo $<
	touch $@

$(INPUT_DIRECTORY)/UP000000589_10090_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbMus/dbMus.lock: $(INPUT_DIRECTORY)/UP000000589_10090_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbMus
	tdbloader --loc $(INPUT_DIRECTORY)/dbMus $<
	touch $@

$(INPUT_DIRECTORY)/UP000002494_10116_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbRat/dbRat.lock: $(INPUT_DIRECTORY)/UP000002494_10116_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbRat
	tdbloader --loc $(INPUT_DIRECTORY)/dbRat $<
	touch $@

$(INPUT_DIRECTORY)/UP000000803_7227_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbDroso/dbDroso.lock: $(INPUT_DIRECTORY)/UP000000803_7227_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbDroso
	tdbloader --loc $(INPUT_DIRECTORY)/dbDroso $<
	touch $@

$(INPUT_DIRECTORY)/UP000002311_559292_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbSacc/dbSacc.lock: $(INPUT_DIRECTORY)/UP000002311_559292_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbSacc
	tdbloader --loc $(INPUT_DIRECTORY)/dbSacc $<
	touch $@

$(INPUT_DIRECTORY)/UP000002195_44689_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbDicty/dbDicty.lock: $(INPUT_DIRECTORY)/UP000002195_44689_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbDicty
	tdbloader --loc $(INPUT_DIRECTORY)/dbDicty $<
	touch $@

$(INPUT_DIRECTORY)/UP000008227_9823_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbSus/dbSus.lock: $(INPUT_DIRECTORY)/UP000008227_9823_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbSus
	tdbloader --loc $(INPUT_DIRECTORY)/dbSus $<
	touch $@

$(INPUT_DIRECTORY)/UP000000539_9031_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbGal/dbGal.lock: $(INPUT_DIRECTORY)/UP000000539_9031_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbGal
	tdbloader --loc $(INPUT_DIRECTORY)/dbGal $<
	touch $@

$(INPUT_DIRECTORY)/UP000006548_3702_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbAra/dbAra.lock: $(INPUT_DIRECTORY)/UP000006548_3702_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbAra
	tdbloader --loc $(INPUT_DIRECTORY)/dbAra $<
	touch $@

$(INPUT_DIRECTORY)/UP000000518_63746_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbHcv1a/dbHcv1a.lock: $(INPUT_DIRECTORY)/UP000000518_63746_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbHcv1a
	tdbloader --loc $(INPUT_DIRECTORY)/dbHcv1a $<
	touch $@

$(INPUT_DIRECTORY)/UP000008095_11116_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbHcv1b/dbHcv1b.lock: $(INPUT_DIRECTORY)/UP000008095_11116_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbHcv1b
	tdbloader --loc $(INPUT_DIRECTORY)/dbHcv1b $<
	touch $@

$(INPUT_DIRECTORY)/UP000009136_9913_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbBos/dbBos.lock: $(INPUT_DIRECTORY)/UP000009136_9913_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbBos
	tdbloader --loc $(INPUT_DIRECTORY)/dbBos $<
	touch $@

$(INPUT_DIRECTORY)/UP000000437_7955_uniprot_proteome.rdf:
	mkdir -p $(INPUT_DIRECTORY)
	$(SHELL) ./scripts/download_files.sh $(INPUT_DIRECTORY)/ $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) $@

$(INPUT_DIRECTORY)/dbDanio/dbDanio.lock: $(INPUT_DIRECTORY)/UP000000437_7955_uniprot_proteome.rdf
	mkdir -p $(INPUT_DIRECTORY)/dbDanio
	tdbloader --loc $(INPUT_DIRECTORY)/dbDanio $<
	touch $@


$(REACTOME_DIR)/reactome.graphdb.tgz:
	rm -rf $(REACTOME_DIR)
	mkdir -p $(REACTOME_DIR)
	curl --retry 3 --retry-delay 3 --retry-max-time 0 --retry-all-errors -C - -o "$@" "https://reactome.org/download/current/reactome.graphdb.tgz"

$(REACTOME_DIR)/graph.db/reactome.lock: $(REACTOME_DIR)/reactome.graphdb.tgz
	tar -xzf $< -C $(REACTOME_DIR)
	touch $@

.PHONY: setup-reactome

setup-reactome: ./neo4j/data/databases/graph.db/reactome.lock
./neo4j/data/databases/graph.db/reactome.lock: $(REACTOME_DIR)/graph.db/reactome.lock
	mkdir -p ./neo4j/conf/
	mkdir -p ./neo4j/data/databases/
	cp -rf conf/neo4j.conf ./neo4j/conf/
	rm -rf ./neo4j/data/databases/graph.db
	cp -rf $(REACTOME_DIR)/graph.db ./neo4j/data/databases/graph.db



.PHONY: generate-data
generate-data: $(OUTPUT_DIRECTORY)/data_generation.lock

$(OUTPUT_DIRECTORY)/data_generation.lock: $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR)
	$(MAKE) setup-reactome
	$(MAKE) import-triplets
	mkdir -p $(OUTPUT_DIRECTORY)/2025_06
	java -Xms4g -Xmx24g \
	-cp $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR) uk.ac.ebi.uniprot.glygen.GlygenDataGeneratorMain \
	-input $(GLYGEN_DIRECTORY) \
	-output $(OUTPUT_DIRECTORY)/2025_06 \
	-configFile $(GLYGEN_DIRECTORY)/src/main/resources/glygenConfig.properties
	touch $@

.PHONY: generate-other-data
generate-other-data: $(OUTPUT_DIRECTORY)/other_data_generation.lock

$(OUTPUT_DIRECTORY)/other_data_generation.lock: $(GLYGEN_DIRECTORY)/target/$(GLYGEN_JAR)
	mkdir -p $(OUTPUT_DIRECTORY)/2025_06
	$(MAKE) setup-reactome
	$(MAKE) import-triplets
	cd $(GLYGEN_DIRECTORY) && java -Xms4g -Xmx24g \
	-cp ./target/$(GLYGEN_JAR) uk.ac.ebi.uniprot.glygen.GlyGenOtherDataGeneratorMain ../$(GLYGEN_DIRECTORY) ../$(OUTPUT_DIRECTORY)/2025_06
	touch $@

$(OUTPUT_DIRECTORY)/2025_06:
	mkdir -p $@

.PHONY: all
all:
	$(MAKE) generate-glygenjar #Calling individual goals to be able to import triplets simultaneously
	$(MAKE) download-files #Calling individual goals to be able to import triplets simultaneously
	$(MAKE) import-triplets
	$(MAKE) generate-data
	$(MAKE) generate-other-data

.PHONY: clean

clean:
	rm -rfv $(INPUT_DIRECTORY)
	rm -rfv $(GLYGEN_DIRECTORY)/target
	rm -rfv $(OUTPUT_DIRECTORY)/data_generation.lock
	rm -rfv $(OUTPUT_DIRECTORY)/other_data_generation.lock
	rm -rfv $(REACTOME_DIR)
	rm -rfv ./logs
	rm -rfv ./neo4j
