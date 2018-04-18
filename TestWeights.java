import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A class to test the weights given by playing the game multiple times and retrieve the average score.
 * Runs the games in parallel to reduce running time.
 */
public class TestWeights {
    private static int NUM_THREADS;
    private static ExecutorService es;
    private static ArrayList<Callable<Double>> tasks;
    private static int NUM_GAMES_TO_PLAY = 50;
    static File file = new File("C:\\Users\\Hengyu\\Dropbox\\results\\testWeights\\", "results.txt");
    static FileWriter fw;


    // set the weights to test here!
    private static double[] weights = {-12.32740332, -6.485847315, -0.150227959, -7.55674893, -1.835161079, -6.166983091, -1.109461104, -4.509427816};


    public static void main(String[] args) throws InterruptedException, ExecutionException {
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


        try {
            fw = new FileWriter(file, true);
            fw.write(weights + "\r\n");
            fw.write("Average score = " + average / NUM_GAMES_TO_PLAY + " over " + NUM_GAMES_TO_PLAY + " games\r\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
