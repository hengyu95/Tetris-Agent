import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * A class to test the weights given by playing the game multiple times and retrieve the average score.
 * Runs the games in parallel to reduce running time.
 */
public class TestWeights {
    private static int NUM_THREADS;
    private static ExecutorService es;
    private static ArrayList<Callable<Double>> tasks;
    private static int NUM_GAMES_TO_PLAY = 50;
    public static String WEIGHTS_RESULT_FILE = "TestWeights.out";

    // set the weights to test here!
    private static double[] weights = { -0.32488874 , -0.54299503 , -0.01578393 , -0.58728519 , -0.16391705 , -0.39038268 , -0.04095485 , -0.27111979 };

    private static void writeToFile(double average) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(System.currentTimeMillis() + WEIGHTS_RESULT_FILE));
        bw.write(average + "\n");

        for (double weight : weights) {
            bw.write(Double.toString(weight) + " ");
        }


        bw.flush();
        bw.close();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        NUM_THREADS = Runtime.getRuntime().availableProcessors();
        System.out.println("# processors available = " + NUM_THREADS);
        es = Executors.newFixedThreadPool(NUM_THREADS);

        PlayGame.NUM_GAMES_TO_AVERAGE = 1; // only run one game per thread since we are running each game concurrently
        PlayGame.setLevel(1); // usual difficulty

        tasks = new ArrayList<>(NUM_GAMES_TO_PLAY);

        for (int i = 0; i < NUM_GAMES_TO_PLAY; i++)
            tasks.add(new PlayGame(weights));

        double average = 0;
        List<Future<Double>> results = es.invokeAll(tasks);
        for (Future<Double> result : results) {
            average += result.get();
        }

        Debug.printRed("Average score = " + average / NUM_GAMES_TO_PLAY + " over " + NUM_GAMES_TO_PLAY + " games");
        writeToFile(average);
        System.exit(0);
    }
}
