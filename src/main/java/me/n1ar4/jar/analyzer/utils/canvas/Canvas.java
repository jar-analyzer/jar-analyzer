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

package me.n1ar4.jar.analyzer.utils.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

public class Canvas {
    private int row;
    private int col;
    private final List<TextPixel> pixelList = new ArrayList<>();


    public Canvas moveTo(int row, int col) {
        this.row = row;
        this.col = col;
        return this;
    }

    public Canvas up(int count) {
        this.row -= count;
        return this;
    }

    public Canvas down(int count) {
        this.row += count;
        return this;
    }

    public Canvas left(int count) {
        this.col -= count;
        return this;
    }

    public Canvas right(int count) {
        this.col += count;
        return this;
    }

    public void rectifyPosition() {
        int minRow = 0;
        int minCol = 0;
        for (TextPixel pixel : pixelList) {
            int row = pixel.row;
            if (row < minRow) {
                minRow = row;
            }
            int col = pixel.col;
            if (col < minCol) {
                minCol = col;
            }
        }

        for (TextPixel pixel : pixelList) {
            pixel.row -= minRow;
            pixel.col -= minCol;
        }
    }

    public void updatePosition(int startRow, int startCol) {
        for (TextPixel pixel : pixelList) {
            pixel.row += startRow;
            pixel.col += startCol;
        }
    }


    public void drawPixel(Box value) {
        TextPixel pixel = findPixel(row, col);
        if (pixel != null) {
            if (Box.isValid(pixel.value)) {
                pixel.value = Box.merge(pixel.value, value.val).val;
            } else {
                pixel.value = value.val;
            }
        } else {
            pixel = TextPixel.valueOf(row, col, value.val);
            pixelList.add(pixel);
            Collections.sort(pixelList);
        }
    }

    public void drawPixel(String value) {
        String firstChar = value.substring(0, 1);
        TextPixel pixel = findPixel(row, col);
        if (pixel != null) {
            pixel.value = firstChar;
        } else {
            pixel = TextPixel.valueOf(row, col, firstChar);
            pixelList.add(pixel);
            Collections.sort(pixelList);
        }
    }

    public Canvas switchDirection(TextDirection from, TextDirection to) {
        if (from == TextDirection.UP && to == TextDirection.LEFT) {
            drawPixel(Box.UP_AND_LEFT);
            left(1);
        } else if (from == TextDirection.UP && to == TextDirection.RIGHT) {
            drawPixel(Box.UP_AND_RIGHT);
            right(1);
        } else if (from == TextDirection.RIGHT && to == TextDirection.UP) {
            drawPixel(Box.UP_AND_RIGHT);
            up(1);
        } else if (from == TextDirection.RIGHT && to == TextDirection.DOWN) {
            drawPixel(Box.DOWN_AND_RIGHT);
            down(1);
        } else if (from == TextDirection.DOWN && to == TextDirection.RIGHT) {
            drawPixel(Box.DOWN_AND_RIGHT);
            right(1);
        } else if (from == TextDirection.DOWN && to == TextDirection.LEFT) {
            drawPixel(Box.DOWN_AND_LEFT);
            left(1);
        } else if (from == TextDirection.LEFT && to == TextDirection.DOWN) {
            drawPixel(Box.DOWN_AND_LEFT);
            down(1);
        } else if (from == TextDirection.LEFT && to == TextDirection.UP) {
            drawPixel(Box.UP_AND_LEFT);
            up(1);
        } else {
            assert false : "impossible here";
        }

        return this;
    }


    public Canvas drawHorizontalLine(int num) {
        int step = num > 0 ? 1 : -1;
        int count = Math.abs(num);
        for (int i = 0; i < count; i++) {
            drawPixel(Box.HORIZONTAL);
            col += step;
        }
        return this;
    }

    public Canvas drawVerticalLine(int num) {
        int step = num > 0 ? 1 : -1;
        int count = Math.abs(num);
        for (int i = 0; i < count; i++) {
            drawPixel(Box.VERTICAL);
            row += step;
        }
        return this;
    }

    public void drawText(String text) {
        int length = text.length();
        for (int i = 0; i < length; i++) {
            String ch = text.substring(i, i + 1);
            drawPixel(ch);
            col++;
        }
    }


