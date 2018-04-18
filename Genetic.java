import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Genetic {

    private AtomicLong temp = new AtomicLong();

    String date = new SimpleDateFormat("dd-MM-yyyy HHmmss").format(new Date());

    File file = new File("C:\\Users\\Hengyu\\Dropbox\\results\\", date + ".txt");

    FileWriter fw;


    private static final int NUM_FEATURES = StateSimulator2.NUM_FEATURES;

    static double bestPerf = 0;

    /* For parallel computing */
    private static final boolean RUN_CONCURRENT = true;
    private final ExecutorService es;
    private final int NUM_THREADS;
    private ArrayList<PlayGame> tasks;

    ArrayList<double[]> population = new ArrayList<double[]>();


    private Genetic() {
        try {
            fw = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (RUN_CONCURRENT) {
            NUM_THREADS = Runtime.getRuntime().availableProcessors();
            System.out.println("# processors available = " + NUM_THREADS);
            es = Executors.newFixedThreadPool(NUM_THREADS);
            tasks = new ArrayList<>(50);
        } else {
            NUM_THREADS = 1;
            es = Executors.newSingleThreadExecutor();
            tasks = new ArrayList<>(1);
        }
    }

    private void normalizeVector(double[] vector) {
        double sum = 0;
        for(double elem: vector) {
            sum += Math.pow(elem, 2);
        }

        sum = Math.sqrt(sum);

        for (int i = 0; i < vector.length; i++) {
            vector[i] /= sum;
        }
    }

    void generateInitWeights() {

        Random r = new Random();

        for (int i = 0; i < 500; i++) {     //initial population of 5000
            double[] array = new double[NUM_FEATURES + 1];

            for (int j = 0; j < NUM_FEATURES; j++) {
                array[j] = r.nextDouble() * 20 - 10;    //random values from -10 to 10
            }

            population.add(array);
        }

    }

    void geneticTrain(ArrayList<double[]> startingWeights) {

        Random r = new Random();

        while (bestPerf < 500000) {
            double[] parent1 = randomSelection(startingWeights);
            double[] parent2 = randomSelection(startingWeights);
            double[] child = reproduce(parent1, parent2);

            if (r.nextDouble() <= 0.1) {         //10% chance of mutating
                mutate(child);
            }

            try {
                getFitness(child);
            } catch (IOException e) {
                e.printStackTrace();
            }

            population.add(child);

            if (population.size() > 650) {
                cull(population);
            }
        }

    }

    void cull(ArrayList<double[]> population) {

        for (int i = 0; i < 150; i++) {

            double minScore = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < population.size(); j++) {
                double[] selected = population.get(j);

                if (selected[NUM_FEATURES] < minScore) {
                    minScore = selected[NUM_FEATURES];
                    minIndex = j;
                }

            }

            population.remove(minIndex);       //kill lowest scorers of the population :(
        }
    }

    void mutate(double[] child) {

        Random r = new Random();

        int i = r.nextInt(NUM_FEATURES - 1);

        child[i] += r.nextDouble() * 6 - 3;      //increase or decrease by random value, maximum of 2

    }


    void mutate2(double[] child) {

        Random r = new Random();

        int i = r.nextInt(NUM_FEATURES - 1);

        child[i] = r.nextDouble() * 20 - 10;      //weight mutates to random value

    }


    double[] reproduce2(double[] parent1, double[] parent2) {    //child gets weighted averaged values from parents
        double[] child = new double[NUM_FEATURES + 1];
        double weight1 = parent1[NUM_FEATURES] / (parent1[NUM_FEATURES] + parent2[NUM_FEATURES]);
        double weight2 = parent2[NUM_FEATURES] / (parent1[NUM_FEATURES] + parent2[NUM_FEATURES]);

        for (int i = 0; i < NUM_FEATURES; i++) {

            child[i] = parent1[i] * weight1 + parent2[i] * weight2;
        }

        return child;
    }

    double[] reproduce(double[] parent1, double[] parent2) {      //crossover like in textbook
        double[] child = new double[NUM_FEATURES + 1];

        Random r = new Random();
        int j = r.nextInt(NUM_FEATURES);

        for (int i = 0; i < NUM_FEATURES; i++) {

            if (i < j)
                child[i] = parent1[i];

            else
                child[i] = parent2[i];

        }

     /*   try {
            fw.write((child[0] + " " + child[1] + " " + child[2] + " " + child[3] + " " + child[4] + " " +
                    child[5]) + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return child;
    }


    double[] randomSelection(ArrayList<double[]> population) {

        Random r = new Random();
        double maxScore = 0;
        int maxIndex = 0;

        for (int i = 0; i < 0.1 * (population.size()); i++) {               //randomly select 10% members of the population

            int select = r.nextInt(population.size());
            double[] selected = population.get(select);

            if (selected[NUM_FEATURES] > maxScore) {
                maxScore = selected[NUM_FEATURES];      //highest average score guy gets chosen
                maxIndex = select;
            }

        }
/*        try {
            double[] selected = population.get(maxIndex);
            fw.write("Parent chosen: " + maxIndex + "\r\n");
           fw.write(selected[0] + " " + selected[1] + " " + selected[2] + " " + selected[3] + " "
                    + selected[4] + " " + selected[5] + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        System.out.println("Parent picked: " + maxIndex + ", " + "Score = " + maxScore);

        return population.get(maxIndex);

    }


    public void getFitness(double[] weights) throws IOException {


        PlayGame pg = new PlayGame(weights);

        IntStream.range(0, 50).parallel().forEach(i -> {
            try {
                temp.addAndGet(pg.call().longValue());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

/*        for (int i = 0; i < 50; i++) { //average 50 times of number of rows cleared
            try {
                temp += pg.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        weights[NUM_FEATURES] = temp.doubleValue() / 50;
//        System.out.println("Temp = " + temp + "\nScore = " + weights[NUM_FEATURES]);

        temp.set(0);


        try {
            fw.write("Weights are: ");
            for (int i = 0; i <= NUM_FEATURES; i++) {
                fw.write(weights[i] + " ");
            }
            fw.write("\r\n");
            fw.flush();
        } catch (IOException e) {
        }

        if (weights[NUM_FEATURES] > bestPerf)
            bestPerf = weights[NUM_FEATURES];
    }

    public static void main(String args[]) {
        Genetic g = new Genetic();
        g.run();

    }

    public void play(double[] weights, PlayGame pg) {

        try {
            weights[NUM_FEATURES] += pg.call();
            System.out.println(weights[NUM_FEATURES]);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {

        ZonedDateTime now = ZonedDateTime.now();

        generateInitWeights();

        for (int i = 0; i < population.size(); i++) {
            try {
                getFitness(population.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        geneticTrain(population);

        long seconds = now.until(ZonedDateTime.now(), ChronoUnit.SECONDS);
        System.out.println(seconds + " seconds.");
    }
}
