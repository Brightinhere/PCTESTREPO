public class MainPar {

    public static void main(String[] args) throws InterruptedException {
        // calculate n roots with precision s
        BigMath.calculateThirdRootsConcurrently(250, 2000);
        //BigMath.calculateThirdRootsConcurrently(25, 2500);

        //BigMath.calculateThirdRootsConcurrently(7, 2275);   // 7.5s
        //BigMath.calculateThirdRootsConcurrently(7, 3050);   // 15s
        //BigMath.calculateThirdRootsConcurrently(7, 4000);   // 30s
        //BigMath.calculateThirdRootsConcurrently(7, 5150);   // 60s
    }
}
