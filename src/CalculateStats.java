import java.io.IOException;

public class CalculateStats {

    public static void main(String[] args) throws IOException {
        System.out.println("Filtering Algorithm Statistics:\n");
        System.out.println(Filtering.calculateFilteringStats());
        System.out.println("Entropy Algorithm Statistics:\n");
        System.out.println(Entropy.calculateEntropyStats());
    }

}
