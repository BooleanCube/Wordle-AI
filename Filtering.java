import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Filtering {

    public static Check known = new Check();
    static String[] openers = {"adieu", "words", "crypt"};
    static ArrayList<String> words = new ArrayList<>();

    static HashMap<Character, Double> letterFrequency = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Tools.initWordList(words);
        Tools.initFreqList(words, letterFrequency);
        Tools.printRules();
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String guess = find(known);
        System.out.println(guess);
        String feedback;
        while((feedback=bf.readLine()) != null) {
            known.update(guess, feedback.toUpperCase());
            guess = find(known);
            System.out.println(guess);
            if(guess.equalsIgnoreCase("GGs") ||
                    guess.equalsIgnoreCase("There is no 5 letter word I can find that matches this!")) break;
        }
    }

    public static String calculateFilteringStats() throws IOException {
        int[] attemptTracker = new int[17];
        Tools.initWordList(words);
        Tools.initFreqList(words, letterFrequency);
        for(String word : words) {
            if(word.equalsIgnoreCase("faxer"))
                word = word;
            int tries = 1;
            Check check = new Check();
            String guess = find(check);
            String feedback = Tools.getFeedback(word, guess);
            while(!feedback.equalsIgnoreCase("GGGGG")) {
                check.update(guess, feedback.toUpperCase());
                if(check.isFull()) break;
                guess = find(check);
                if(guess.equalsIgnoreCase("There is no 5 letter word I can find that matches this!")) { tries = 17; break; }
                feedback = Tools.getFeedback(word, guess);
                ++tries;
            }
            attemptTracker[tries-1]++;
        }
        StringBuilder r = new StringBuilder();
        int win = 0;
        for(int i=0; i<attemptTracker.length; i++) {
            r.append(i+1).append(" tries: ").append(attemptTracker[i]).append(" - ").append(attemptTracker[i] / 5757d).append("%\n");
            if(i<6) win += attemptTracker[i];
        }
        r.append("\n").append("Win Percentage: ").append(win/5757f).append("%\n");
        return r.toString();
    }

    static String find(Check known) {
        if(known == null) return "Bad feedback.. read the rules!";
        if(known.isFull()) return "GGs";
        ArrayList<String> possible = new ArrayList<>();
        if(known.isEmpty()) Collections.addAll(possible, openers);
        else for(String word : words) if(compare(word, known)) possible.add(word);
        for(String word : known.used) possible.remove(word);
        if(possible.isEmpty()) return "There is no 5 letter word I can find that matches this!";
        possible.sort(Comparator.comparingDouble(Filtering::score));
        known.used.add(possible.get(0));
        return possible.get(0);
    }

    static boolean compare(String guess, Check check) {
        for(GreenCharacter gc : check.green)
            if(gc.value != guess.charAt(gc.idx))
                return false;
        for(YellowCharacter yc : check.yellow) {
            if(guess.indexOf(yc.value) == -1)
                return false;
            for(int idx : yc.notIdx)
                if(guess.charAt(idx) == yc.value)
                    return false;
        }
        for(BlackCharacter bc : check.black) {
            if(bc.idx > -1 && guess.indexOf(bc.value) == bc.tempIdx)
                return false;
            else if(bc.idx == -1 && guess.indexOf(bc.value) > -1)
                return false;
        }
        return true;
    }

    static double score(String word) {
        double score = 0d;
        for(char c : word.toCharArray()) score -= letterFrequency.get(c);
        return score;
    }



}

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
        for(int i=0; i<feedback.length(); i++) {
            char fb = feedback.charAt(i);
            char gc = guess.charAt(i);
            if(fb == 'G') {
                yellow.removeIf(yc -> yc.value == gc);
                boolean met = false;
                for(GreenCharacter gch : green) {
                    if(gch.idx == i) {
                        met = true;
                        break;
                    }
                }
                if(!met) {
                    for(int j=0; j<black.size(); j++) {
                        BlackCharacter bc = black.get(j);
                        if(bc.value == gc) {
                            black.remove(bc);
                            black.add(j, new BlackCharacter(gc, bc.tempIdx));
                            break;
                        }
                    }
                    green.add(new GreenCharacter(gc, i));
                }

            } else if(fb == 'Y') {
                boolean met = false;
                for(YellowCharacter yc : yellow) {
                    if(yc.value == gc) {
                        yc.notIdx.add(i);
                        met = true;
                    }
                }
                if(!met) yellow.add(new YellowCharacter(gc, i));
            } else if(fb == 'B') {
                boolean met = false;
                for(BlackCharacter bc : black) {
                    if (bc.value == gc) {
                        met = true;
                        break;
                    }
                }
                if(!met) {
                    for(GreenCharacter gch : green) {
                        if(gch.value == gc) {
                            BlackCharacter blc = new BlackCharacter(gc, i);
                            blc.tempIdx = gch.idx;
                            black.add(blc);
                            met = true;
                            break;
                        }
                    }
                    if(!met) {
                        BlackCharacter blc = new BlackCharacter(gc);
                        blc.tempIdx = i;
                        black.add(blc);
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

class YellowCharacter {
    public ArrayList<Integer> notIdx = new ArrayList<>();
    public char value;
    public YellowCharacter(char val, int idx) {
        notIdx.add(idx);
        value = val;
    }
}
class GreenCharacter {
    public int idx;
    public char value;
    public GreenCharacter(char val, int idx) {
        this.idx = idx;
        value = val;
    }
}
class BlackCharacter {
    public char value;
    public int idx = -1;
    public int tempIdx = -1;
    public BlackCharacter(char val) {
        value = val;
    }
    public BlackCharacter(char val, int idx) {
        value = val;
        this.idx = idx;
    }
}