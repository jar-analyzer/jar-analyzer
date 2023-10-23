package me.n1ar4.jar.analyzer.cfg.theme.line;

import me.n1ar4.jar.analyzer.cfg.canvas.Box;
import me.n1ar4.jar.analyzer.cfg.canvas.Canvas;
import me.n1ar4.jar.analyzer.cfg.canvas.Drawable;
import me.n1ar4.jar.analyzer.cfg.canvas.TextDirection;

public class ContinuousLine implements Drawable {
    private final Canvas localCanvas = new Canvas();

    private TextDirection direction = TextDirection.RIGHT;

    public void setDirection(TextDirection direction) {
        this.direction = direction;
    }

    public ContinuousLine turn(TextDirection direction) {
        if (this.direction == TextDirection.UP && direction == TextDirection.LEFT) {
            localCanvas.drawPixel(Box.DOWN_AND_LEFT);
            localCanvas.left(1);
        }
        else if (this.direction == TextDirection.UP && direction == TextDirection.RIGHT) {
            localCanvas.drawPixel(Box.DOWN_AND_RIGHT);
            localCanvas.right(1);
        }
        else if (this.direction == TextDirection.RIGHT && direction == TextDirection.UP) {
            localCanvas.drawPixel(Box.UP_AND_LEFT);
            localCanvas.up(1);
        }
        else if (this.direction == TextDirection.RIGHT && direction == TextDirection.DOWN) {
            localCanvas.drawPixel(Box.DOWN_AND_LEFT);
            localCanvas.down(1);
        }
        else if (this.direction == TextDirection.DOWN && direction == TextDirection.RIGHT) {
            localCanvas.drawPixel(Box.UP_AND_RIGHT);
            localCanvas.right(1);
        }
        else if (this.direction == TextDirection.DOWN && direction == TextDirection.LEFT) {
            localCanvas.drawPixel(Box.UP_AND_LEFT);
            localCanvas.left(1);
        }
        else if (this.direction == TextDirection.LEFT && direction == TextDirection.DOWN) {
            localCanvas.drawPixel(Box.DOWN_AND_RIGHT);
            localCanvas.down(1);
        }
        else if (this.direction == TextDirection.LEFT && direction == TextDirection.UP) {
            localCanvas.drawPixel(Box.UP_AND_RIGHT);
            localCanvas.up(1);
        }
        else {
            assert false : "impossible here";
        }
        this.direction = direction;
        return this;
    }

    public ContinuousLine drawLine(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("'count' should be greater than zero: " + count);
        }

        switch (direction) {
            case UP:
                localCanvas.drawVerticalLine(-count);
                break;
            case RIGHT:
                localCanvas.drawHorizontalLine(count);
                break;
            case DOWN:
                localCanvas.drawVerticalLine(count);
                break;
            case LEFT:
                localCanvas.drawHorizontalLine(-count);
                break;
            default:
                assert false : "impossible here";
        }
        return this;
    }

    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        localCanvas.updatePosition(startRow, startCol);
        localCanvas.rectifyPosition();

        canvas.overlay(localCanvas);
    }
}
