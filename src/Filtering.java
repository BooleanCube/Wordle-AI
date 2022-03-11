import java.io.*;
import java.util.ArrayList;
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
        System.out.println(guess);
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
        String unique = uniqueForSimilar(possible, known);
        if(unique != null) return unique;
        known.used.add(possible.get(0));
        return possible.get(0);
    }

    static String uniqueForSimilar(ArrayList<String> possible, Check check) {
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
            for(String word : check.used) newPossible.remove(word);
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

