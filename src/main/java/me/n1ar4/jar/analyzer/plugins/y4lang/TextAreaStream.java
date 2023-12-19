package me.n1ar4.jar.analyzer.plugins.y4lang;

import javax.swing.*;
import java.io.OutputStream;

public class TextAreaStream extends OutputStream {
    private final JTextArea textArea;

    public TextAreaStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        textArea.append(String.valueOf((char) b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
