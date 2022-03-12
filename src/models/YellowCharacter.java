package models;

import java.util.ArrayList;

public class YellowCharacter {
    public ArrayList<Integer> notIdx = new ArrayList<>();
    public char value;

    public YellowCharacter(char val, int idx) {
        notIdx.add(idx);
        value = val;
    }
}
