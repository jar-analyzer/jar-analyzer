/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games.flappy;

import java.util.*;

/**
 * Thread-safe, UI-independent model for the rewritten Flappy Bird game.
 */
public final class FlappyGame {
    public static final int WIDTH = 360;
    public static final int HEIGHT = 600;
    public static final int GROUND_Y = 540;
    public static final int BIRD_X = 88;
    public static final int BIRD_WIDTH = 40;
    public static final int BIRD_HEIGHT = 30;
    public static final int PIPE_WIDTH = 58;

    public enum State {
        READY, RUNNING, PAUSED, GAME_OVER
    }

    public static final class PipeView {
        private final int x;
        private final int gapTop;
        private final int gapHeight;

        private PipeView(int x, int gapTop, int gapHeight) {
            this.x = x;
            this.gapTop = gapTop;
            this.gapHeight = gapHeight;
        }

        public int getX() {
            return x;
        }

        public int getGapTop() {
            return gapTop;
        }

        public int getGapHeight() {
            return gapHeight;
        }
    }

    public static final class Snapshot {
        private final State state;
        private final int birdY;
        private final double birdVelocity;
        private final int score;
        private final int bestScore;
        private final int groundOffset;
        private final List<PipeView> pipes;

        private Snapshot(State state, int birdY, double birdVelocity, int score,
                         int bestScore, int groundOffset, List<PipeView> pipes) {
            this.state = state;
            this.birdY = birdY;
            this.birdVelocity = birdVelocity;
            this.score = score;
            this.bestScore = bestScore;
            this.groundOffset = groundOffset;
            this.pipes = pipes;
        }

        public State getState() {
            return state;
        }

        public int getBirdY() {
            return birdY;
        }

        public double getBirdVelocity() {
            return birdVelocity;
        }

        public int getScore() {
            return score;
        }

        public int getBestScore() {
            return bestScore;
        }

        public int getGroundOffset() {
            return groundOffset;
        }

        public List<PipeView> getPipes() {
            return pipes;
        }
    }

    private static final class PipeState {
        private double x;
        private final int gapTop;
        private final int gapHeight;
        private boolean scored;

        private PipeState(double x, int gapTop, int gapHeight) {
            this.x = x;
            this.gapTop = gapTop;
            this.gapHeight = gapHeight;
        }
    }

    private final Random random;
    private final List<PipeState> pipes = new ArrayList<>();
    private State state = State.READY;
    private double birdY = 260;
    private double birdVelocity;
    private double spawnElapsed;
    private double groundOffset;
    private int score;
    private int bestScore;

    public FlappyGame() {
        this(new Random());
    }

    FlappyGame(Random random) {
        this.random = random;
    }

    public synchronized void flap() {
        if (state == State.READY) {
            state = State.RUNNING;
        } else if (state == State.GAME_OVER) {
            restart();
        } else if (state == State.PAUSED) {
            state = State.RUNNING;
        }
        if (state == State.RUNNING) {
            birdVelocity = -340;
        }
    }

    public synchronized void togglePause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED) {
            state = State.RUNNING;
        }
    }

    public synchronized void restart() {
        bestScore = Math.max(bestScore, score);
        pipes.clear();
        birdY = 260;
        birdVelocity = 0;
        spawnElapsed = 0;
        groundOffset = 0;
        score = 0;
        state = State.RUNNING;
    }

    public synchronized void update(double deltaSeconds) {
        if (state != State.RUNNING || deltaSeconds <= 0) {
            return;
        }
        double delta = Math.min(0.05, deltaSeconds);
        double pipeSpeed = Math.min(245, 145 + score * 4.5);
        int gapHeight = Math.max(112, 150 - score);

        birdVelocity += 910 * delta;
        birdY += birdVelocity * delta;
        groundOffset = (groundOffset + pipeSpeed * delta) % 32;
        spawnElapsed += delta;
        if (spawnElapsed >= 1.55) {
            spawnElapsed = 0;
            int minTop = 78;
            int maxTop = GROUND_Y - gapHeight - 90;
            pipes.add(new PipeState(WIDTH + 20,
                    minTop + random.nextInt(maxTop - minTop + 1), gapHeight));
        }

        Iterator<PipeState> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            PipeState pipe = iterator.next();
            pipe.x -= pipeSpeed * delta;
            if (!pipe.scored && pipe.x + PIPE_WIDTH < BIRD_X) {
                pipe.scored = true;
                score++;
            }
            if (pipe.x + PIPE_WIDTH < -10) {
                iterator.remove();
            }
        }

        if (birdY < 0 || birdY + BIRD_HEIGHT >= GROUND_Y || collided()) {
            state = State.GAME_OVER;
            bestScore = Math.max(bestScore, score);
        }
    }

    public synchronized Snapshot snapshot() {
        List<PipeView> views = new ArrayList<>();
        for (PipeState pipe : pipes) {
            views.add(new PipeView((int) Math.round(pipe.x), pipe.gapTop, pipe.gapHeight));
        }
        return new Snapshot(state, (int) Math.round(birdY), birdVelocity,
                score, bestScore, (int) groundOffset,
                Collections.unmodifiableList(views));
    }

    synchronized void addPipe(int x, int gapTop, int gapHeight) {
        pipes.add(new PipeState(x, gapTop, gapHeight));
    }

    private boolean collided() {
        int birdLeft = BIRD_X + 5;
        int birdRight = BIRD_X + BIRD_WIDTH - 5;
        int birdTop = (int) birdY + 4;
        int birdBottom = (int) birdY + BIRD_HEIGHT - 4;
        for (PipeState pipe : pipes) {
            int pipeLeft = (int) pipe.x;
            int pipeRight = pipeLeft + PIPE_WIDTH;
            if (birdRight > pipeLeft && birdLeft < pipeRight
                    && (birdTop < pipe.gapTop
                    || birdBottom > pipe.gapTop + pipe.gapHeight)) {
                return true;
            }
        }
        return false;
    }
}
