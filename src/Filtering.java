import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Filtering {

    public static Check known = new Check();
    static ArrayList<String> words = new ArrayList<>();

    static HashMap<Character, Integer>[] letterFrequency = new HashMap[5];

    public static void main(String[] args) throws IOException {
        Tools.initWordList(words);
        letterFrequency = Tools.initFreqList(words);
        Tools.printRules();
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String guess = find(known);
        System.out.println(guess);
        String feedback;
        int tries = 0;
        while((feedback=bf.readLine()) != null) {
            known.update(guess, feedback.toUpperCase());
            guess = find(known);
            System.out.println(guess);
            ++tries;
            if(guess.equalsIgnoreCase("GGs") ||
                    guess.equalsIgnoreCase("There is no 5 letter word I can find that matches this!")) break;
        }
        System.out.println(tries + " tries");
    }

    public static String calculateFilteringStats() throws IOException {
        int[] attemptTracker = new int[10];
        Tools.initWordList(words);
        letterFrequency = Tools.initFreqList(words);
        for(String word : words) {
            int tries = 1;
            Check check = new Check();
            String guess = find(check);
            String feedback = Tools.getFeedback(word, guess);
            while(!feedback.equalsIgnoreCase("GGGGG")) {
                check.update(guess, feedback.toUpperCase());
                if(check.isFull()) break;
                guess = find(check);
                if(guess.equalsIgnoreCase("There is no 5 letter word I can find that matches this!")) { tries = 10; break; }
                feedback = Tools.getFeedback(word, guess);
                ++tries;
            }
            attemptTracker[tries-1]++;
        }
        StringBuilder r = new StringBuilder();
        int win = 0;
        for(int i=0; i<attemptTracker.length; i++) {
            if(i==attemptTracker.length-1) r.append("Failed tries: ").append(attemptTracker[i]).append(" - ").append(attemptTracker[i] / 2315d * 100d).append(" %\n");
            else r.append(i+1).append(" tries: ").append(attemptTracker[i]).append(" - ").append(attemptTracker[i] / 2315d * 100d).append(" %\n");
            if(i<6) win += attemptTracker[i];
        }
        r.append("Win Percentage: ").append(win/2315d*100d).append(" %\n");
        r.append("---------------------------------------------------\n");
        return r.toString();
    }

    static String find(Check known) {
        if(known == null) return "Bad feedback.. read the rules!";
        if(known.isFull()) return "GGs";
        ArrayList<String> possible = new ArrayList<>();
        for(String word : words) if(compare(word, known)) possible.add(word);
        for(String word : known.used) possible.remove(word);
        if(possible.isEmpty()) return "There is no 5 letter word I can find that matches this!";
        letterFrequency = Tools.initFreqList(possible);
        possible.sort(Comparator.comparingDouble(Filtering::score));
        String unique = uniqueForSimilar(possible);
        if(unique != null) return unique;
        known.used.add(possible.get(0));
        return possible.get(0);
    }

    static String uniqueForSimilar(ArrayList<String> possible) {
        char[] letters = possible.get(0).toCharArray();
        ArrayList<Character> oddBalls = new ArrayList<>();
        for(String word : possible) {
            char[] sw = word.toCharArray();
            for(int i=0; i<5; i++) {
                if (letters[i] != sw[i]) {
                    letters[i] = '\u0000';
                    oddBalls.add(sw[i]);
                }
            }
        }
        int count = 0;
        for(char c : letters) if(c != '\u0000') count++;
        if(count >= 3 && possible.size() > 2) {
            ArrayList<String> newPossible = new ArrayList<>(words);
            newPossible.sort(Comparator.comparingInt(f -> scoreUnique(f, oddBalls)));
            return newPossible.get(0);
        }
        return null;
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
            int count = Tools.countOccurences(guess, bc.value);
            if(count > 0)
                return false;
        }
        return true;
    }

    static double score(String word) {
        double score = 0d;
        for(int i=0; i<5; i++) score -= letterFrequency[i].get(word.charAt(i));
        return score;
    }

    static int scoreUnique(String word, ArrayList<Character> oddBalls) {
        int sum = 0;
        for(char c : oddBalls) if(word.indexOf(c) > -1) sum -= 1;
        return sum;
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
                for(int j=0; j<black.size(); j++) {
                    BlackCharacter bc = black.get(j);
                    if(bc.value == gc) {
                        black.remove(bc);
                        break;
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
                            met = true;
                            break;
                        }
                    }
                    if(!met) {
                        for(YellowCharacter yc : yellow) {
                            if(yc.value == gc) {
                                met = true;
                                break;
                            }
                        }
                        if(!met) black.add(new BlackCharacter(gc));
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
    public int count = 0;
    public BlackCharacter(char val) {
        value = val;
    }
}
