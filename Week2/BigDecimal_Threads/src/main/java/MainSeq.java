public class MainSeq {

    public static void main(String[] args) {
        // calculate n roots with precision s
        BigMath.calculateThirdRootsSequentially(250, 1000);
        //BigMath.calculateThirdRootsSequentially(25, 2500);

        //BigMath.calculateThirdRootsSequentially(7, 2275);   // 7.5s
        //BigMath.calculateThirdRootsSequentially(7, 3050);   // 15s
        //BigMath.calculateThirdRootsSequentially(7, 4000);   // 30s
        //BigMath.calculateThirdRootsSequentially(7, 5150);   // 60s
    }
}
