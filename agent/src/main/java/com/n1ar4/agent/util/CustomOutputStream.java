package com.n1ar4.agent.util;

import java.io.IOException;
import java.io.OutputStream;

public class CustomOutputStream extends OutputStream {

    public StringBuilder stringBuilder;

    public CustomOutputStream(){
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        this.stringBuilder.append(Character.valueOf((char) b));
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.stringBuilder.append(b);
    }

    public void clearBuffer(){
        stringBuilder.delete(0 , stringBuilder.length());
    }

    public String getResult(){
        String result = stringBuilder.toString();
        this.clearBuffer();

        return result;
    }
}
