/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.cfg;

public enum ControlFlowGraphType {
    NONE,
    ONE_INSN_ONE_BOX,
    MULTI_INSN_ONE_BOX,
    STANDARD,
    EXPERIMENT_ONE_INSN_ONE_BOX,
    EXPERIMENT_MULTI_INSN_ONE_BOX
}
