import re
from glob import glob
from pathlib import Path

pattern = re.compile(r".*/\d{4}_\d{2}$")
release_folders = glob("./releases/*", recursive=False)

filtered_folders = [f for f in release_folders if pattern.match(f) and Path(f).is_dir()]

print(filtered_folders)
