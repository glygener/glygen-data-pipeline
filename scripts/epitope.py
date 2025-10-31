import argparse
import json
import os
from pathlib import Path
from typing import Dict

import pandas as pd
import requests
from utils import read_glygen_config_file


def recursive_flatten(df: pd.DataFrame) -> pd.DataFrame:
    """Flatten nested columns in the dataframe."""
    while True:
        list_cols = [
            col
            for col in df.columns
            if df[col].apply(lambda x: isinstance(x, list)).any()
        ]
        if not list_cols:
            break
        for col in list_cols:
            print(f"Flattening {col}")
            df = df.explode(col).reset_index(drop=True)
            if df[col].apply(lambda x: isinstance(x, dict)).any():
                normalized = pd.json_normalize(df[col]).add_prefix(f"{col}.")
                df = df.drop(columns=[col]).join(normalized)
    return df


def download_epitope_data(epitope_folder: Path):
    """Download epitope data from the given URL and save it in the epitope folder."""

    epitope_folder_path = Path(epitope_folder)
    epitope_folder_path.mkdir(parents=True, exist_ok=True)

    url = "https://www.ebi.ac.uk/proteins/api/epitope?offset=0&size=-1&match_score=1"
    response = requests.get(url)
    epitope_file = epitope_folder_path / "epitope.json"

    with open(epitope_file, "w") as f:
        f.write(response.text)
    print(f"Downloaded epitope data to {epitope_file}")


def load_epitope_data(epitope_folder: Path) -> pd.DataFrame:
    """Load epitope data from the 'epitope.json' file inside the given epitope folder."""
    epitope_file = Path(epitope_folder) / "epitope.json"
    if not epitope_file.exists():
        print(f"Epitope data file '{epitope_file}' not found. Downloading it now...")
        download_epitope_data(epitope_folder)

    with open(epitope_file) as f:
        print("Loading epitope data")
        data = json.load(f)
        df = pd.json_normalize(data)
        print("Flattening epitope data")
        epitope_df = recursive_flatten(df)
    return epitope_df


def process_specie(
    specie: str,
    specie_dict: Dict,
    epitope_df: pd.DataFrame,
    output_folder: Path,
) -> pd.DataFrame:
    """Process species and extracts its epitope data, saving the results in the output folder."""
    try:
        print(f"Processing {specie}")

        output_file = Path(output_folder) / f"IEDB-{specie}.tsv"

        taxid = specie_dict.get("taxId")
        if not taxid or not taxid.isnumeric():
            print(f"Error during the process of {specie}")
            return

        result_sub = epitope_df[epitope_df["taxid"] == int(taxid)]

        result_sub = result_sub[
            [
                "accession",
                "features.description",
                "features.xrefs.name",
                "features.xrefs.id",
                "features.begin",
                "features.end",
                "features.epitopeSequence",
                "features.evidences.code",
                "features.evidences.source.name",
                "features.evidences.source.id",
            ]
        ]

        result_sub = result_sub.drop_duplicates()
        result_sub = result_sub.rename(
            columns={
                "accession": "uniprotkb_accession",
                "features.description": "epitope_description",
                "features.xrefs.name": "data_source",
                "features.xrefs.id": "iedb_id",
                "features.begin": "begin_aa_pos",
                "features.end": "end_aa_pos",
                "features.epitopeSequence": "epitope_sequence",
                "features.evidences.code": "evidence_code",
                "features.evidences.source.name": "evidence_source",
                "features.evidences.source.id": "evidence_source_id",
            }
        )

        # Write to file, append after first chunk
        result_sub.to_csv(output_file, sep="\t", index=False, mode="w")

    except Exception as e:
        print(f"Error processing {specie}: {e}")


def generate_iedb(
    output_path: Path,
    epitope_df: pd.DataFrame,
    config_data: Dict,
):
    """
    Generates IEDB epitope data files for multiple species based on the provided configuration and epitope DataFrame.

    This function creates an output folder for epitope data, downloads necessary epitope resources,
    and processes each species defined in the configuration data to generate species-specific epitope files.

    Args:
        output_path (Path): The base directory where the epitope data will be stored.
        epitope_df (pd.DataFrame): DataFrame containing epitope information to be processed.
        config_data (Dict): Dictionary mapping species names to their configuration dictionaries.

    """
    epitope_folder = Path(output_path) / "epitope"

    download_epitope_data(epitope_folder)

    for specie, specie_dict in config_data.items():
        process_specie(specie, specie_dict, epitope_df, epitope_folder)


def main():
    """Main function to execute the script."""

    parser = argparse.ArgumentParser(
        description="Process dbSNP files and merge with epitope data."
    )
    parser.add_argument(
        "output_path",
        type=str,
        help="The output path.",
    )

    args = parser.parse_args()

    output_path = args.output_path

    if not os.path.exists(output_path):
        print(f"Folder '{output_path}' does not exist.")
        return

    epitope_folder = Path(output_path) / "epitope"

    epitope_df = load_epitope_data(epitope_folder)

    config_file = "./glygen/src/main/resources/glygenConfig.properties"
    config_data = read_glygen_config_file(config_file)

    generate_iedb(output_path, epitope_df, config_data)
    print("Done")


if __name__ == "__main__":
    main()
