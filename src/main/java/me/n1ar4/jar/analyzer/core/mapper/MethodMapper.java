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

package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MethodEntity;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodMapper {
    int insertMethod(List<MethodEntity> method);

    List<MethodResult> selectMethodsByClassName(@Param("className") String className);

    List<MethodResult> selectMethods(@Param("className") String className,
                                     @Param("methodName") String methodName,
                                     @Param("methodDesc") String methodDesc);

    List<MethodResult> selectMethodsLike(@Param("className") String className,
                                         @Param("methodName") String methodName,
                                         @Param("methodDesc") String methodDesc);

    List<MethodResult> selectMethodsByClassNameNoJar(@Param("className") String className);

    int updateMethod(@Param("className") String className,
                     @Param("methodName") String methodName,
                     @Param("methodDesc") String methodDesc,
                     @Param("newItem") String newItem);

    List<MethodResult> selectAllMethods();
}
