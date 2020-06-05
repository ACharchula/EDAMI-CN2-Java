package pl.antc;

import pl.antc.algorithm.AlgorithmRunner;
import pl.antc.csv.PrepareData;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        String choice;
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("=== CN2 MENU ===");
            System.out.println("1. Recreate test and training data");
            System.out.println("2. Run algorithm for adult data");
            System.out.println("3. Run algorithm for car data");
            System.out.println("4. Run algorithm for nursery data");
            System.out.println("Any other input to close a program");
            choice = scanner.nextLine();
            if (choice.equals("1")) {
                PrepareData.prepareAdultData();
                PrepareData.prepareCarData();
                PrepareData.prepareNurseryData();
                System.out.println("Done!");
            } else if (choice.equals("2")) {
                AlgorithmRunner.runCN2("data/adult/training.data", "data/adult/test.data", getStarMaxSize(), getMinSignificance());
            } else if (choice.equals("3")) {
                AlgorithmRunner.runCN2("data/cars/training.data", "data/cars/test.data", getStarMaxSize(), getMinSignificance());
            } else if (choice.equals("4")) {
                AlgorithmRunner.runCN2("data/nursery/training.data", "data/nursery/test.data", getStarMaxSize(), getMinSignificance());
            } else {
                break;
            }
        } while(true);
    }

    private static int getStarMaxSize() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert max size of star [int]: ");
        return scanner.nextInt();
    }

    private static double getMinSignificance() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert minimum significance [double (comma separated)]: ");
        return scanner.nextDouble();
    }
}
