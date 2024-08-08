import os
import shutil
import sys
import subprocess

VERSION = "2.24"
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


def copy_config_files(target_dir):
    source_path = os.path.join("build", "config.yaml")
    target_path = os.path.join(target_dir, "config.yaml")
    shutil.copy(source_path, target_path)


def copy_exe_files(target_dir):
    source_path = os.path.join("build", "start.exe")
    target_path = os.path.join(target_dir, "start.exe")
    shutil.copy(source_path, target_path)


def copy_file(source_path, destination_path):
    try:
        shutil.copy2(source_path, destination_path)
    except Exception as e:
        print("[!] error: ", str(e))


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
    release_win_system_dir = "jar-analyzer-{}-windows-system".format(VERSION)
    release_win_full_dir = "jar-analyzer-{}-windows-full".format(VERSION)
    release_win_21_dir = "jar-analyzer-{}-windows-21".format(VERSION)
    release_zip_dir = "jar-analyzer-{}".format(VERSION)

    print("[*] make dirs")
    subprocess.run("mkdir {}".format(release_win_system_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_win_system_dir, "lib"), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}".format(release_win_full_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_win_full_dir, "lib"), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}".format(release_win_21_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_win_21_dir, "lib"), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}".format(release_zip_dir), shell=True, cwd=target_directory)
    subprocess.run("mkdir {}\\{}".format(release_zip_dir, "lib"), shell=True, cwd=target_directory)

    print("[*] copy file")
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_win_system_dir, "lib"))
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_win_full_dir, "lib"))
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_win_21_dir, "lib"))
    copy_jar_files(java_target_directory, "{}/{}/{}".format(target_directory, release_zip_dir, "lib"))

    copy_config_files("{}/{}".format(target_directory, release_win_system_dir))
    copy_config_files("{}/{}".format(target_directory, release_win_full_dir))

    copy_exe_files("{}/{}".format(target_directory, release_win_system_dir))
    copy_exe_files("{}/{}".format(target_directory, release_win_full_dir))
    copy_exe_files("{}/{}".format(target_directory, release_win_21_dir))

    print("[*] build start scripts")
    copy_file("build\\start-system.bat", "release\\" + release_win_system_dir + "\\start.bat")
    copy_file("build\\start-full.bat", "release\\" + release_win_full_dir + "\\start.bat")
    copy_file("build\\start-21.bat", "release\\" + release_win_21_dir + "\\start.bat")

    print("[*] build license")
    copy_file("LICENSE", "release\\" + release_win_system_dir + "\\LICENSE")
    copy_file("LICENSE", "release\\" + release_win_full_dir + "\\LICENSE")
    copy_file("LICENSE", "release\\" + release_win_21_dir + "\\LICENSE")
    copy_file("LICENSE", "release\\" + release_zip_dir + "\\LICENSE")

    print("[*] build version")
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_system_dir))
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_full_dir))
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_21_dir))
    subprocess.run("echo {} > {}".format(VERSION, "VERSION.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_zip_dir))

    print("[*] build about")
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_system_dir))
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_full_dir))
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_win_21_dir))
    subprocess.run("echo {} > {}".format(PROJECT, "ABOUT.txt"), shell=True,
                   cwd="{}/{}".format(target_directory, release_zip_dir))

    print("[*] copy build agent.jar")
    copy_file("agent-jar-with-dependencies.jar", "lib\\agent.jar")

    print("[*] copy windows agent.jar")
    copy_file("lib\\agent.jar", "release\\" + release_win_system_dir + "\\lib\\agent.jar")
    copy_file("lib\\agent.jar", "release\\" + release_win_full_dir + "\\lib\\agent.jar")

    print("[*] copy windows tools.jar")
    copy_file("lib\\tools.jar", "release\\" + release_win_system_dir + "\\lib\\tools.jar")
    copy_file("lib\\tools.jar", "release\\" + release_win_full_dir + "\\lib\\tools.jar")

    print("[*] copy windows attach.dll")
    subprocess.run("mkdir {}\\jre\\bin".format(release_win_full_dir), shell=True, cwd=target_directory)
    copy_file("lib\\attach.dll", "release\\" + release_win_full_dir + "\\jre\\bin\\attach.dll")

    # JAVA 21 NOT SUPPORT SHELL ANALYZER

    print("[*] build finish")
