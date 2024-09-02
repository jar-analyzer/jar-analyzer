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

package me.n1ar4.dbg.parser;

import com.sun.jdi.Location;

import java.util.ArrayList;

public class CoreParser {
    private static String extractOperands(byte[] bytes, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", bytes[start + i]));
        }
        return sb.toString();
    }

    public static String getOpcodeName(int opcode) {
        return OpcodeConst.getOpcodeName(opcode);
    }

    public static int getOpcodeDetail(int opcode) {
        return OpcodeConst.getNoOfOperands(opcode);
    }

    private static MethodObject parse(String className,
                                      String methodName,
                                      String methodDesc,
                                      byte[] bytecode) {
        ArrayList<OpcodeObject> opcodes = new ArrayList<>();
        for (int i = 0; i < bytecode.length; ) {
            int opcode = bytecode[i] & 0xFF;
            String opcodeStr = getOpcodeName(opcode);
            int detail = getOpcodeDetail(opcode);
            int length = detail + 1;
            String operands = null;
            if (length > 1) {
                operands = extractOperands(bytecode, i + 1, length - 1);
            }
            OpcodeObject object = new OpcodeObject();
            object.setOpcode(opcode);
            object.setOpcodeStr(opcodeStr);
            object.setOperands(operands);
            object.setOpcodeIndex(i);
            opcodes.add(object);
            i += length;
        }
        MethodObject methodObject = new MethodObject();
        methodObject.setClassName(className);
        methodObject.setMethodName(methodName);
        methodObject.setMethodDec(methodDesc);
        methodObject.setOpcodes(opcodes);
        return methodObject;
    }

    public static MethodObject parse(Location location) {
        return parse(location.declaringType().name(),
                location.method().name(),
                location.method().signature(),
                location.method().bytecodes());
    }
}
