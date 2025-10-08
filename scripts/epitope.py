import argparse
import glob
import json
import os
from pathlib import Path

import pandas as pd
import requests


def recursive_flatten(df):
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


def download_epitope_data(epitope_folder):
    """Download epitope data from the given URL and save it in the epitope folder."""

    epitope_folder_path = Path(epitope_folder)
    epitope_folder_path.mkdir(parents=True, exist_ok=True)

    url = "https://www.ebi.ac.uk/proteins/api/epitope?offset=0&size=-1&match_score=1"
    response = requests.get(url)
    epitope_file = epitope_folder_path / "epitope.json"

    with open(epitope_file, "w") as f:
        f.write(response.text)
    print(f"Downloaded epitope data to {epitope_file}")


def load_epitope_data(epitope_folder):
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


def process_db_file(db_file, epitope_df, output_folder):
    """Process each dbSNP file and merge it with epitope data, saving the results in the output folder."""
    try:
        fname = Path(Path(db_file).stem).stem
        print(f"Processing {fname}")

        output_file = Path(output_folder) / f"{fname.replace('dbSNP', 'IEDB')}.tsv"
        first_chunk = True
        chunk_iter = pd.read_csv(
            db_file, compression="gzip", sep="\t", low_memory=False, chunksize=100000
        )

        for chunk in chunk_iter:
            result = chunk.merge(
                epitope_df,
                left_on="uniprotkb_accession",
                right_on="accession",
                how="inner",
            )

            result_sub = result[
                [
                    "uniprotkb_accession",
                    "gene_name",
                    "protein_name",
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
                    "uniprotkb_accession": "uniprotkb_accession",
                    "gene_name": "gene_name",
                    "protein_name": "protein_name",
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
            if first_chunk:
                result_sub.to_csv(output_file, sep="\t", index=False, mode="w")
                first_chunk = False
            else:
                result_sub.to_csv(
                    output_file, sep="\t", index=False, mode="a", header=False
                )

    except Exception as e:
        print(f"Error processing {db_file}: {e}")


def process_release_folder(release_folder, epitope_df):
    """Process all dbSNP files in the release folder and save TSVs in the 'epitope' folder inside it."""

    epitope_folder = Path(release_folder) / "epitope"

    download_epitope_data(epitope_folder)

    db_files = glob.glob(f"{release_folder}/dbSNP*.gz", recursive=True)

    if not db_files:
        print(f"No dbSNP files found in {release_folder}.")

    for db_file in db_files:
        print(f"Processing {db_file}")
        process_db_file(db_file, epitope_df, epitope_folder)


def main():
    """Main function to execute the script."""

    parser = argparse.ArgumentParser(
        description="Process dbSNP files and merge with epitope data."
    )
    parser.add_argument(
        "release_folder",
        type=str,
        help="Path to the release folder containing dbSNP files.",
    )

    args = parser.parse_args()

    release_folder = args.release_folder

    if not os.path.exists(release_folder):
        print(f"Release folder '{release_folder}' does not exist.")
        return

    epitope_folder = Path(release_folder) / "epitope"

    epitope_df = load_epitope_data(epitope_folder)

    process_release_folder(release_folder, epitope_df)
    print("Done")


if __name__ == "__main__":
    main()
