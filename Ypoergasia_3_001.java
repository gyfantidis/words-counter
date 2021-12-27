package ypoergasia_3_001;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Ypoergasia_3_001 {

    private static final int THREAD_COUNT = 8;
    private static final int CALLS = 100;
    private static final String API_URL = "http://loripsum.net/api/10/plaintext";

    //main
    public static void main(String[] args) {
        
        long startTime = System.nanoTime(); 

        //πίνακας από τα νήματα
        ProcessThread[] threads = new ProcessThread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ProcessThread();
            threads[i].start();
        }

        for (ProcessThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //δήλωση μεταβλητών 
        final HashMap<Character, Double> charCount = new HashMap<>();
        double lengthOfWords = 0;
        double lengthOfChars = 0;

        //μορφή του αριθμού
        DecimalFormat df = new DecimalFormat("#.##");

        // ένωση των νημάτων
        for (ProcessThread thread : threads) {

            //ένωση αριθμών λέξεων και γραμμάτων του κάθε νήματος
            lengthOfWords = lengthOfWords + thread.getLengthOfWords();
            lengthOfChars = lengthOfChars + thread.getLengthOfChars();

            //ένωση γράμματος και αριθμού επανάληψής του
            thread.getCharCount().forEach((letter, count) -> {
                double countSum = count;
                if (charCount.containsKey(letter)) {
                    countSum += charCount.get(letter);
                }
                charCount.put(letter, countSum);
            });
        }

        //υπολογισμός μέσου όρου
        double averageOfWordChars = lengthOfChars / lengthOfWords;

        //υπολογισμός ποσοστού εμφάνησης του κάθε γράμματος 
        double sumOfRates = 0;
        for (Map.Entry<Character, Double> entry : charCount.entrySet()) {
            double rate = 0;
            rate = (entry.getValue() / lengthOfChars) * 100;
            sumOfRates = sumOfRates + rate;
            charCount.put(entry.getKey(), rate);

        }
        //εκτύπωση αποτελεσμάτων 
        System.out.println("\n---------- RESULTS -----------");
        System.out.println("Total length of Chars : " + lengthOfChars + " chars.");
        System.out.println("Total length of Words : " + lengthOfWords + " words.");
        System.out.println("The texts have : " + df.format(averageOfWordChars) + " chars per word.");

        //εκτύπωση ποσοστών εμφάνησης του κάθε γράμματος
        System.out.println("\n---------- CHARS AVERAGE -----------");

        charCount.forEach((letter, count) -> {
            System.out.println(letter + ": " + df.format(count) + "%");
        });

        //εκτύπωση αθροίσματος ποσοστών, θα πρέπει να είναι 100
        System.out.println("Total sum of rates : " + df.format(sumOfRates) + "%");
        
         double totalTime = System.nanoTime() - startTime;
        System.out.println("\nFor " + threads.length + " threads :");
        System.out.println("Total time: " + totalTime / 1_000_000_000 + " seconds");

    }

    // το νήμα
    static class ProcessThread extends Thread {

        private final HashMap< Character, Double> charCount = new HashMap<Character, Double>();
        private double lengthOfWords = 0;
        private double lengthOfChars = 0;

        @Override
        public void run() {
            for (int i = 0; i < CALLS; i++) {
                String data = loadDataFromUrl();//βάλε σε ένα String τα δεδομένα που κατέβασες
                String[] words = data.split(" ");// χώρισε τις λέξεις
                data = data.replaceAll("[^a-zA-Z]+", ""); //κράτα μόνο τα γράμματα
                data = data.toLowerCase();// κάνε όλα τα γράμματα μικρά
                char[] letters = data.toCharArray();// μετέτρεψε το String data σε πίνακα από Characters

                lengthOfWords = words.length + lengthOfWords; //πρόσθεσε τις λέξεις του κάθε call
                lengthOfChars = data.length() + lengthOfChars; //πρόσθεσε τους χαρακτήρες του κάθε call

                //πέρασε στο HashMap τα γράμματα και το πόσες φορές εμφανίστηκαν (αφού το μετρήσεις) 
                for (int j = 0; j < letters.length; j++) {
                    double count = 1;
                    if (charCount.containsKey(letters[j])) {
                        count += charCount.get(letters[j]);
                    }
                    charCount.put(letters[j], count);
                }
            }
        }

        // μέθοδος διαβάσματος δεδομένων από ένα url
        private String loadDataFromUrl() {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(API_URL);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                    result.append(" ");
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
        //getters

        public HashMap<Character, Double> getCharCount() {
            return charCount;
        }

        public double getLengthOfWords() {
            return lengthOfWords;
        }

        public double getLengthOfChars() {
            return lengthOfChars;
        }
    }
}
