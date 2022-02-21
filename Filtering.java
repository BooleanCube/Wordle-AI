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
    static ArrayList<String> used = new ArrayList<>();

    static HashMap<String, Double> letterFrequency = new HashMap<>();

    public static void main(String[] args) throws IOException {
        initWordList();
        initFreqList();
        printRules();
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

    static String find(Check known) {
        if(known == null) return "Bad feedback.. read the rules!";
        if(known.isFull()) return "GGs";
        ArrayList<String> possible = new ArrayList<>();
        if(known.isEmpty()) Collections.addAll(possible, openers);
        else for(String word : words) if(compare(word, known)) possible.add(word);
        for(String word : used) possible.remove(word);
        if(possible.isEmpty()) return "There is no 5 letter word I can find that matches this!";
        possible.sort(Comparator.comparingDouble(Filtering::score));
        used.add(possible.get(0));
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
            if(bc.idx > -1 && guess.indexOf(bc.value) != guess.lastIndexOf(bc.value))
                return false;
            else if(bc.idx == -1 && guess.indexOf(bc.value) > -1)
                return false;
        }
        return true;
    }

    static double score(String word) {
        double score = 0d;
        for(char c : word.toCharArray()) score -= letterFrequency.get(String.valueOf(c));
        return score;
    }

    static void printRules() {
        System.out.print("WORDLE AI RULES:\n" +
                "I will attempt to guess the word in as few tries as possible!\n" +
                "However, I do need some feedback on the guesses I make! You can tell me where I was right or wrong by typing back a 5 character word with the letters G,Y, and B.\n" +
                "G = Correct Letter | Correct Position\n" +
                "Y = Correct Letter | Wrong Position  \n" +
                "B = Wrong Letter   | Wrong Position  \n");
    }

    static void initWordList() throws IOException {
        URL db = new URL("https://www-cs-faculty.stanford.edu/~knuth/sgb-words.txt");
        BufferedReader bf = new BufferedReader(new InputStreamReader(db.openStream()));
        for(int i=0; i<5757; i++) words.add(bf.readLine());
        bf.close();
    }

    static void initFreqList() {
        letterFrequency.put("e", 11.1607);
        letterFrequency.put("a", 8.4966);
        letterFrequency.put("r", 7.5809);
        letterFrequency.put("i", 7.5448);
        letterFrequency.put("o", 7.1635);
        letterFrequency.put("t", 6.9509);
        letterFrequency.put("n", 6.6544);
        letterFrequency.put("s", 5.7351);
        letterFrequency.put("l", 5.4893);
        letterFrequency.put("c", 4.5388);
        letterFrequency.put("u", 3.6308);
        letterFrequency.put("d", 3.3844);
        letterFrequency.put("p", 3.1671);
        letterFrequency.put("m", 3.0129);
        letterFrequency.put("h", 3.0034);
        letterFrequency.put("g", 2.4705);
        letterFrequency.put("b", 2.0720);
        letterFrequency.put("f", 1.8121);
        letterFrequency.put("y", 1.7779);
        letterFrequency.put("w", 1.2899);
        letterFrequency.put("k", 1.1016);
        letterFrequency.put("v", 1.0074);
        letterFrequency.put("x", 0.2902);
        letterFrequency.put("z", 0.2722);
        letterFrequency.put("j", 0.1965);
        letterFrequency.put("q", 0.1962);
    }

}

class Check {
    public ArrayList<GreenCharacter> green;
    public ArrayList<YellowCharacter> yellow;
    public ArrayList<BlackCharacter> black;

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
                            black.set(j, new BlackCharacter(gc, (int)bc.tempIdx));
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
                            black.add(new BlackCharacter(gc, i));
                            met = true;
                            break;
                        }
                    }
                    if(!met) {
                        for(YellowCharacter yc : yellow){
                            if(yc.value == gc) {
                                black.add(new BlackCharacter(gc, i));
                                met = true;
                                break;
                            }
                        }
                        if(!met) black.add(new BlackCharacter(gc, (long)i));
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
    public long tempIdx = -1;
    public BlackCharacter(char val) {
        value = val;
    }
    public BlackCharacter(char val, int idx) {
        value = val;
        this.idx = idx;
    }
    public BlackCharacter(char val, long idx) {
        value = val;
        this.tempIdx = idx;
    }
}