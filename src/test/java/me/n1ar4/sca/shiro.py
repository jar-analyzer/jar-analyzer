import os
import zipfile
import hashlib

files_to_check = [
    "org/apache/shiro/mgt/AbstractRememberMeManager.class",
    "org/apache/shiro/util/AntPathMatcher.class",
    "org/apache/shiro/config/Ini.class"
]

def calculate_sha256(file_path):
    sha256 = hashlib.sha256()
    with open(file_path, 'rb') as f:
        while True:
            data = f.read(65536)
            if not data:
                break
            sha256.update(data)
    return sha256.hexdigest()

jar_files = [f for f in os.listdir('.') if f.endswith('.jar')]

results = {}

for jar_file in jar_files:
    with zipfile.ZipFile(jar_file, 'r') as jar:
        for file_to_check in files_to_check:
            try:
                with jar.open(file_to_check) as f:
                    file_data = f.read()
                    sha256_hash = hashlib.sha256(file_data).hexdigest()
                    if jar_file not in results:
                        results[jar_file] = {}
                    results[jar_file][file_to_check] = sha256_hash
            except KeyError:
                if jar_file not in results:
                    results[jar_file] = {}
                results[jar_file][file_to_check] = None

for jar_file, hashes in results.items():
    print(f"{jar_file}:")
    for file, sha256_hash in hashes.items():
        print(f"  {file}: {sha256_hash}")

