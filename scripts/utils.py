from pathlib import Path
from typing import Dict


def read_glygen_config_file(config_file: Path) -> Dict:
    data = {}
    current_species = None

    with open(config_file, "r", encoding="utf-8") as f:
        for line in f:
            if line.startswith("#"):
                continue
            line = line.strip()
            if not line or len(line.split("=", 1)) < 2:
                continue
            prefix, value = line.split("=", 1)
            _, key = prefix.split(".", 1)
            if key == "name":
                current_species = value
                data[current_species] = {}
            else:
                if not current_species:
                    continue
                data[current_species][key] = value

    return data
