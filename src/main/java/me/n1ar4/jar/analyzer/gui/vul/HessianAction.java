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

package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class HessianAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getHessianButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // com/caucho/hessian/io/AbstractHessianInput.readObject
            SearchCondition ar = new SearchCondition();
            ar.setClassName("com/caucho/hessian/io/AbstractHessianInput");
            ar.setMethodName("readObject");
            conditions.add(ar);

            // com/caucho/hessian/io/HessianInput.readObject
            SearchCondition hr = new SearchCondition();
            hr.setClassName("com/caucho/hessian/io/HessianInput");
            hr.setMethodName("readObject");
            conditions.add(hr);

            // com/caucho/hessian/io/Hessian2Input.readObject
            SearchCondition h2r = new SearchCondition();
            h2r.setClassName("com/caucho/hessian/io/Hessian2Input");
            h2r.setMethodName("readObject");
            conditions.add(h2r);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}