    public void drawRectangle(int width, int height) {

        drawPixel(Box.DOWN_AND_RIGHT);
        right(1);
        for (int i = 0; i < width; i++) {
            drawPixel(Box.HORIZONTAL);
            right(1);
        }


        drawPixel(Box.DOWN_AND_LEFT);
        down(1);
        for (int i = 0; i < height; i++) {
            drawPixel(Box.VERTICAL);
            down(1);
        }


        drawPixel(Box.UP_AND_LEFT);
        left(1);
        for (int i = 0; i < width; i++) {
            drawPixel(Box.HORIZONTAL);
            left(1);
        }


        drawPixel(Box.UP_AND_RIGHT);
        up(1);
        for (int i = 0; i < height; i++) {
            drawPixel(Box.VERTICAL);
            up(1);
        }
    }

    public void drawTable(int[] rowHeightArray, int[] colWidthArray) {
        int startRow = row;
        int startCol = col;

        int rowCount = rowHeightArray.length;
        int colCount = colWidthArray.length;

        int totalWidth = colCount - 1;
        for (int width : colWidthArray) {
            totalWidth += width;
        }

        int totalHeight = rowCount - 1;
        for (int height : rowHeightArray) {
            totalHeight += height;
        }


        drawRectangle(totalWidth, totalHeight);


        moveTo(startRow, startCol);
        for (int i = 0; i < colCount - 1; i++) {
            int width = colWidthArray[i];
            right(width + 1);
            drawPixel(Box.DOWN_AND_HORIZONTAL);
            down(1);
            drawVerticalLine(totalHeight);
            drawPixel(Box.UP_AND_HORIZONTAL);
            up(totalHeight + 1);
        }


        moveTo(startRow, startCol);
        for (int i = 0; i < rowCount - 1; i++) {
            int height = rowHeightArray[i];
            down(height + 1);
            drawPixel(Box.VERTICAL_AND_RIGHT);
            right(1);
            drawHorizontalLine(totalWidth);
            drawPixel(Box.VERTICAL_AND_LEFT);
            left(totalWidth + 1);
        }

    }


    public void printPixels() {
        Collections.sort(pixelList);
        StringBuilder sb = new StringBuilder();
        Formatter fm = new Formatter(sb);
        for (TextPixel pixel : pixelList) {
            fm.format("[DEBUG] %s%n", pixel);
        }
        System.out.println(sb);
    }

    public List<String> getLines() {
        List<String> lines = new ArrayList<>();
        Collections.sort(pixelList);

        int maxRow = findMaxRow(pixelList);
        for (int row = 0; row <= maxRow; row++) {
            List<TextPixel> rowList = findRowItems(row);
            int maxCol = findMaxCol(rowList);
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col <= maxCol; col++) {

                TextPixel item = null;
                if (i < rowList.size()) {
                    item = rowList.get(i);
                }

                if (item != null && item.col == col) {
                    sb.append(item.value);
                    i++;
                } else {
                    sb.append(Box.SPACE.val);
                }
            }
            String line = sb.toString();
            lines.add(line);
        }
        return lines;
    }

    @Override
    public String toString() {
        List<String> lines = getLines();
        return StringUtils.list2str(lines);
    }

    public void draw(int row, int col, Drawable drawable) {
        drawable.draw(this, row, col);
    }

    public void overlay(Canvas canvas) {
        for (TextPixel pixel : canvas.pixelList) {
            int targetRow = pixel.row;
            int targetCol = pixel.col;
            TextPixel targetPixel = findPixel(targetRow, targetCol);
            if (targetPixel != null) {
                if (Box.isValid(targetPixel.value) && Box.isValid(pixel.value)) {
                    targetPixel.value = Box.merge(targetPixel.value, pixel.value).val;
                } else {
                    targetPixel.value = pixel.value;
                }
            } else {
                targetPixel = TextPixel.valueOf(targetRow, targetCol, pixel.value);
                pixelList.add(targetPixel);
            }
        }

        Collections.sort(pixelList);
    }


    private TextPixel findPixel(int row, int col) {
        for (TextPixel item : pixelList) {
            if (item.row == row && item.col == col) {
                return item;
            }
        }
        return null;
    }

    private int findMaxRow(List<TextPixel> list) {
        int maxRow = 0;
        for (TextPixel item : list) {
            if (item.row > maxRow) {
                maxRow = item.row;
            }
        }
        return maxRow;
    }

    private int findMaxCol(List<TextPixel> list) {
        int maxCol = 0;
        for (TextPixel item : list) {
            if (item.col > maxCol) {
                maxCol = item.col;
            }
        }
        return maxCol;
    }

    private List<TextPixel> findRowItems(int row) {
        List<TextPixel> list = new ArrayList<>();
        for (TextPixel item : pixelList) {
            if (item.col < 0) {
                continue;
            }
            if (item.row == row) {
                list.add(item);
            }
        }
        return list;
    }

}
