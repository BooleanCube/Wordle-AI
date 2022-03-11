# Wordle-AI
> Written AI Algorithms which help with solving Wordle puzzles!

## Wordle
[Wordle](https://www.nytimes.com/games/wordle/index.html) is a fun puzzle game where each day you are given a singular 5 letter word in the English langauge and your goal is to guess the word in 6 attempts. There will be feedback given for each attempt such as (B)lack, (Y)ellow, and (G)reen. Each color means a different thing:
- Black means the letter is not in the word at all
- Yellow means the letter is in the word but not in that position
- Green means the letter is in the word and in the right position.

**Example of me playing Wordle**:

![image](https://user-images.githubusercontent.com/47650058/155051905-5f820085-0bc5-47a0-8f10-b24fc6034a61.png)

*[Wordle Unlimited](https://www.wordleunlimited.com/) allows you to play an unlimited amount of wordle games per day rather than just once per day!*


### Artificial Intelligence Algorithms:

- **Filtering**: <br>
Based on the given feedback the algorithm will filter all possible words until the feedback says the word is correct! After filtering it sorts the list of possible words by letter frequency for each position. `SLATE` being the word with the highest score from the letter position frequencies. There is also a check for similar words that could result in loops such as `found, pound, round, hound, sound, bound` and uses an alternate word containing the most odd letters from the list of similar words and guess that word instead to break the loop and get more hints. The dictionary for this algorithm is the smallwords.txt file in the `src/` folder which only consits of the 2000 words wordle uses.

- **Entropy**: <br>
Each iteration the AI tries to increase the amount of information by decreasing the amount of uncertainty in bits. It calculates the Entropy, or in other words, the average amount of information gained for each possible pattern of feedback. The higher the entropy value of a word, the more information we gain from the word at average. In the end, it will keep gaining information until there is only 1 possible solution left. The dictionary for this algorithm is the `smallwords.txt` file in the `src/` folder which only consits of the 2000 words wordle uses. Credits to [3blue1brown's wordle information theory video](https://youtu.be/v68zYyaEmEA) and [shannon entropy article](https://en.wikipedia.org/wiki/Entropy_(information_theory)) for explaining shannon entropy concepts amazingly. *Currently this algorithm looks very unoptimized and slow.*


### TODO
- [ ] Optimize Entropy Algorithm

*written by BooleanCube :]*
