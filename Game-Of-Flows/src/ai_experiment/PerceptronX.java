package ai_experiment;

import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Sam
 */
public class PerceptronX {

    public static double dot_product(double[] value, double weights[]) {
        double total = 0.0;
        for (int i = 0; i < value.length; i++) {
            total += value[i] * weights[i];
        }
        return total;
    }

    public static void main(String[] args) {
        double threshold = 0.5;
        double learning_rate = 0.01;
        double[] weights = {0,0,0,0,0};
        double[][][] training_set = {
            {{1, 0, 0, 0, 1}, {0}}, 
            {{1, 1, 1, 1, 1}, {1}}, 
            {{0, 1, 0, 0, 0}, {0}},
            {{1, 0, 1, 0, 1}, {0}}, 
            {{0, 1, 1, 1, 0}, {0}}, 
            {{1, 0, 1, 1, 0}, {0}}, 
            {{1, 1, 0, 1, 0}, {0}},
            {{1, 1, 1, 0, 0}, {0}}
        };
        
        double[][][] test_set = {
            {{1, 1, 0, 1, 0}, {0}}, 
            {{1, 1, 1, 0, 0}, {0}},
            {{1, 1, 1, 1, 1}, {1}}
        };
        
        while (true) {
            for (int i = 0; i < 60; i++) {
                System.out.print('-');
            }
            System.out.println();
            double error_count = 0;
            for (double[][] training_data : training_set) {
                System.out.println(Arrays.toString(weights));
                int result = dot_product(training_data[0], weights) > threshold ? 1 : 0;
                double error = training_data[1][0] - result;
                if (error != 0) {
                    error_count += 1;
                    for (int index = 0; index < training_data[0].length; index++) {
                        weights[index] += learning_rate * error * training_data[0][index];
                    }
                }
            }
            System.out.println("Error count: " + error_count);
            if (error_count == 0) {
                break;
            }
        }
        System.out.println(Arrays.toString(weights));
        for(double[][] test_data : test_set){
                int result = dot_product(test_data[0], weights) > threshold ? 1 : 0;
                boolean worked = (test_data[1][0] - result) == 0;
                System.out.println(Arrays.toString(test_data[0]) + " - " + (worked ? "Success" : "Failure"));
        }
    }
}
