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

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


public final class Run
{
    private final static void printUsage()
    {
        try
        {
            System.out.println("Usage: txtmark [options] [input-file]");
            System.out.println("Options:");
            System.out.println(CmdLineParser.generateHelp(80, false, new TxtmarkArguments()));
        }
        catch (final IOException e)
        {
            //
        }
    }

    public static void main(final String[] args)
    {
        final TxtmarkArguments ta = new TxtmarkArguments();
        List<String> rest = new ArrayList<String>();

        boolean parseError = false;
        try
        {
            rest = CmdLineParser.parse(args, ta);
        }
        catch (final IOException e)
        {
            System.out.println("Error: " + e.getMessage());
            System.out.println("---");
            parseError = true;
        }

        if (ta.printHelp || parseError)
        {
            printUsage();
            System.exit(parseError ? 1 : 0);
        }

        // Build configuration from command line arguments
        final Configuration.Builder cfgBuilder = Configuration.builder();
        cfgBuilder.setEncoding(ta.encoding)
                .setEnablePanicMode(ta.panicMode)
                .setSafeMode(ta.safeMode)
                .setAllowSpacesInFencedCodeBlockDelimiters(!ta.noFencedSpaced);
        // Check for extended profile
        if (ta.forceExtendedProfile)
        {
            cfgBuilder.forceExtentedProfile();
        }
        // Connect highlighter if any
        if (ta.highlighter != null && !ta.highlighter.isEmpty())
        {
            if (!new File(ta.highlighter).exists())
            {
                System.err.println("Program '" + ta.highlighter + "' not found");
                System.exit(1);
            }
            cfgBuilder.setCodeBlockEmitter(new CodeBlockEmitter(ta.encoding, ta.highlighter));
        }

        // Ready for action
        final Configuration config = cfgBuilder.build();
        boolean processOk = true;
        InputStream input = null;
        Writer output = null;
        try
        {
            final String inFile = rest.isEmpty() ? "--" : rest.get(0);
            final String outFile = ta.outFile;

            if (inFile.equals("--"))
            {
                input = System.in;
            }
            else
            {
                input = new FileInputStream(inFile);
            }

            final String result = Processor.process(input, config);

            if (outFile == null)
            {
                output = new OutputStreamWriter(System.out, ta.encoding);
            }
            else
            {
                output = new OutputStreamWriter(new FileOutputStream(outFile), ta.encoding);
            }

            output.write(result);
        }
        catch (final IOException e)
        {
            processOk = false;
            System.err.println("Exception: " + e.toString());
            e.printStackTrace(System.err);
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (final IOException e)
                {
                    // ignore
                }
            }
            if (output != null)
            {
                try
                {
                    output.close();
                }
                catch (final IOException e)
                {
                    // ignore
                }
            }
        }

        System.exit(processOk ? 0 : 1);
    }
}
