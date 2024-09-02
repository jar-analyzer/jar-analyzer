/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.analyze.cfg;

import me.n1ar4.jar.analyzer.utils.canvas.Box;
import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.TextDirection;
import me.n1ar4.jar.analyzer.utils.theme.line.ContinuousLine;
import me.n1ar4.jar.analyzer.utils.theme.text.PlainText;

import java.util.ArrayList;
import java.util.List;

public class TextGraph {
    private static final int ROW_SPACE = 1;
    private static final int COL_SPACE = 3;

    private final InsnBlock[] blockArray;
    private final TextBox[] boxArray;
    private final int boxNum;
    private final int maxInstructionLength;
    private final Canvas canvas = new Canvas();

    public TextGraph(InsnBlock[] blockArray) {
        this.blockArray = blockArray;
        this.boxNum = blockArray.length;
        this.boxArray = new TextBox[boxNum];
        this.maxInstructionLength = findMaxStringLength(blockArray);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public String draw(int startRow, int startCol) {
        int row = startRow;
        int col = startCol;
        int length = boxArray.length;
        for (int i = 0; i < length; i++) {
            InsnBlock block = blockArray[i];

            int width = getOdd(maxInstructionLength + 2);
            int height = block.lines.size();

            TextBox box = TextBox.valueOf(row, col, width, height);
            boxArray[i] = box;
            drawBoxAndText(box, block.lines);

            row += height + ROW_SPACE + 2;
        }

        drawLinks();
        return canvas.toString();
    }

    private void drawBoxAndText(TextBox box, List<String> lines) {
        int row = box.row;
        int col = box.col;
        int width = box.width;
        int height = box.height;

        canvas.moveTo(row, col);
        canvas.drawRectangle(width, height);
        canvas.draw(row + 1, col + 2, PlainText.valueOf(lines));
    }

    private void drawLinks() {
        for (int i = 0; i < boxNum; i++) {
            InsnBlock currentBlock = blockArray[i];
            TextBox currentBox = boxArray[i];


            List<TextBox> nextBoxes = findBoxes(currentBlock.nextBlockList);
            for (TextBox nextBox : nextBoxes) {
                int rowStart = currentBox.row + currentBox.height + 1;
                int rowStop = nextBox.row;
                int col = currentBox.col + currentBox.width / 2;

                canvas.moveTo(rowStart, col);
                canvas.drawPixel(Box.DOWN_AND_HORIZONTAL);
                canvas.moveTo(rowStop, col);
                canvas.drawPixel(Box.UP_AND_HORIZONTAL);
                canvas.moveTo(rowStart + 1, col).drawVerticalLine(rowStop - rowStart - 1);
            }


            List<TextBox> jumpBoxes = findBoxes(currentBlock.jumpBlockList);
            for (TextBox nextBox : jumpBoxes) {
                int rowStart = currentBox.row + currentBox.height;
                int rowStop = nextBox.row + 1;
                int colStart = currentBox.col + currentBox.width + 1;
                int colStop = currentBox.col + currentBox.width + 1 + (i + 1) * COL_SPACE;

                canvas.moveTo(rowStart, colStart);
                canvas.drawPixel(Box.VERTICAL_AND_RIGHT);
                canvas.moveTo(rowStop, colStart);
                canvas.drawPixel(Box.VERTICAL_AND_RIGHT);

                if (rowStart < rowStop) {
                    ContinuousLine line = new ContinuousLine();
                    line.setDirection(TextDirection.RIGHT);
                    line.drawLine(colStop - colStart)
                            .turn(TextDirection.DOWN).drawLine(rowStop - rowStart - 1)
                            .turn(TextDirection.LEFT).drawLine(colStop - colStart);
                    canvas.draw(rowStart, colStart + 1, line);

                } else {
                    ContinuousLine line = new ContinuousLine();
                    line.setDirection(TextDirection.RIGHT);
                    line.drawLine(colStop - colStart)
                            .turn(TextDirection.UP).drawLine(rowStart - rowStop - 1)
                            .turn(TextDirection.LEFT).drawLine(colStop - colStart);
                    canvas.draw(rowStart, colStart + 1, line);
                }
            }
        }
    }

    private List<TextBox> findBoxes(List<InsnBlock> blockList) {
        List<TextBox> boxList = new ArrayList<>();

        for (int i = 0; i < boxNum; i++) {
            InsnBlock block = blockArray[i];
            if (blockList.contains(block)) {
                boxList.add(boxArray[i]);
            }
        }

        return boxList;
    }

    private int getOdd(int num) {
        int remainder = num % 2;
        if (remainder == 0) {
            return num + 1;
        }
        return num;
    }

    private int findMaxStringLength(InsnBlock[] blockArray) {
        int maxLength = 0;
        for (InsnBlock block : blockArray) {
            int length = findMaxStringLength(block.lines);
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    private int findMaxStringLength(List<String> lines) {
        int maxLength = 0;
        for (String item : lines) {
            if (item == null) continue;
            int length = item.length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }
}
