/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * Utility class for handling corrupted StackMapTable in class files.
 * When EXPAND_FRAMES throws IndexOutOfBoundsException, this handler
 * automatically falls back to SKIP_FRAMES mode and tracks corrupted files.
 */
public class StackMapFrameHandler {

    /**
     * Handle IndexOutOfBoundsException in class file parsing.
     * If caused by corrupted StackMapTable, fallback to SKIP_FRAMES mode.
     *
     * @param file    The class file entity being parsed
     * @param visitor The ClassVisitor to reuse for fallback parsing
     * @param logger  The logger for warning messages
     * @param context The context description for logging (e.g., "class discovery", "spring analysis")
     * @param e       The IndexOutOfBoundsException that was caught
     * @return true if fallback parsing succeeded, false otherwise
     */
    public static boolean handleParseException(ClassFileEntity file, ClassVisitor visitor, Logger logger, String context, IndexOutOfBoundsException e) {
        if (e.getMessage() != null && e.getMessage().contains("out of bounds")) {
            String warnMsg = String.format("[StackMapTable Corrupted] %s - invalid offset: %s in %s",
                    context, e.getMessage(), file.getJarName());
            logger.warn(warnMsg);
            // Also output to GUI log panel with yellow color
            LogUtil.warn(warnMsg);
            try {
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(visitor, Const.FallbackASMOptions);
                String successMsg = String.format("Successfully re-parsed with SKIP_FRAMES mode: %s", file.getJarName());
                logger.info(successMsg);
                LogUtil.info(successMsg);
                // Track corrupted file for popup notification
                AnalyzeEnv.corruptedFiles.add(file.getJarName() + "!" + file.getClassName() +
                        " [" + e.getMessage() + "]");
                return true;
            } catch (Exception fallbackEx) {
                String errorMsg = String.format("Failed to re-parse with SKIP_FRAMES mode: %s", file.getJarName());
                logger.error(errorMsg, fallbackEx);
                LogUtil.error(errorMsg + " - " + fallbackEx.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Handle IndexOutOfBoundsException in class file parsing (byte array version).
     */
    public static boolean handleParseException(byte[] classBytes, ClassVisitor visitor, String fileInfo, Logger logger, String context, IndexOutOfBoundsException e) {
        if (e.getMessage() != null && e.getMessage().contains("out of bounds")) {
            String warnMsg = String.format("[StackMapTable Corrupted] %s - invalid offset: %s in %s",
                    context, e.getMessage(), fileInfo);
            logger.warn(warnMsg);
            // Also output to GUI log panel with yellow color
            LogUtil.warn(warnMsg);
            try {
                ClassReader cr = new ClassReader(classBytes);
                cr.accept(visitor, Const.FallbackASMOptions);
                // Track corrupted file for popup notification
                AnalyzeEnv.corruptedFiles.add(fileInfo + " [" + e.getMessage() + "]");
                return true;
            } catch (Exception fallbackEx) {
                String errorMsg = String.format("Failed to re-parse with SKIP_FRAMES mode: %s", fileInfo);
                logger.error(errorMsg, fallbackEx);
                LogUtil.error(errorMsg + " - " + fallbackEx.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Check if an exception is caused by corrupted StackMapTable.
     */
    public static boolean isCorruptedStackMapTable(IndexOutOfBoundsException e) {
        return e.getMessage() != null && e.getMessage().contains("out of bounds");
    }

    /**
     * Track a corrupted file for popup notification.
     */
    public static void trackCorruptedFile(String jarName, String className, String errorMessage) {
        AnalyzeEnv.corruptedFiles.add(jarName + "!" + className + " [" + errorMessage + "]");
    }
}
