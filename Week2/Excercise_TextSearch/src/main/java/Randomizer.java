import java.util.Random;

public class Randomizer extends Random {

    public Randomizer() {
        super();
    }
    public Randomizer(long seed) {
        super(seed);
    }
    /**
     * creates some random word out of upper and lower case characters.
     * @param wordLength
     * @return
     */
    public String nextWord(int wordLength) {
        String word = "";
        for (int i = 0; i < wordLength; i++) {
            word += this.nextChar();
        }
        return word;
    }
    public char nextChar() {
        return (char)((this.nextBoolean() ? (int)'A' : (int)'a') + this.nextInt(26));
    }
}
