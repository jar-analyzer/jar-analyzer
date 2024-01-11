package me.n1ar4.jar.analyzer.utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

public class ConsoleUtils {
    private static final Kernel32 KERNEL32 = Native.load("kernel32", Kernel32.class);
    private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;

    public static void setWindowsColorSupport() {
        HANDLE hStdOut = KERNEL32.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
        IntByReference pMode = new IntByReference(0);
        if (!KERNEL32.GetConsoleMode(hStdOut, pMode)) {
            return;
        }
        int mode = pMode.getValue();
        int newMode = mode | ENABLE_VIRTUAL_TERMINAL_PROCESSING;
        KERNEL32.SetConsoleMode(hStdOut, newMode);
    }
}
