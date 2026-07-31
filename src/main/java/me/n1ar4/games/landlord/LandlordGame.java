/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 */

package me.n1ar4.games.landlord;

import java.util.*;

/**
 * Thread-safe three-player Fight the Landlord state machine.
 */
public final class LandlordGame {
    public static final int HUMAN = 0;
    public static final int LEFT_AI = 1;
    public static final int RIGHT_AI = 2;
    private static final String[] PLAYER_NAMES = {"你", "电脑甲", "电脑乙"};

    public enum State {
        BIDDING, PLAYING, GAME_OVER
    }

    public static final class Snapshot {
        private final State state;
        private final int currentPlayer;
        private final int landlord;
        private final List<PlayingCard> humanHand;
        private final List<PlayingCard> bottomCards;
        private final List<PlayingCard> lastCards;
        private final Combination lastCombination;
        private final int lastPlayer;
        private final int[] cardCounts;
        private final String message;
        private final String winner;
        private final int lastActionPlayer;
        private final String lastActionText;

        private Snapshot(State state, int currentPlayer, int landlord,
                         List<PlayingCard> humanHand, List<PlayingCard> bottomCards,
                         List<PlayingCard> lastCards, Combination lastCombination,
                         int lastPlayer, int[] cardCounts, String message, String winner,
                         int lastActionPlayer, String lastActionText) {
            this.state = state;
            this.currentPlayer = currentPlayer;
            this.landlord = landlord;
            this.humanHand = humanHand;
            this.bottomCards = bottomCards;
            this.lastCards = lastCards;
            this.lastCombination = lastCombination;
            this.lastPlayer = lastPlayer;
            this.cardCounts = cardCounts;
            this.message = message;
            this.winner = winner;
            this.lastActionPlayer = lastActionPlayer;
            this.lastActionText = lastActionText;
        }

        public State getState() {
            return state;
        }

        public int getCurrentPlayer() {
            return currentPlayer;
        }

        public int getLandlord() {
            return landlord;
        }

        public List<PlayingCard> getHumanHand() {
            return humanHand;
        }

        public List<PlayingCard> getBottomCards() {
            return bottomCards;
        }

        public List<PlayingCard> getLastCards() {
            return lastCards;
        }

        public Combination getLastCombination() {
            return lastCombination;
        }

        public int getLastPlayer() {
            return lastPlayer;
        }

        public int getCardCount(int player) {
            return cardCounts[player];
        }

        public String getMessage() {
            return message;
        }

        public String getWinner() {
            return winner;
        }

        public int getLastActionPlayer() {
            return lastActionPlayer;
        }

        public String getLastActionText() {
            return lastActionText;
        }

        public boolean isHumanTurn() {
            return currentPlayer == HUMAN && state != State.GAME_OVER;
        }

        public boolean canHumanPass() {
            return state == State.PLAYING && currentPlayer == HUMAN
                    && lastCombination != null && lastPlayer != HUMAN;
        }
    }

    private final Random random;
    private final List<PlayingCard>[] hands;
    private final List<PlayingCard> bottomCards = new ArrayList<>();
    private final boolean[] bids = new boolean[3];
    private State state;
    private int currentPlayer;
    private int bidCount;
    private int landlord = -1;
    private Combination lastCombination;
    private List<PlayingCard> lastCards = Collections.emptyList();
    private int lastPlayer = -1;
    private int passCount;
    private String message;
    private String winner;
    private int lastActionPlayer;
    private String lastActionText;

    @SuppressWarnings("unchecked")
    public LandlordGame() {
        this(new Random());
    }

