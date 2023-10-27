/*
 * Copyright (C) 2015 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.n1ar4.jar.analyzer.txtmark.cmd;

import me.n1ar4.jar.analyzer.txtmark.BlockEmitter;

import java.io.IOException;
import java.util.List;


final class CodeBlockEmitter implements BlockEmitter
{
    private final String encoding;
    private final String program;

    public CodeBlockEmitter(final String encoding, final String program)
    {
        this.encoding = encoding;
        this.program = program;
    }

    private static void append(final StringBuilder out, final List<String> lines)
    {
        out.append("<pre class=\"pre_no_hl\">");
        for (final String l : lines)
        {
            HlUtils.escapedAdd(out, l);
            out.append('\n');
        }
        out.append("</pre>");
    }

    @Override
    public void emitBlock(final StringBuilder out, final List<String> lines, final String meta)
    {
        if (meta == null || meta.isEmpty())
        {
            append(out, lines);
        }
        else
        {
            try
            {
                out.append(HlUtils.highlight(lines, meta, this.program, this.encoding));
                out.append('\n');
            }
            catch (final IOException e)
            {
                // Ignore or do something, still, pump out the lines
                append(out, lines);
            }
        }
    }
}
