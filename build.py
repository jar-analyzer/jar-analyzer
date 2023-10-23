import os
import shutil
import sys
import subprocess

OLD_VERSION = "2.0-beta"
VERSION = "2.1-beta"
PROJECT = "PROJECT: https://github.com/jar-analyzer/jar-analyzer"


def copy_jar_files(source_dir, target_dir):
    key_word = "-jar-with-dependencies.jar"
    os.makedirs(target_dir, exist_ok=True)
    for root, dirs, files in os.walk(source_dir):
        for file in files:
            if file.endswith(key_word):
                source_path = os.path.join(root, file)
                final_name = file.split(key_word)[0] + ".jar"
                target_path = os.path.join(target_dir, final_name)
                shutil.copy(source_path, target_path)


def replace_version(file_path, old, new):
    with open(file_path, 'r') as file:
        content = file.read()
    updated_content = content.replace(old, new)
    with open(file_path, 'w') as file:
        file.write(updated_content)


if __name__ == '__main__':
    java_target_directory = "target"
    target_directory = "release"
    print("[*] make new release dir: {}".format(VERSION))
    release_system_dir = "jar-analyzer-{}-windows-system".format(VERSION)
    release_embed_dir = "jar-analyzer-{}-windows-embed".format(VERSION)
    subprocess.run("mkdir {}".format(release_system_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_system_dir, "lib"), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}".format(release_embed_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_embed_dir, "lib"), shell=True, cwd=target_directory)
    print("[*] copy file")
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_system_dir, "lib"))
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_embed_dir, "lib"))
    print("[*] fix version string")
    system_ver_file = "build/launch4j-use-system-jre.xml"
    embed_ver_file = "build/launch4j-use-embed-jre.xml"
    replace_version(system_ver_file, OLD_VERSION, VERSION)
    replace_version(embed_ver_file, OLD_VERSION, VERSION)
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_system_dir))
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_embed_dir))
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_system_dir))
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_embed_dir))
    print("[*] prepare build finish")
