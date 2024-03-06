windres resource.rc -O coff -o resource.o
gcc main.c resource.o -o start.exe