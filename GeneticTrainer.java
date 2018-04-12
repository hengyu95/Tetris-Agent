import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;

public class GeneticTrainer extends Trainer {

    File file = new File("results.txt");

    FileWriter fw;

    static int index = 0;

    static double bestPerf = 0;

    static double totalScore = 0;

    ArrayList<double[]> population = new ArrayList<double[]>();


    public GeneticTrainer() {
        try {
            fw = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generateInitWeights() {

        Random r = new Random();

        for (int i = 0; i < 300; i++) {     //initial population of 300
            double[] array = new double[6];

            for (int j = 0; j < 4; j++) {
                array[j] = r.nextDouble() * 20 - 10;      //random values from -10 to 10
            }

            array[4] = -100000000;  //if move leads to lost it's awful don't do it

            population.add(array);
        }

    }

    void geneticTrain(ArrayList<double[]> startingWeights) {

        Random r = new Random();

        while (mostRowsCleared < 10000) {
            double[] parent1 = randomSelection(startingWeights);
            double[] parent2 = randomSelection(startingWeights);
            double[] child = reproduce2(parent1, parent2);

            if (r.nextDouble() <= 0.05) {         //5% chance of mutating
                mutate(child);
            }

            try {
                getFitness(child);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(child[0] + " " + child[1] + " " + child[2] + " " + child[3] + " " + child[4] + " " + child[5]);
            System.out.println(bestPerf + " " + totalScore);
            population.add(child);

            if (population.size() > 400) {
                cull(population);
            }
        }


        System.out.println("Best configuration was " + bestConfiguration + " which cleared " + mostRowsCleared + ".");


    }

    void cull(ArrayList<double[]> population) {

        for (int i = 0; i < 100; i++) {    //probably there's a much more efficient way to do this

            double minScore = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < population.size(); j++) {
                double[] selected = population.get(j);

                if (selected[5] < minScore) {
                    minScore = selected[5];
                    minIndex = j;
                }

            }

            population.remove(minIndex);       //kill lowest scorers of the population :(
        }


    }

    void mutate(double[] child) {

        Random r = new Random();

        int i = r.nextInt(4);

        child[i] += r.nextDouble() * 0.4 - 0.2;      //increase or decrease by random value, maximum of 0.2

    }


    double[] reproduce2(double[] parent1, double[] parent2) {    //child gets weighted averaged values from parents
        double[] child = new double[6];
        double weight1 = parent1[5] / (parent1[5] + parent2[5]);
        double weight2 = parent2[5] / (parent1[5] + parent2[5]);

        for (int i = 0; i < 5; i++) {

            child[i] = parent1[i] * weight1 + parent2[i] * weight2;
        }

        return child;
    }

    double[] reproduce(double[] parent1, double[] parent2) {      //crossover like in AIMA textbook
        double[] child = new double[6];

        Random r = new Random();
        int j = r.nextInt(5);

        for (int i = 0; i < 5; i++) {

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

            if (selected[5] > maxScore) {
                maxScore = selected[5];      //highest average score guy gets chosen
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

        return population.get(maxIndex);

    }


    public void getFitness(double[] weights) throws IOException {

        int temp = 0;
        Heuristics.setWeights(weights[0], weights[1], weights[2], weights[3]);

        for (int i = 0; i < 50; i++) { //average 20 times of number of rows cleared
            temp += simulateConfiguration(weights[0], weights[1], weights[2], weights[3]);
        }

        weights[5] = temp / 50.0;

        totalScore += weights[5];

        try {

            fw.write(weights[5] + " " + index + " " + mostRowsCleared + "\r\n"); //write results to results.txt
            fw.write("Weights are: " + weights[0] + " " + weights[1] + " " + weights[2] + " " + weights[3] + " "
                    + weights[4] + "\r\n");
            index++;
            fw.flush();
        } catch (IOException e) {
        }

        if (weights[5] > bestPerf)
            bestPerf = weights[5];
    }

    public static void main(String args[]) {
        GeneticTrainer gt = new GeneticTrainer();
        gt.run();

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
