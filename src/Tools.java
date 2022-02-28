import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Tools {
    public static void printRules() {
        System.out.print("WORDLE AI RULES:\n" +
                "I will attempt to guess the word in as few tries as possible!\n" +
                "However, I do need some feedback on the guesses I make! You can tell me where I was right or wrong by typing back a 5 character word with the letters G, Y, and B.\n" +
                "G = Correct Letter | Correct Position\n" +
                "Y = Correct Letter | Wrong Position  \n" +
                "B = Wrong Letter   | Wrong Position  \n");
    }

    public static void initWordList(ArrayList<String> words) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader("src/words.txt"));
        for(int i=0; i<2315; i++) words.add(bf.readLine());
        bf.close();
    }

    public static HashMap<Character, Integer>[] initFreqList(ArrayList<String> words) {
        HashMap<Character, Integer> frequencyMap[] = new HashMap[5];
        char[] alphabet = new char[]{'a','b','c','d','e','f','g','h', 'i', 'j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        for(int i=0; i<5; i++) {
            int[] charCounter = new int[26];
            for(String word : words) charCounter[(int)word.charAt(i) - 97]++;
            HashMap<Character, Integer> map = new HashMap<>();
            for(int j=0; j<26; j++) map.put(alphabet[j], charCounter[j]);
            frequencyMap[i] = map;
        }
        return frequencyMap;
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
