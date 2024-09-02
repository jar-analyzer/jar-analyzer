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

package me.n1ar4.jar.analyzer.core;

import java.util.ArrayList;
import java.util.List;

public class FinderRunner {
    public static int find(String total, String methodName, int paramNum) {
        // 以第一处方法名索引开始搜索
        for (int i = total.indexOf(methodName);
            // 循环找直到找不到为止
             i >= 0; i = total.indexOf(methodName, i + 1)) {
            // 如果方法名上一位是空格且下一位是字符
            // 认为找到的方法（定义或某些情况的调用）
            if (total.charAt(i - 1) == ' ' &&
                    total.charAt(i + methodName.length()) == '(') {
                // 前第二位是空格这是方法调用
                // 因为第二位如果不是空格那么必然不是方法定义
                if (i - 2 > 0 && total.charAt(i - 2) == ' ') {
                    continue;
                }
                int curNum = 1;
                // 不能使用数组因为不知道具体长度
                List<Character> temp = new ArrayList<>();
                for (int j = i + methodName.length() + 1; ; j++) {
                    temp.add(total.charAt(j));
                    // 遇到结尾
                    if (total.charAt(j) == ')') {
                        // 参数为0个的情况
                        if (total.charAt(j - 1) == '(') {
                            curNum = 0;
                        }
                        // 参数匹配认为找到了
                        if (curNum == paramNum) {
                            return i;
                        } else {
                            if (paramNum > curNum) {
                                // 当参数不足当情况下
                                // 注解个数
                                int atNum = 0;
                                // 右括号默认是-1
                                // 因为参数最终一定以右括号结尾
                                int rightNum = -1;
                                for (Character character : temp) {
                                    if (character == '@') {
                                        // 遇到注解
                                        atNum++;
                                    }
                                    if (character == ')') {
                                        // 遇到右括号
                                        rightNum++;
                                    }
                                }
                                if (atNum == 0) {
                                    // 已经遇到了右括号但不存在注解
                                    // 且参数的数量不匹配
                                    // 这不是预期直接跳出
                                    break;
                                } else {
                                    // 存在注解且右括号正好比注解多一个
                                    if (rightNum == atNum) {
                                        // 认为找到了
                                        return i;
                                    } else if (atNum > rightNum) {
                                        // 没遇到注解和结尾情况应该继续走
                                        continue;
                                    }
                                }
                            }
                        }
                        break;
                    } else if (total.charAt(j) == ',') {
                        // 已遍历参数数量+1
                        curNum++;
                    } else if (total.charAt(j) == '<') {
                        int dNum = -1;
                        while (true) {
                            j++;
                            if (total.charAt(j) == '>') {
                                if (dNum != -1) {
                                    curNum -= dNum;
                                }
                                break;
                            }
                            if (total.charAt(j) == ',') {
                                dNum++;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
