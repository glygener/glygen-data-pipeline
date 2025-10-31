import argparse
import os
import time
from pathlib import Path
from typing import Optional

import ijson
import requests
from loguru import logger


def parse_variant_file(input_file: Path, output_file: Path, force: bool):
    """Parse variant file from GlyGen and format it for database loading.

    This function processes a JSON input file containing variant data and converts it to a
    tab-separated format with specific columns for variant information. The output includes
    details such as protein accession, genomic location, variant effects, and disease
    associations.

    Parameters
    ----------
    input_file : Path
        Path to the input JSON file containing variant data
    output_file : Path
        Path where the formatted TSV output should be written
    force : bool
        If True, overwrites existing output file. If False, skips processing if output exists

    Returns
    -------
    Path
        Path to the output TSV file

    """

    if os.path.exists(output_file) and not force:
        logger.debug(f"File '{output_file}' already exists. Skipping parsing.")
        return output_file

    logger.debug(f"Parsing {input_file}")
    with open(output_file, "w", encoding="utf-8") as out:
        header = (
            "uniprotkb_accession\tgene_name\tprotein_name\tdata_source\t"
            "dbsnp_id\tcosmic_id\tdescription\tcytogenetic_band\tchromosome_id\t"
            "position\tref_allele\talt_allele\tref_aa\talt_aa\tbegin_aa_pos\tend_aa_pos\t"
            "frequency\tmutation_type\tpolyphen_score\tpolyphen_prediction\t"
            "sift_score\tsift_prediction\tsomatic_status\tdisease\tdisease_description\tdisease_xrefs\n"
        )
        out.write(header)

        with open(input_file, "r", encoding="utf-8") as f:
            parser = ijson.items(f, "item")

            for value in parser:
                try:
                    features = value.get("features", [])
                    if not isinstance(features, list):
                        continue

                    for feature in features:
                        xrefs = feature.get("xrefs", [])
                        if not isinstance(xrefs, list):
                            continue

                        cosmic_xref = next(
                            (
                                r
                                for r in xrefs
                                if r.get("name")
                                and "cosmic" in r["name"].lower()
                                and r.get("id")
                            ),
                            None,
                        )

                        snps = ",".join(
                            sorted(
                                {
                                    r["id"]
                                    for r in xrefs
                                    if r.get("id", "").startswith("rs")
                                }
                            )
                        )
                        descriptions = ",".join(
                            [
                                f"[{';'.join(d['sources'])}]: {d['value']}"
                                for d in feature.get("descriptions", [])
                            ]
                        )
                        data_sources = ",".join(
                            sorted({r.get("name", "") for r in xrefs if r.get("name")})
                        )
                        cytogenetic_band = feature.get("cytogeneticBand", "")

                        genomic_location = feature.get("genomicLocation")
                        chromosome_id, position_str, ref_allele, alt_allele = (
                            "",
                            "",
                            "",
                            "",
                        )

                        if genomic_location:
                            location = (
                                genomic_location[0]
                                if isinstance(genomic_location, list)
                                else genomic_location
                            )
                            parts = location.split(":")
                            if len(parts) > 1:
                                chromosome_id = parts[0]
                                if chromosome_id == "23":
                                    chromosome_id = "X"
                                elif chromosome_id == "24":
                                    chromosome_id = "Y"
                                try:
                                    position_part = parts[1].split(".")[1]
                                    letters = [
                                        i
                                        for i, ch in enumerate(position_part)
                                        if ch.isalpha()
                                    ]
                                    if letters:
                                        position_str = position_part[: letters[0]]
                                    ref_allele = "".join(
                                        filter(
                                            str.isalpha,
                                            parts[1].split(".")[-1].split(">")[0],
                                        )
                                    )
                                    alt_allele = (
                                        "".join(
                                            filter(str.isalpha, parts[1].split(">")[-1])
                                        )
                                        if ">" in parts[1]
                                        else ""
                                    )
                                except Exception as e:
                                    logger.warning(e)
                        else:
                            logger.debug(
                                f'{value.get("accession")} has no genomic location'
                            )
                        frequency = ""
                        if "populationFrequencies" in feature:
                            freqs = feature["populationFrequencies"]
                            frequency = freqs[0].get("frequency", "") if freqs else ""

                        polyphen = next(
                            (
                                p
                                for p in feature.get("predictions", [])
                                if p.get("predAlgorithmNameType") == "PolyPhen"
                            ),
                            None,
                        )
                        sift = next(
                            (
                                p
                                for p in feature.get("predictions", [])
                                if p.get("predAlgorithmNameType") == "SIFT"
                            ),
                            None,
                        )
                        disease = next(
                            (
                                p
                                for p in feature.get("predictions", [])
                                if p.get("predictionValType")
                                and p["predictionValType"] != "benign"
                            ),
                            None,
                        )
                        disease_description = feature.get("association", [{}])[0]

                        if cosmic_xref:
                            line = (
                                "\t".join(
                                    [
                                        str(value.get("accession", "")),
                                        str(value.get("geneName", "")),
                                        str(value.get("proteinName", "")),
                                        data_sources,
                                        snps,
                                        cosmic_xref["id"],
                                        descriptions,
                                        cytogenetic_band,
                                        chromosome_id,
                                        position_str,
                                        ref_allele,
                                        alt_allele,
                                        str(feature.get("wildType", "")),
                                        str(feature.get("mutatedType", "")),
                                        str(feature.get("begin", "")),
                                        str(feature.get("end", "")),
                                        str(frequency),
                                        str(feature.get("consequenceType", "")),
                                        str(polyphen["score"]) if polyphen else "",
                                        (
                                            str(polyphen["predictionValType"])
                                            if polyphen
                                            else ""
                                        ),
                                        str(sift["score"]) if sift else "",
                                        str(sift["predictionValType"]) if sift else "",
                                        str(feature.get("somaticStatus", "")),
                                        (
                                            str(disease["predictionValType"])
                                            if disease
                                            else ""
                                        ),
                                        str(disease_description.get("name", "")),
                                        "",
                                    ]
                                )
                                + "\n"
                            )
                            out.write(line)
                except Exception as err:
                    logger.error(err)

    logger.success(f"Finished processing file. {input_file}")


