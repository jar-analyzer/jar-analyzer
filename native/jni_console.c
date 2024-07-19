#include "jni_console.h"

#include <jni.h>
#include <windows.h>

JNIEXPORT void JNICALL Java_me_n1ar4_jar_analyzer_utils_ConsoleUtils_setWindowsColorSupport(
        JNIEnv *env, jclass clazz) {
    HANDLE hStdOut = GetStdHandle(STD_OUTPUT_HANDLE);
    DWORD mode;
    if (!GetConsoleMode(hStdOut, &mode)) {
        return;
    }
    SetConsoleMode(hStdOut, mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
}