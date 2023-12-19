package me.n1ar4.jar.analyzer.plugins.y4lang;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.OutputStream;

public class TestAreaStream extends OutputStream {
    private final JTextArea textArea;

    public TestAreaStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        textArea.append(String.valueOf((char) b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