def download_variation_json(
    taxid: str,
    specie: str,
    output_folder: Path,
    force=False,
    chunk_mb: int = 1,
) -> Optional[Path]:
    """
    Download a species-specific variation JSON file from the UniProt Proteins API and save it to disk.

    This function constructs a UniProt Proteins API URL using the provided taxonomy identifier (taxid),
    requests the full set of variations for that taxon, streams the response to disk as
    "<output_folder>/<specie>.json", and logs download progress. It will create parent directories
    for the target file if they do not already exist.

    Args:
        taxid (str): Taxonomy identifier to filter variations (used in the API query).
        specie (str): Species name used to name the output file (filename will be "<specie>.json").
        output_folder (Path): Directory where the JSON file will be written. The final filename is
            constructed by joining this folder and the specie filename.
        force (bool, optional): If True, overwrite an existing file. If False (default) and the
            destination file already exists, the function will skip downloading and return the
            existing file path.
        chunk_mb (int, optional): Size of each streaming chunk in megabytes (default: 1). Larger
            values may improve throughput at the cost of increased memory usage per chunk.

    Returns:
        Optional[Path]: Path (or path-like string) to the saved JSON file on success, or None if an
        error occurred during download or write. Note: the function catches exceptions internally,
        prints an error message on failure, and returns None rather than propagating the exception.

    """

    url = f"https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid={taxid}"
    logger.debug(f"Downloading {specie} JSON variant file")
    output_filename = f"{output_folder}/{specie}.json"
    output_folder = os.path.dirname(output_filename)
    if output_folder and not os.path.exists(output_folder):
        os.makedirs(output_folder, exist_ok=True)

    if os.path.exists(output_filename) and not force:
        logger.debug(f"File '{output_filename}' already exists. Skipping download.")
        return output_filename

    try:
        response = requests.get(url, timeout=30, stream=True)
        response.raise_for_status()

        total_size = int(response.headers.get("content-length", 0))
        chunk_size = chunk_mb * 1024 * 1024
        downloaded = 0
        last_log_time = 0
        log_interval = 10

        with open(output_filename, "wb") as f:
            for chunk in response.iter_content(chunk_size=chunk_size):
                if chunk:
                    f.write(chunk)
                    downloaded += len(chunk)

                    current_time = time.time()
                    if (
                        current_time - last_log_time > log_interval
                    ) or downloaded == total_size:
                        if total_size:
                            percent = downloaded * 100 // total_size
                            logger.debug(
                                f"Downloaded {percent}% ({downloaded} of {total_size} bytes)"
                            )
                        else:
                            logger.debug(
                                f"Downloaded {specie} JSON variant file {downloaded / (1024 * 1024):.2f} MB so far (total size unknown)"
                            )
                        last_log_time = current_time

        logger.debug(f"Download completed and saved as {output_filename}")
        return output_filename

    except Exception as e:
        print(f"Error: {e}")
        return None


def main():
    """Main function to execute the script."""
    parser = argparse.ArgumentParser(description="Process COSMIC data.")
    parser.add_argument(
        "output_path",
        type=str,
        help="The output path.",
    )
    parser.add_argument(
        "--force",
        type=bool,
        default=False,
        help="Force download.",
    )

    args = parser.parse_args()

    output_folder = args.output_path
    force = args.force

    taxid = "9606"
    specie = "homo-sapiens"
    try:
        json_file = download_variation_json(
            taxid=taxid,
            specie=specie,
            output_folder=output_folder,
            force=force,
        )
        parsed_json_file = f"{output_folder}/{specie}.tsv"
        parse_variant_file(
            input_file=json_file,
            output_file=parsed_json_file,
            force=force,
        )
    except Exception as e:
        logger.exception(e)


if __name__ == "__main__":
    main()