    @SuppressWarnings("unchecked")
    LandlordGame(Random random) {
        this.random = random;
        this.hands = new List[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        restart();
    }

    public synchronized void restart() {
        for (List<PlayingCard> hand : hands) {
            hand.clear();
        }
        bottomCards.clear();
        List<PlayingCard> deck = createDeck();
        Collections.shuffle(deck, random);
        for (int i = 0; i < 51; i++) {
            hands[i % 3].add(deck.get(i));
        }
        bottomCards.addAll(deck.subList(51, 54));
        for (List<PlayingCard> hand : hands) {
            Collections.sort(hand);
        }
        for (int i = 0; i < bids.length; i++) {
            bids[i] = false;
        }
        state = State.BIDDING;
        currentPlayer = HUMAN;
        bidCount = 0;
        landlord = -1;
        lastCombination = null;
        lastCards = Collections.emptyList();
        lastPlayer = -1;
        passCount = 0;
        winner = null;
        lastActionPlayer = -1;
        lastActionText = "";
        message = "请选择抢地主或不抢";
    }

    public synchronized boolean humanBid(boolean bid) {
        if (state != State.BIDDING || currentPlayer != HUMAN) {
            return false;
        }
        recordBid(HUMAN, bid);
        return true;
    }

    public synchronized boolean playHumanCards(Set<Integer> selectedIds) {
        if (state != State.PLAYING || currentPlayer != HUMAN || selectedIds.isEmpty()) {
            message = "请先选择要出的牌";
            return false;
        }
        Set<Integer> ids = new HashSet<>(selectedIds);
        List<PlayingCard> selected = new ArrayList<>();
        for (PlayingCard card : hands[HUMAN]) {
            if (ids.contains(card.getId())) {
                selected.add(card);
            }
        }
        if (selected.size() != ids.size()) {
            message = "选中的牌已发生变化";
            return false;
        }
        return play(HUMAN, selected);
    }

    public synchronized boolean humanPass() {
        if (state != State.PLAYING || currentPlayer != HUMAN
                || lastCombination == null || lastPlayer == HUMAN) {
            message = "当前不能不出";
            return false;
        }
        pass(HUMAN);
        return true;
    }

    /**
     * Performs exactly one AI bid/play/pass action.
     */
    public synchronized boolean performAiStep() {
        if (state == State.GAME_OVER || currentPlayer == HUMAN) {
            return false;
        }
        if (state == State.BIDDING) {
            int power = handPower(hands[currentPlayer]);
            boolean bid = power >= 9 || bidCount == 2 && !bids[0] && !bids[1];
            recordBid(currentPlayer, bid);
            return true;
        }

        List<PlayingCard> choice = LandlordAi.choose(hands[currentPlayer], lastCombination);
        if (choice.isEmpty()) {
            if (lastCombination == null || lastPlayer == currentPlayer) {
                choice = Collections.singletonList(hands[currentPlayer].get(0));
                play(currentPlayer, choice);
            } else {
                pass(currentPlayer);
            }
        } else {
            play(currentPlayer, choice);
        }
        return true;
    }

    public synchronized Snapshot snapshot() {
        int[] counts = {hands[0].size(), hands[1].size(), hands[2].size()};
        return new Snapshot(state, currentPlayer, landlord,
                immutableCopy(hands[HUMAN]), immutableCopy(bottomCards),
                immutableCopy(lastCards), lastCombination, lastPlayer,
                counts, message, winner, lastActionPlayer, lastActionText);
    }

    private void recordBid(int player, boolean bid) {
        bids[player] = bid;
        bidCount++;
        lastActionPlayer = player;
        lastActionText = bid ? "抢地主！" : "不抢";
        message = PLAYER_NAMES[player] + (bid ? " 抢地主" : " 不抢");
        if (bidCount >= 3) {
            finishBidding();
        } else {
            currentPlayer = (currentPlayer + 1) % 3;
        }
    }

    private void finishBidding() {
        int bestPlayer = -1;
        int bestPower = Integer.MIN_VALUE;
        for (int player = 0; player < 3; player++) {
            if (bids[player]) {
                int power = handPower(hands[player]);
                if (power > bestPower) {
                    bestPower = power;
                    bestPlayer = player;
                }
            }
        }
        if (bestPlayer < 0) {
            for (int player = 0; player < 3; player++) {
                int power = handPower(hands[player]);
                if (power > bestPower) {
                    bestPower = power;
                    bestPlayer = player;
                }
            }
            message = "无人抢地主，由牌力最高者担任";
        }
        landlord = bestPlayer;
        hands[landlord].addAll(bottomCards);
        Collections.sort(hands[landlord]);
        currentPlayer = landlord;
        state = State.PLAYING;
        message = PLAYER_NAMES[landlord] + " 成为地主，请出牌";
    }

    private boolean play(int player, List<PlayingCard> cards) {
        Combination combination = LandlordRules.evaluate(cards);
        if (combination == null) {
            message = "这些牌不是有效牌型";
            return false;
        }
        if (lastCombination != null && lastPlayer != player
                && !combination.beats(lastCombination)) {
            message = combination + " 压不过 " + lastCombination;
            return false;
        }
        if (!hands[player].containsAll(cards)) {
            message = "手牌不足";
            return false;
        }

        hands[player].removeAll(cards);
        lastCards = new ArrayList<>(cards);
        Collections.sort(lastCards);
        lastCombination = combination;
        lastPlayer = player;
        passCount = 0;
        lastActionPlayer = player;
        lastActionText = "出牌 · " + combination;
        message = PLAYER_NAMES[player] + " 出了 " + combination;
        if (hands[player].isEmpty()) {
            state = State.GAME_OVER;
            winner = player == landlord ? "地主胜利" : "农民胜利";
            message = PLAYER_NAMES[player] + " 出完所有手牌";
        } else {
            currentPlayer = (player + 1) % 3;
        }
        return true;
    }

    private void pass(int player) {
        passCount++;
        lastActionPlayer = player;
        lastActionText = "不出";
        message = PLAYER_NAMES[player] + " 不出";
        if (passCount >= 2) {
            currentPlayer = lastPlayer;
            lastCombination = null;
            lastCards = Collections.emptyList();
            lastPlayer = -1;
            passCount = 0;
            message += "，新一轮自由出牌";
        } else {
            currentPlayer = (player + 1) % 3;
        }
    }

    private static int handPower(List<PlayingCard> hand) {
        int power = 0;
        int[] counts = new int[18];
        for (PlayingCard card : hand) {
            counts[card.getRank()]++;
            if (card.getRank() >= 15) {
                power += card.getRank() - 13;
            }
        }
        if (counts[16] == 1 && counts[17] == 1) {
            power += 7;
        }
        for (int count : counts) {
            if (count == 4) {
                power += 6;
            }
        }
        return power;
    }

    private static List<PlayingCard> createDeck() {
        List<PlayingCard> deck = new ArrayList<>(54);
        int id = 0;
        PlayingCard.Suit[] suits = {
                PlayingCard.Suit.SPADE, PlayingCard.Suit.HEART,
                PlayingCard.Suit.CLUB, PlayingCard.Suit.DIAMOND
        };
        for (int rank = 3; rank <= 15; rank++) {
            for (PlayingCard.Suit suit : suits) {
                deck.add(new PlayingCard(id++, suit, rank));
            }
        }
        deck.add(new PlayingCard(id++, PlayingCard.Suit.JOKER, 16));
        deck.add(new PlayingCard(id, PlayingCard.Suit.JOKER, 17));
        return deck;
    }

    private static List<PlayingCard> immutableCopy(List<PlayingCard> cards) {
        return Collections.unmodifiableList(new ArrayList<>(cards));
    }
}
