/*
 * Copyright (C) 2013-2015 René Jeschke <rene_jeschke@yahoo.de>
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

final class TxtmarkArguments
{
    @CmdArgument(l = "help", s = 'h', isSwitch = true, desc = "prints a summary of command line arguments.")
    public boolean printHelp            = false;

    @CmdArgument(l = "extended", isSwitch = true, desc = "forces extended profile")
    public boolean forceExtendedProfile = false;

    @CmdArgument(l = "panic-mode", isSwitch = true, desc = "enables panic mode")
    public boolean panicMode            = false;

    @CmdArgument(l = "safe-mode", isSwitch = true, desc = "enables safe mode")
    public boolean safeMode             = false;

    @CmdArgument(l = "no-fenced-spaces", isSwitch = true, desc = "disables spaces in fenced code block delimiters")
    public boolean noFencedSpaced       = false;

    @CmdArgument(l = "encoding", desc = "sets the IO encoding.")
    public String  encoding             = "UTF-8";

    @CmdArgument(l = "out-file", s = 'o', desc = "specifies the output filename, writes to stdout otherwise")
    public String  outFile              = null;

    @CmdArgument(l = "highlighter", desc = "full path to a program taking three arguments [meta in-file out-file] "
            + "that should be used for highlighting fenced code blocks. 'in-file' contains the text to be highlighted/escaped, "
            + "the result is expected to be written to 'out-file'")
    public String  highlighter          = null;
}
