"""
put this file in the folder that you want to rename and run the script
all the files will be renamed with ".txt" at the end of its file name
all the files in the current directory and its subdirectories will be renamed as well
"""
import os


def main():
    for path, subdirs, files in os.walk('.'):
        for file_name in files:
            new_file_path = os.path.join(path, file_name)
            new_file_name = os.path.join(path, file_name + ".txt")
            os.rename(new_file_path, new_file_name)

    if os.path.exists("./rename_to_.txt.py.txt"):
        os.remove("./rename_to_.txt.py.txt")
    else:
        print("renaming process is not done successfully")


if __name__ == '__main__':
    main()
