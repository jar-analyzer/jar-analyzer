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

package me.n1ar4.games.pocker;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class Time extends Thread {
    Main main;
    boolean isRun = true;
    int i;

    public Time(Main m, int i) {
        this.main = m;
        this.i = i;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (i > -1 && isRun) {
            main.time[1].setText("倒计时:" + i--);
            second(1);
        }
        if (i == -1)
            main.time[1].setText("不抢");
        main.landlord[0].setVisible(false);
        main.landlord[1].setVisible(false);
        for (Card card2 : main.playerList[1])
            card2.canClick = true;
        if (main.time[1].getText().equals("抢地主")) {
            main.playerList[1].addAll(main.lordList);
            openlord(true);
            second(2);
            Common.order(main.playerList[1]);
            Common.rePosition(main, main.playerList[1], 1);
            main.publishCard[1].setEnabled(false);
            setlord(1);
        } else {
            if (Common.getScore(main.playerList[0]) < Common
                    .getScore(main.playerList[2])) {
                main.time[2].setText("抢地主");
                main.time[2].setVisible(true);
                setlord(2);
                openlord(true);
                second(3);
                main.playerList[2].addAll(main.lordList);
                Common.order(main.playerList[2]);
                Common.rePosition(main, main.playerList[2], 2);
                openlord(false);
            } else {
                main.time[0].setText("抢地主");
                main.time[0].setVisible(true);
                setlord(0);
                openlord(true);
                second(3);
                main.playerList[0].addAll(main.lordList);
                Common.order(main.playerList[0]);
                Common.rePosition(main, main.playerList[0], 0);
                openlord(false);
            }
        }
        main.landlord[0].setVisible(false);
        main.landlord[1].setVisible(false);
        turnOn(false);
        for (int i = 0; i < 3; i++) {
            main.time[i].setText("不要");
            main.time[i].setVisible(false);
        }
        main.turn = main.dizhuFlag;
        while (true) {
            if (main.turn == 1) {
                if (main.time[0].getText().equals("不要")
                        && main.time[2].getText().equals("不要"))
                    main.publishCard[1].setEnabled(false);
                else {
                    main.publishCard[1].setEnabled(true);
                }
                turnOn(true);
                timeWait(30, 1);
                turnOn(false);
                main.turn = (main.turn + 1) % 3;
                if (win())
                    break;
            }
            if (main.turn == 0) {
                computer0();
                main.turn = (main.turn + 1) % 3;
                if (win())
                    break;
            }
            if (main.turn == 2) {
                computer2();
                main.turn = (main.turn + 1) % 3;
                if (win())
                    break;
            }
        }
    }

    @SuppressWarnings("all")
    public void second(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public void openlord(boolean is) {
        for (int i = 0; i < 3; i++) {
            if (is)
                main.lordList.get(i).turnFront();
            else {
                main.lordList.get(i).turnRear();
            }
            main.lordList.get(i).canClick = true;
        }
    }


    public void setlord(int i) {
        Point point = new Point();
        if (i == 1) {
            point.x = 80;
            point.y = 430;
            main.dizhuFlag = 1;
        }
        if (i == 0) {
            point.x = 80;
            point.y = 20;
            main.dizhuFlag = 0;
        }
        if (i == 2) {
            point.x = 700;
            point.y = 20;
            main.dizhuFlag = 2;
        }
        main.dizhu.setLocation(point);
        main.dizhu.setVisible(true);
    }


    public void turnOn(boolean flag) {
        main.publishCard[0].setVisible(flag);
        main.publishCard[1].setVisible(flag);
    }


    public void computer0() {
        timeWait(1, 0);
        ShowCard(0);

    }


    public void computer2() {
        timeWait(1, 2);
        ShowCard(2);

    }

    @SuppressWarnings("all")
    public void ShowCard(int role) {
        int orders[] = new int[]{4, 3, 2, 1, 5};
        Model model = Common.getModel(main.playerList[role], orders);

        List<String> list = new Vector<>();

        if (main.time[(role + 1) % 3].getText().equals("不要")
                && main.time[(role + 2) % 3].getText().equals("不要")) {

            if (model.a123.size() > 0) {
                list.add(model.a123.get(model.a123.size() - 1));
            } else if (model.a3.size() > 0) {

                if (model.a1.size() > 0) {
                    list.add(model.a1.get(model.a1.size() - 1));
                } else if (model.a2.size() > 0) {
                    list.add(model.a2.get(model.a2.size() - 1));
                }
                list.add(model.a3.get(model.a3.size() - 1));
            } else if (model.a112233.size() > 0) {
                list.add(model.a112233.get(model.a112233.size() - 1));
            } else if (model.a111222.size() > 0) {
                String name[] = model.a111222.get(0).split(",");

                if (name.length / 3 <= model.a1.size()) {
                    list.add(model.a111222.get(model.a111222.size() - 1));
                    for (int i = 0; i < name.length / 3; i++)
                        list.add(model.a1.get(i));
                } else if (name.length / 3 <= model.a2.size()) {
                    list.add(model.a111222.get(model.a111222.size() - 1));
                    for (int i = 0; i < name.length / 3; i++)
                        list.add(model.a2.get(i));
                }

            } else if (model.a2.size() > (model.a111222.size() * 2 + model.a3
                    .size())) {
                list.add(model.a2.get(model.a2.size() - 1));
            } else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size())) {
                list.add(model.a1.get(model.a1.size() - 1));
            } else if (model.a4.size() > 0) {

                int sizea1 = model.a1.size();
                int sizea2 = model.a2.size();
                if (sizea1 >= 2) {
                    list.add(model.a1.get(sizea1 - 1));
                    list.add(model.a1.get(sizea1 - 2));
                    list.add(model.a4.get(0));

                } else if (sizea2 >= 2) {
                    list.add(model.a2.get(sizea1 - 1));
                    list.add(model.a2.get(sizea1 - 2));
                    list.add(model.a4.get(0));

                } else {
                    list.add(model.a4.get(0));

                }

            }
        } else {


            if (role != main.dizhuFlag) {
                int f = 0;
                if (main.time[main.dizhuFlag].getText().equals("不要")) {
                    f = 1;
                }
                if ((role + 1) % 3 == main.dizhuFlag) {
                    if ((Common.jugdeType(main.currentList[(role + 2) % 3]) != CardType.c1 ||
                            Common.jugdeType(main.currentList[(role + 2) % 3]) != CardType.c2)
                            && main.currentList[main.dizhuFlag].size() < 1)
                        f = 1;
                    if (main.currentList[(role + 2) % 3].size() > 0 &&
                            Common.getValue(main.currentList[(role + 2) % 3].get(0)) > 13)
                        f = 1;
                }
                if (f == 1) {
                    main.time[role].setVisible(true);
                    main.time[role].setText("不要");
                    return;
                }
            }

            int can = 0;
            if (role == main.dizhuFlag) {
                if (main.playerList[(role + 1) % 3].size() <= 5
                        || main.playerList[(role + 2) % 3].size() <= 5)
                    can = 1;
            } else {
                if (main.playerList[main.dizhuFlag].size() <= 5)
                    can = 1;
            }

            List<Card> player;
            if (main.time[(role + 2) % 3].getText().equals("不要"))
                player = main.currentList[(role + 1) % 3];
            else
                player = main.currentList[(role + 2) % 3];


            CardType cType = Common.jugdeType(player);

            if (cType == CardType.c1) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            1, 4, 3, 2, 5});
                AI_1(model.a1, player, list, role);
            } else if (cType == CardType.c2) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            2, 4, 3, 5, 1});
                AI_1(model.a2, player, list, role);
            } else if (cType == CardType.c3) {
                AI_1(model.a3, player, list, role);
            } else if (cType == CardType.c4) {
                AI_1(model.a4, player, list, role);
            } else if (cType == CardType.c31) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            3, 1, 4, 2, 5});
                AI_2(model.a3, model.a1, player, list, role);
            } else if (cType == CardType.c32) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            3, 2, 4, 5, 1});
                AI_2(model.a3, model.a2, player, list, role);
            } else if (cType == CardType.c411) {
                AI_5(model.a4, model.a1, player, list, role);
            } else if (cType == CardType.c422) {
                AI_5(model.a4, model.a2, player, list, role);
            } else if (cType == CardType.c123) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            5, 3, 2, 4, 1});
                AI_3(model.a123, player, list, role);
            } else if (cType == CardType.c1122) {
                if (can == 1)
                    model = Common.getModel(main.playerList[role], new int[]{
                            2, 4, 3, 5, 1});
                AI_3(model.a112233, player, list, role);
            } else if (cType == CardType.c11122234) {
                AI_4(model.a111222, model.a1, player, list, role);
            } else if (cType == CardType.c1112223344) {
                AI_4(model.a111222, model.a2, player, list, role);
            }
            if (list.size() == 0 && can == 1) {
                int len4 = model.a4.size();
                if (len4 > 0)
                    list.add(model.a4.get(len4 - 1));
            }
        }
        main.currentList[role].clear();
        if (list.size() > 0) {
            Point point = new Point();
            if (role == 0)
                point.x = 200;
            if (role == 2)
                point.x = 550;
            if (role == 1) {
                point.x = (770 / 2) - (main.currentList[1].size() + 1) * 15 / 2;
                point.y = 300;
            }
            point.y = (400 / 2) - (list.size() + 1) * 15 / 2;
            List<Card> temp = new Vector<Card>();
            for (int i = 0, len = list.size(); i < len; i++) {
                List<Card> cards = getCardByName(main.playerList[role],
                        list.get(i));
                for (Card card : cards) {
                    temp.add(card);
                }
            }
            temp = Common.getOrder2(temp);
            for (Card card : temp) {
                Common.move(card, card.getLocation(), point);
                point.y += 15;
                main.container.setComponentZOrder(card, 0);
                main.currentList[role].add(card);
                main.playerList[role].remove(card);
            }
            Common.rePosition(main, main.playerList[role], role);
        } else {
            main.time[role].setVisible(true);
            main.time[role].setText("不要");
        }
        for (Card card : main.currentList[role])
            card.turnFront();
    }

    @SuppressWarnings("all")
    public List getCardByName(List<Card> list, String n) {
        String[] name = n.split(",");
        List<Card> cardsList = new Vector<>();
        int j = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if (j < name.length && list.get(i).name.equals(name[j])) {
                cardsList.add(list.get(i));
                i = 0;
                j++;
            }
        }
        return cardsList;
    }

    @SuppressWarnings("all")
    public void AI_3(List<String> model, List<Card> player, List<String> list,
                     int role) {
        for (int i = 0, len = model.size(); i < len; i++) {
            String[] s = model.get(i).split(",");
            if (s.length == player.size()
                    && getValueInt(model.get(i)) > Common.getValue(player
                    .get(0))) {
                list.add(model.get(i));
                return;
            }
        }
    }

    @SuppressWarnings("all")
    public void AI_4(List<String> model1, List<String> model2,
                     List<Card> player, List<String> list, int role) {
        player = Common.getOrder2(player);
        int len1 = model1.size();
        int len2 = model2.size();

        if (len1 < 1 || len2 < 1)
            return;
        for (int i = 0; i < len1; i++) {
            String[] s = model1.get(i).split(",");
            String[] s2 = model2.get(0).split(",");
            if ((s.length / 3 <= len2)
                    && (s.length * (3 + s2.length) == player.size())
                    && getValueInt(model1.get(i)) > Common.getValue(player
                    .get(0))) {
                list.add(model1.get(i));
                for (int j = 1; j <= s.length / 3; j++)
                    list.add(model2.get(len2 - j));
            }
        }
    }

    @SuppressWarnings("all")
    public void AI_5(List<String> model1, List<String> model2,
                     List<Card> player, List<String> list, int role) {
        player = Common.getOrder2(player);
        int len1 = model1.size();
        int len2 = model2.size();

        if (len1 < 1 || len2 < 2)
            return;
        for (int i = 0; i < len1; i++) {
            if (getValueInt(model1.get(i)) > Common.getValue(player.get(0))) {
                list.add(model1.get(i));
                for (int j = 1; j <= 2; j++)
                    list.add(model2.get(len2 - j));
            }
        }
    }

    @SuppressWarnings("all")
    public void AI_1(List<String> model, List<Card> player, List<String> list,
                     int role) {
        for (int len = model.size(), i = len - 1; i >= 0; i--) {
            if (getValueInt(model.get(i)) > Common.getValue(player.get(0))) {
                list.add(model.get(i));
                break;
            }
        }

    }

    @SuppressWarnings("all")
    public void AI_2(List<String> model1, List<String> model2,
                     List<Card> player, List<String> list, int role) {
        player = Common.getOrder2(player);
        int len1 = model1.size();
        int len2 = model2.size();

        if (len1 > 0 && model1.get(0).length() < 10) {
            list.add(model1.get(0));
            System.out.println("王炸");
            return;
        }
        if (len1 < 1 || len2 < 1)
            return;
        for (int len = len1, i = len - 1; i >= 0; i--) {
            if (getValueInt(model1.get(i)) > Common.getValue(player.get(0))) {
                list.add(model1.get(i));
                break;
            }
        }
        list.add(model2.get(len2 - 1));
        if (list.size() < 2)
            list.clear();
    }

    @SuppressWarnings("all")
    public void timeWait(int n, int player) {
        if (main.currentList[player].size() > 0)
            Common.hideCards(main.currentList[player]);
        if (player == 1) {
            int i = n;

            while (main.nextPlayer == false && i >= 0) {

                main.time[player].setText("倒计时:" + i);
                main.time[player].setVisible(true);
                second(1);
                i--;
            }
            if (i == -1 && player == 1) {
                ShowCard(1);
            }
            main.nextPlayer = false;
        } else {
            for (int i = n; i >= 0; i--) {
                second(1);
                main.time[player].setText("倒计时:" + i);
                main.time[player].setVisible(true);
            }
        }
        main.time[player].setVisible(false);
    }

    @SuppressWarnings("all")
    public int getValueInt(String n) {
        String name[] = n.split(",");
        String s = name[0];
        int i = Integer.parseInt(s.substring(2, s.length()));
        if (s.substring(0, 1).equals("5"))
            i += 3;
        if (s.substring(2, s.length()).equals("1")
                || s.substring(2, s.length()).equals("2"))
            i += 13;
        return i;
    }

    public boolean win() {
        if (Main.closed()) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            if (main.playerList[i].isEmpty()) {
                String s;
                if (i == 1) {
                    s = "恭喜你，胜利了!";
                } else {
                    s = "恭喜电脑" + i + ",赢了!";
                }
                for (int j = 0; j < main.playerList[(i + 1) % 3].size(); j++)
                    main.playerList[(i + 1) % 3].get(j).turnFront();
                for (int j = 0; j < main.playerList[(i + 2) % 3].size(); j++)
                    main.playerList[(i + 2) % 3].get(j).turnFront();
                JOptionPane.showMessageDialog(main, s);
                return true;
            }
        }
        return false;
    }
}
