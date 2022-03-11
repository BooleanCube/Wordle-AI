import java.util.ArrayList;

class Check {
    public ArrayList<GreenCharacter> green;
    public ArrayList<YellowCharacter> yellow;
    public ArrayList<BlackCharacter> black;
    public ArrayList<String> used = new ArrayList<>();

    public Check() {
        green = new ArrayList<>();
        yellow = new ArrayList<>();
        black = new ArrayList<>();
    }

    public void update(String guess, String feedback) {
        for (int i = 0; i < feedback.length(); i++) {
            char fb = feedback.charAt(i);
            char gc = guess.charAt(i);
            if (fb == 'G') {
                yellow.removeIf(yc -> yc.value == gc);
                boolean met = false;
                for (GreenCharacter gch : green) {
                    if (gch.idx == i) {
                        met = true;
                        break;
                    }
                }
                if (!met) {
                    for (int j = 0; j < black.size(); j++) {
                        BlackCharacter bc = black.get(j);
                        if (bc.value == gc) {
                            black.remove(bc);
                            break;
                        }
                    }
                    green.add(new GreenCharacter(gc, i));
                }

            } else if (fb == 'Y') {
                boolean met = false;
                for (YellowCharacter yc : yellow) {
                    if (yc.value == gc) {
                        yc.notIdx.add(i);
                        met = true;
                    }
                }
                for (int j = 0; j < black.size(); j++) {
                    BlackCharacter bc = black.get(j);
                    if (bc.value == gc) {
                        black.remove(bc);
                        break;
                    }
                }
                if (!met) yellow.add(new YellowCharacter(gc, i));
            } else if (fb == 'B') {
                boolean met = false;
                for (BlackCharacter bc : black) {
                    if (bc.value == gc) {
                        met = true;
                        break;
                    }
                }
                if (!met) {
                    for (GreenCharacter gch : green) {
                        if (gch.value == gc) {
                            met = true;
                            break;
                        }
                    }
                    if (!met) {
                        for (YellowCharacter yc : yellow) {
                            if (yc.value == gc) {
                                met = true;
                                break;
                            }
                        }
                        if (!met) black.add(new BlackCharacter(gc));
                    }
                }
            } else Filtering.known = null;
        }
    }

    public boolean isEmpty() {
        return green.isEmpty() && yellow.isEmpty();
    }

    public boolean isFull() {
        return green.size() == 5;
    }

}
