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
package com.github.rjeschke.txtmark.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class HlUtils
{
    final static AtomicInteger IN_COUNT  = new AtomicInteger(0);
    final static AtomicInteger OUT_COUNT = new AtomicInteger(0);
    final static long          ID        = System.nanoTime();

    public static String highlight(final List<String> lines, final String meta, final String prog, final String encoding)
            throws IOException
    {
        final File tmpIn = new File(System.getProperty("java.io.tmpdir"),
                String.format("txtmark_code_%d_%d.in", ID, IN_COUNT.incrementAndGet()));
        final File tmpOut = new File(System.getProperty("java.io.tmpdir"),
                String.format("txtmark_code_%d_%d.out", ID, OUT_COUNT.incrementAndGet()));

        try
        {

            final Writer w = new OutputStreamWriter(new FileOutputStream(tmpIn), encoding);

            try
            {
                for (final String s : lines)
                {
                    w.write(s);
                    w.write('\n');
                }
            }
            finally
            {
                w.close();
            }

            final List<String> command = new ArrayList<String>();

            command.add(prog);
            command.add(meta);
            command.add(tmpIn.getAbsolutePath());
            command.add(tmpOut.getAbsolutePath());

            final ProcessBuilder pb = new ProcessBuilder(command);
            final Process p = pb.start();
            final InputStream pIn = p.getInputStream();
            final byte[] buffer = new byte[2048];

            int exitCode = 0;
            for (;;)
            {
                if (pIn.available() > 0)
                {
                    pIn.read(buffer);
                }
                try
                {
                    exitCode = p.exitValue();
                }
                catch (final IllegalThreadStateException itse)
                {
                    continue;
                }
                break;
            }

            if (exitCode == 0)
            {
                final Reader r = new InputStreamReader(new FileInputStream(tmpOut), encoding);
                try
                {
                    final StringBuilder sb = new StringBuilder();
                    for (;;)
                    {
                        final int c = r.read();
                        if (c >= 0)
                        {
                            sb.append((char)c);
                        }
                        else
                        {
                            break;
                        }
                    }
                    return sb.toString();
                }
                finally
                {
                    r.close();
                }
            }

            throw new IOException("Exited with exit code: " + exitCode);
        }
        finally
        {
            tmpIn.delete();
            tmpOut.delete();
        }
    }

    public static void escapedAdd(final StringBuilder sb, final String str)
    {
        for (int i = 0; i < str.length(); i++)
        {
            final char ch = str.charAt(i);
            if (ch < 33 || Character.isWhitespace(ch) || Character.isSpaceChar(ch))
            {
                sb.append(' ');
            }
            else
            {
                switch (ch)
                {
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(ch);
                    break;
                }
            }
        }
    }
}
