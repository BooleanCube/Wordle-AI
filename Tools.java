import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Tools {
    public static void printRules() {
        System.out.print("WORDLE AI RULES:\n" +
                "I will attempt to guess the word in as few tries as possible!\n" +
                "However, I do need some feedback on the guesses I make! You can tell me where I was right or wrong by typing back a 5 character word with the letters G,Y, and B.\n" +
                "G = Correct Letter | Correct Position\n" +
                "Y = Correct Letter | Wrong Position  \n" +
                "B = Wrong Letter   | Wrong Position  \n");
    }

    public static void initWordList(ArrayList<String> words) throws IOException {
        URL db = new URL("https://www-cs-faculty.stanford.edu/~knuth/sgb-words.txt");
        BufferedReader bf = new BufferedReader(new InputStreamReader(db.openStream()));
        for(int i=0; i<5757; i++) words.add(bf.readLine());
        bf.close();
    }

    public static void initFreqList(ArrayList<String> words, HashMap<Character, Double> letterFrequency) {
        char[] alphabet = new char[]{'a','b','c','d','e','f','g','h','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

        //calculated with 5k list of words
        double[] charCount = new double[]{2348d, 715d, 964d, 1181d, 3009d, 561d, 679d, 814d, 1592d, 89d, 596d, 1586d, 843d, 1285d, 1915d, 955d, 53d, 1910d, 3033d, 1585d, 1089d, 318d, 505d, 139d, 886d, 135d};
        double total = 5757d*5d;
        for(int i=97; i<123; i++) letterFrequency.put((char)i, charCount[i-97]/total);
    }

    public static String getFeedback(String word, String guess) {
        StringBuilder r = new StringBuilder();
        for(int i=0; i<5; i++) {
            char w = word.charAt(i);
            char g = guess.charAt(i);
            if(w != g) {
                if(word.indexOf(g) > -1) r.append('Y');
                else r.append('B');
            } else {
                r.append('G');
                int idx = guess.substring(0, i).lastIndexOf(g);
                if(idx > -1) {
                    if(r.charAt(idx) == 'G') continue;
                    if(countOccurences(word.substring(0, i+1), g) >= countOccurences(guess.substring(0, i+1), g)) r = new StringBuilder(r.substring(0, idx) + 'Y' + r.substring(idx+1));
                    else r = new StringBuilder(r.substring(0, idx) + 'B' + r.substring(idx+1));
                }
            }
        }
        return r.toString();
    }

    public static int countOccurences(String s, char c) {
        int count = 0;
        while(s.indexOf(c) > -1) {
            ++count;
            s = s.substring(s.indexOf(c)+1);
        }
        return count;
    }

}
