import os
import shutil
import glob

def delete_directory(dir_path):
    if os.path.exists(dir_path) and os.path.isdir(dir_path):
        shutil.rmtree(dir_path)
        print(f"[*] deleted directory: {dir_path}")
    else:
        print(f"[*] directory not found: {dir_path}")

def delete_file(file_path):
    if os.path.exists(file_path) and os.path.isfile(file_path):
        os.remove(file_path)
        print(f"[*] deleted file: {file_path}")
    else:
        print(f"[*] file not found: {file_path}")

def delete_class_files():
    class_files = glob.glob("*.class")
    if class_files:
        for class_file in class_files:
            os.remove(class_file)
            print(f"[*] deleted file: {class_file}")
    else:
        print(f"[*] no .class files found")

paths_to_delete = [
    "agent/target",
    "jar-analyzer-temp",
    "logs",
    "target",
    ".jar-analyzer",
    "agent-jar-with-dependencies.jar",
    "jar-analyzer.db",
    "JAR-ANALYZER-ERROR.txt",
    "jar-analyzer-lockfile"
]

for path in paths_to_delete:
    if os.path.isdir(path):
        delete_directory(path)
    elif os.path.isfile(path):
        delete_file(path)

print(f"[*] jar-analyzer clean")
delete_class_files()
