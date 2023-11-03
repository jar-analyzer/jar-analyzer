package me.n1ar4.jar.analyzer.utils.theme.text;

import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.Drawable;

import java.util.ArrayList;
import java.util.List;

public class PlainText implements Drawable {
    public final List<String> lines = new ArrayList<>();

    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            if (line == null) line = "";
            canvas.moveTo(startRow + i, startCol)
                    .drawText(line);
        }
    }

    public static PlainText valueOf(String line) {
        PlainText text = new PlainText();
        text.lines.add(line);
        return text;
    }

    public static PlainText valueOf(List<String> lines) {
        PlainText text = new PlainText();
        text.lines.addAll(lines);
        return text;
    }
}
