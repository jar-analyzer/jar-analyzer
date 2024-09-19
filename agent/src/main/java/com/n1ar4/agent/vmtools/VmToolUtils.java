/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.n1ar4.agent.vmtools;

import arthas.VmTool;
import com.n1ar4.agent.Agent;

import java.io.*;

/**
 * @author hengyunabc 2021-04-27
 */
@SuppressWarnings("all")
public class VmToolUtils {
    private static String libName = null;
    public static String fileSeparator;
    public static String tmpDir;

    static {
        if (OSUtils.isMac()) {
            libName = "libArthasJniLibrary.dylib";
        }
        if (OSUtils.isLinux()) {
            if (OSUtils.isArm32()) {
                libName = "libArthasJniLibrary-arm.so";
            } else if (OSUtils.isArm64()) {
                libName = "libArthasJniLibrary-aarch64.so";
            } else if (OSUtils.isX86_64()) {
                libName = "libArthasJniLibrary-x64.so";
            } else {
                libName = "libArthasJniLibrary-" + OSUtils.arch() + ".so";
            }
        }
        if (OSUtils.isWindows()) {
            libName = "libArthasJniLibrary-x64.dll";
            if (OSUtils.isX86()) {
                libName = "libArthasJniLibrary-x86.dll";
            }
        }
        fileSeparator = System.getProperty("file.separator");
        tmpDir = System.getProperty("java.io.tmpdir");
    }

    public static String detectLibName() {
        return libName;
    }

    public static String getlibFullTempPath() {
        return tmpDir + fileSeparator + libName;
    }

    public static VmTool getVmToolInstances() {
        String libname = VmToolUtils.detectLibName();
        String libpath = VmToolUtils.getlibFullTempPath();
        File libFile = new File(libpath);
        try {
            InputStream in = Agent.class.getResourceAsStream(String.format("/%s", libname));
            if (in == null) {
                return null;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(libpath);
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (libFile.exists() == false)
            return null; // write File From Resource Failed
        VmTool instance = VmTool.getInstance(libFile.getAbsolutePath());
        return instance;
    }

}
