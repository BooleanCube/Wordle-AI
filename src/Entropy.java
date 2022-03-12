import models.BlackCharacter;
import models.Check;
import models.GreenCharacter;
import models.YellowCharacter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Entropy {

    public static Check known = new Check();
    static ArrayList<String> words = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Tools.initWordList(words);
        //letterFrequency = Tools.initFreqList(words);
        Tools.printRules();
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String guess = "raise"; //find(known);
        System.out.println(guess);
        String feedback;
        int tries = 1;
        while((feedback=bf.readLine()) != null) {
            known.update(guess, feedback.toUpperCase());
            guess = find(known);
            System.out.println(guess);
            if(guess.equalsIgnoreCase("GGs") ||
                    guess.equalsIgnoreCase("There is no 5 letter word I can find that matches this!")) break;
            ++tries;
        }
        System.out.println(tries + " tries");
    }

    public static String calculateEntropyStats() throws IOException {
        int[] attemptTracker = new int[10000];
        Tools.initWordList(words);
        for(String word : words) {
            int tries = 1;
            Check check = new Check();
            //replaced with just raise to increase time complexity because the first guess is always given as raise by the AI
            String guess = "raise"; //find(known)
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
        ArrayList<String> possible = getPossible(known, words);
        if(possible.size() == 1) {
            known.used.add(possible.get(0));
            return possible.get(0);
        }
        if(possible.isEmpty()) return "There is no 5 letter word I can find that matches this!";

        HashMap<Double, ArrayList<String>> entropyMap = new HashMap<>();
        ArrayList<Double> entropyValues = new ArrayList<>();
        for(String word : words) {
            double entropy = calculateEntropy(word, possible);
            if(!entropyValues.contains(entropy)) {
                entropyValues.add(entropy);
                ArrayList<String> mapValue = new ArrayList<>(); mapValue.add(word);
                entropyMap.put(entropy, mapValue);
            } else entropyMap.get(entropy).add(word);
        }
        entropyValues.sort(Collections.reverseOrder());
        possible.clear();
        for(double entropy : entropyValues) possible.addAll(entropyMap.get(entropy));

        for(String word : known.used) possible.remove(word);

        known.used.add(possible.get(0));
        return possible.get(0);
    }

    static ArrayList<String> getPossible(Check known, ArrayList<String> w) {
        ArrayList<String> possible = new ArrayList<>();
        if(known == null) return null;
        if(known.isFull()) return possible;
        for(String word : w) if(compare(word, known)) possible.add(word);
        for(String word : known.used) possible.remove(word);
        if(possible.isEmpty()) return possible;
        return possible;
    }

    static double calculateEntropy(String word, ArrayList<String> possible) {
        double entropy = 0.0;
        ArrayList<Check> patterns = new ArrayList<>();
        getCheckPatterns(patterns, word, "", new char[]{'B','Y','G'});
        for(Check pattern : patterns) {
            double p = (double)getPossible(pattern, possible).size() / possible.size();
            if(p > 0) entropy += (p * (Math.log(1/p) / Math.log(2)))/letterCountCurve(countRepetitiveLetters(word));
        }
        return entropy;
    }

    static double letterCountCurve(int x) {
        return 2 * Math.pow(x-1, 2) + 1;
    }

    static int countRepetitiveLetters(String word) {
        HashSet<Character> visited = new HashSet<>();
        int counter = 6;
        for(char c : word.toCharArray()) {
            if(!visited.contains(c)) {
                counter--;
                visited.add(c);
            }
        }
        return counter;
    }

    static void getCheckPatterns(ArrayList<Check> patterns, String word, String pattern, char[] types) {
        if(pattern.length()>=5) {
            Check check = new Check();
            check.update(word, pattern);
            patterns.add(check);
            return;
        }
        for(char c : types)
            getCheckPatterns(patterns, word, pattern+c, types);
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

}
