import java.util.Random;

public class Prova {
    public static void main(String[] args) {
        for (int i = 1000; i < 10000; i++) {
            int number = i;
            if (squareSum(number)) {
                System.out.println("\nNumber found: " + number);
                System.out.println("\nNumbers to generate: ");
                for (int j = 0; j < 100; j++) {
                    int m = generateNumber(splitNumber(number)[0], splitNumber(number)[1]);
                    System.out.print(" " + m + " ");
                }
            }
        }
    }

    public static int[] splitNumber(int n) {
        if (n < 1000 || n > 9999) {
            throw new IllegalArgumentException("Number must be between 1000 and 9999");
        }

        int first = n / 100;
        int second = n % 100;

        int[] result = { first, second };
        return result;
    }

    public static boolean squareSum(int n) {
        int[] result = splitNumber(n);
        int sum = result[0] + result[1];
        int m = sum * sum;
        if (m == n) {
            return true;
        }

        return false;
    }

    public static int generateNumber(int m, int n) {
        Random random = new Random();
        int number = 0;
        if (n < m) {
            number = random.nextInt(m - n + 1) + n;
        } else {
            number = random.nextInt(n - m + 1) + m;
        }
        return number;
    }
}
