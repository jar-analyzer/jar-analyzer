package me.n1ar4.y4lang;

import com.beust.jcommander.Parameter;

public class Command {
    @Parameter(names = {"-h", "--help"}, description = "Help Info", help = true)
    public boolean help;

    @Parameter(description = "Y4Lang File")
    public String filename;
}
