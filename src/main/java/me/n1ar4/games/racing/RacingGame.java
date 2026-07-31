/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.games.racing;

import java.util.*;

/**
 * Pure game state for the bundled lane-dodging racing game.
 */
public final class RacingGame {
    public static final int WORLD_WIDTH = 420;
    public static final int WORLD_HEIGHT = 640;
    public static final int ROAD_LEFT = 48;
    public static final int ROAD_WIDTH = 324;
    public static final int LANE_COUNT = 3;
    public static final int CAR_WIDTH = 46;
    public static final int CAR_HEIGHT = 82;
    public static final int PLAYER_Y = 525;

    public enum State {
        READY, RUNNING, PAUSED, GAME_OVER
    }

    public static final class Obstacle {
        private final int lane;
        private final int style;
        private double y;

        private Obstacle(int lane, double y, int style) {
            this.lane = lane;
            this.y = y;
            this.style = style;
        }

        public int getLane() {
            return lane;
        }

        public int getY() {
            return (int) Math.round(y);
        }

        public int getStyle() {
            return style;
        }
    }

    public static final class Snapshot {
        private final State state;
        private final int playerLane;
        private final int score;
        private final int bestScore;
        private final int speed;
        private final int roadOffset;
        private final List<Obstacle> obstacles;

        private Snapshot(State state, int playerLane, int score, int bestScore,
                         int speed, int roadOffset, List<Obstacle> obstacles) {
            this.state = state;
            this.playerLane = playerLane;
            this.score = score;
            this.bestScore = bestScore;
            this.speed = speed;
            this.roadOffset = roadOffset;
            this.obstacles = obstacles;
        }

        public State getState() {
            return state;
        }

        public int getPlayerLane() {
            return playerLane;
        }

        public int getScore() {
            return score;
        }

        public int getBestScore() {
            return bestScore;
        }

        public int getSpeed() {
            return speed;
        }

        public int getRoadOffset() {
            return roadOffset;
        }

        public List<Obstacle> getObstacles() {
            return obstacles;
        }
    }

    private final Random random;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private State state = State.READY;
    private int playerLane = 1;
    private int score;
    private int bestScore;
    private double distance;
    private double spawnElapsed;
    private double roadOffset;

    public RacingGame() {
        this(new Random());
    }

    RacingGame(Random random) {
        this.random = random;
    }

    public synchronized void startOrResume() {
        if (state == State.READY || state == State.PAUSED) {
            state = State.RUNNING;
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
        obstacles.clear();
        playerLane = 1;
        score = 0;
        distance = 0;
        spawnElapsed = 0;
        roadOffset = 0;
        state = State.RUNNING;
    }

    public synchronized void moveLeft() {
        if (state == State.RUNNING && playerLane > 0) {
            playerLane--;
        }
    }

    public synchronized void moveRight() {
        if (state == State.RUNNING && playerLane < LANE_COUNT - 1) {
            playerLane++;
        }
    }

    public synchronized void update(long elapsedMillis) {
        if (state != State.RUNNING || elapsedMillis <= 0) {
            return;
        }
        double seconds = Math.min(elapsedMillis, 50) / 1000.0;
        double speed = currentSpeed();
        distance += speed * seconds;
        score = (int) (distance / 12);
        roadOffset = (roadOffset + speed * seconds) % 80;
        spawnElapsed += elapsedMillis;

        double spawnInterval = Math.max(430, 980 - score * 2.2);
        if (spawnElapsed >= spawnInterval) {
            spawnElapsed = 0;
            spawnObstacle();
        }

        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obstacle = iterator.next();
            obstacle.y += speed * seconds;
            if (collides(obstacle)) {
                state = State.GAME_OVER;
                bestScore = Math.max(bestScore, score);
                return;
            }
            if (obstacle.y > WORLD_HEIGHT + CAR_HEIGHT) {
                iterator.remove();
                score += 15;
                distance += 180;
            }
        }
    }

    public synchronized Snapshot snapshot() {
        List<Obstacle> copy = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            copy.add(new Obstacle(obstacle.lane, obstacle.y, obstacle.style));
        }
        return new Snapshot(state, playerLane, score, bestScore,
                (int) currentSpeed(), (int) roadOffset,
                Collections.unmodifiableList(copy));
    }

    synchronized void addObstacle(int lane, double y) {
        obstacles.add(new Obstacle(lane, y, 0));
    }

    public static int laneX(int lane) {
        int laneWidth = ROAD_WIDTH / LANE_COUNT;
        return ROAD_LEFT + lane * laneWidth + (laneWidth - CAR_WIDTH) / 2;
    }

    private double currentSpeed() {
        return Math.min(440, 185 + score * 1.1);
    }

    private void spawnObstacle() {
        int lane = random.nextInt(LANE_COUNT);
        if (!obstacles.isEmpty()) {
            Obstacle last = obstacles.get(obstacles.size() - 1);
            if (last.y < 150 && last.lane == lane) {
                lane = (lane + 1 + random.nextInt(LANE_COUNT - 1)) % LANE_COUNT;
            }
        }
        obstacles.add(new Obstacle(lane, -CAR_HEIGHT, random.nextInt(2)));
    }

    private boolean collides(Obstacle obstacle) {
        if (obstacle.lane != playerLane) {
            return false;
        }
        int paddingX = 7;
        int paddingY = 8;
        int obstacleTop = obstacle.getY() + paddingY;
        int obstacleBottom = obstacle.getY() + CAR_HEIGHT - paddingY;
        int playerTop = PLAYER_Y + paddingY;
        int playerBottom = PLAYER_Y + CAR_HEIGHT - paddingY;
        return obstacleBottom > playerTop && obstacleTop < playerBottom
                && CAR_WIDTH - paddingX * 2 > 0;
    }
}
