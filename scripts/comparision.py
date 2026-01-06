import functools
import json
import logging
import re
from glob import glob
from pathlib import Path
from typing import Dict, List

import pandas as pd
import requests
from jinja2 import Environment, FileSystemLoader


def setup_logger(name, log_file, level=logging.DEBUG):
    """Function to set up a logger with a specified name and log file."""
    formatter = logging.Formatter(
        "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    )

    logs_dir = Path("./logs").resolve()
    if not logs_dir.exists():
        logs_dir.mkdir(parents=True, exist_ok=True)
    log_path = logs_dir / log_file

    handler = logging.FileHandler(str(log_path))

    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)

    return logger


logger = setup_logger("comparision", "glygen.log")

config_file: Path = Path("./glygen/src/main/resources/glygenConfig.properties")


def read_stats_files(
    release_folder: Path = None,
):
    """
    Reads and processes statistics files for different species based on a configuration file.

    Args:
        release_folder (Path): Path to the folder containing the statistics JSON files.

    Returns:
        List[Dict[str, Union[str, int]]]: A list of dictionaries, each containing:
            - "specie" (str): Normalized species name.
            - "sequence_n" (int): Number of simple sequences.
            - "annotation_n" (int): Number of glycosylation annotations.

    Notes:
        - More info can be found here: https://www.ebi.ac.uk/seqdb/confluence/pages/viewpage.action?spaceKey=UniProt&title=GlyGen+new+production
        - The configuration file is expected to have keys matching the patterns `\d+\.name` and `\d+\.taxId`.
        - Statistics files are expected to have filenames starting with "statistics" and ending with ".json".
        - The species name is extracted from the filename and normalized using the `get_normalised_specie_name` function.
    """
    config_data = dict(
        zip(
            read_configurations(config_file, r"\d+\.name$"),
            read_configurations(config_file, r"\d+\.taxId$"),
        )
    )
    if not release_folder:
        return config_data
    stats_files = glob.glob(f"{release_folder}/statistics*.json")
    final_stats_data = []
    for stats_file in stats_files:
        specie = str(Path(stats_file).stem).replace("statistics-", "")
        specie_id = config_data.get(specie, "")
        specie_normalised = get_normalised_specie_name(specie_id)

        with open(stats_file, encoding="utf-8") as json_file:
            json_data = json.load(json_file)
            sequence_n = json_data.get("class", {}).get("up:Simple_Sequence", 0)
            annotation_n = json_data.get("class", {}).get(
                "up:Glycosylation_Annotation", 0
            )

            final_stats_data.append(
                {
                    "specie": specie_normalised,
                    "sequence_n": sequence_n,
                    "annotation_n": annotation_n,
                }
            )
    return final_stats_data


def read_configurations(pattern: str) -> List[str]:
    """
    Read and filter configuration values from a file based on a regex pattern.

    This function reads a configuration file, where each line is expected to be in
    the format "key=value". It filters the keys based on the provided regex pattern
    and returns a list of corresponding values.

    Args:
        pattern (str): A regex pattern to match against the keys.

    Returns:
        List[str]: A list of values whose keys match the given pattern.

    Raises:
        FileNotFoundError: If the config_file does not exist.
        PermissionError: If the user doesn't have permission to read the file.
    """
    compiled_pattern = re.compile(pattern)

    with open(config_file, "r", encoding="utf-8") as f:
        return [
            value.strip()
            for line in f
            if "=" in line
            for key, value in [line.split("=", 1)]
            if compiled_pattern.match(key)
        ]


@functools.lru_cache(maxsize=128)
def get_normalised_specie_name(specie_id: str):
    """
    Fetches the scientific name of a species from the EBI Proteins API based on a given taxonomy ID.
    """
    if not specie_id or str(specie_id).strip() == "":
        return None

    url = f"https://www.ebi.ac.uk/proteins/api/taxonomy/id/{specie_id}"

    try:
        response = requests.get(
            url, headers={"Content-Type": "application/json"}, timeout=10
        )
        response.raise_for_status()
        species_data = response.json()
        return species_data["scientificName"]

    except requests.exceptions.RequestException as e:
        logger.error("Error fetching species name for ID %s: %s", id, e)
        return None
    except (KeyError, ValueError) as e:
        logger.error("Error parsing species data for ID %s: %s", id, e)
        return None


def read_dbsnps_from_release(release_folder: str):
    db_files = glob.glob(f"{release_folder}/dbSNP*.gz", recursive=True)
    for db_file in db_files:
        try:
            fname = Path(Path(db_file).stem).stem
            logger.info(f"processing {fname}")
            first_chunk = True
            for chunk in pd.read_csv(
                db_file,
                compression="gzip",
                sep="\t",
                low_memory=False,
                chunksize=100000,
            ):
                chunk["filename"] = Path(db_file).name
                yield chunk  # Yield each chunk as a generator
        except Exception as e:
            print(f"Error processing {db_file}: {e}")


def releases_comparison(release_folder1: Path, release_folder2: Path) -> Dict:
    """
    Compare the data of the last two releases and categorize the differences.

    This function reads statistical data from the last two release files and
    compares them to identify species that are only present in the previous
    release, only present in the actual release, or common to both releases
    with their respective data.

    That function is used to fulfil changelog mail template file.
    That jinja template file is interpolated as the release changelog mail.

    More info can be found here: https://www.ebi.ac.uk/seqdb/confluence/pages/viewpage.action?spaceKey=UniProt&title=GlyGen+new+production

    Args:
        last_two_releases (List[Path]): A list containing paths to the last two
            release files. The first element is the previous release, and the
            second element is the actual release.

    Returns:
        Dict: A dictionary containing three keys:
            - "only_in_previous": A dictionary of species present only in the
              previous release.
            - "only_in_actual": A dictionary of species present only in the
              actual release.
            - "common_entries": A dictionary of species present in both
              releases, with their respective data from the previous and
              actual releases.

    """
    previous = read_stats_files(config_file, release_folder1)
    actual = read_stats_files(config_file, release_folder2)

    previous_by_species = {entry["specie"]: entry for entry in previous}
    actual_by_species = {entry["specie"]: entry for entry in actual}

    only_in_previous = dict(
        sorted(
            {
                specie: entry
                for specie, entry in previous_by_species.items()
                if specie not in actual_by_species
            }.items()
        )
    )

    only_in_actual = dict(
        sorted(
            {
                specie: entry
                for specie, entry in actual_by_species.items()
                if specie not in previous_by_species
            }.items()
        )
    )

    common_entries = {
        specie: {
            "previous": previous_by_species[specie],
            "actual": actual_by_species[specie],
        }
        for specie in sorted(
            set(previous_by_species.keys()) & set(actual_by_species.keys())
        )
    }

    return {
        "only_in_previous": only_in_previous,
        "only_in_actual": only_in_actual,
        "common_entries": common_entries,
    }


@staticmethod
def render(template_file: Path, data: dict) -> str:
    "Renders an HTML template with the provided data."
    env = Environment(loader=FileSystemLoader(Path(template_file).parent))
    template = env.get_template(Path(template_file).name)
    return template.render(data)


if __name__ == "__main__":
    releases_comparison()
