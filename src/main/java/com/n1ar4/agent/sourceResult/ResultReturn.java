package com.n1ar4.agent.sourceResult;

import java.io.Serializable;

public class ResultReturn implements Serializable {
    public String objectString;
    public String ConsoleOutput;

    public ResultReturn(String objectString, String consoleOutput) {
        this.objectString = objectString;
        ConsoleOutput = consoleOutput;
    }

    public String getObjectString() {
        return objectString;
    }

    public void setObjectString(String objectString) {
        this.objectString = objectString;
    }

    public String getConsoleOutput() {
        return ConsoleOutput;
    }

    public void setConsoleOutput(String consoleOutput) {
        ConsoleOutput = consoleOutput;
    }
}
