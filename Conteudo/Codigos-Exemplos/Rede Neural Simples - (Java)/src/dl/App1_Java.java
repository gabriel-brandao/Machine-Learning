package dl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.math3.linear.RealMatrix;ning_rate = 0.2; //taxa de aprendizado
    static int epochs_total = 1; //quantidades de vezes para treinar a rede

    private static ArrayList<ArrayList<Double>> manual_numbers = new ArrayList();
    private static ArrayList<ArrayList<Double>> target_numbers = new ArrayList();
    private static ArrayList<Double> manual_one_number = new ArrayList();
    private static ArrayList<Double> target_one_number = new ArrayList();

    private static NeuralNetwork neural_network;

    public static void main(String args[]) {
        neural_network = new NeuralNetwork (NUMBER_PIXELS, nodes_nLh, DIGITS_TOTAL, learning_rate); //cria a rede neural
        trainNeuralNetWork(); //treina a rede neural
        testNeuralNetWork(); //testa a rede neural
        evaluateNeuralNetWork(); //avalia a rede neural
    }

    private static void trainNeuralNetWork() {
        String training_file_name = "data/mnist_train.csv"; //no diretorio data arquivo mnist
        File training_file = new File(training_file_name); //cria um arquivo cm o nome acima

        for (int epoch = 0; epoch < epochs_total; epoch++) {
            try {
                Scanner inputStream = new Scanner(training_file); //lê arquivo, se não der certo chama uma exceção
                String[][] number_pixels = new String[1][NUMBER_PIXELS + 1]; //cria um vetor de 785 elementos

                while (inputStream.hasNext()) { //retorna true se há mais dados de entrada no arquivo

                    for (int example = 0; example < TRAINING_EXAMPLES_TOTAL; example++) { //para cada linha de treino:

                        for (int digit = 0; digit < DIGITS_TOTAL; digit++)
                            target_one_number.add(MIN_VALUE);

                        number_pixels[0] = inputStream.next().split(",");
                        target_one_number.set(Integer.parseInt(number_pixels[0][0]), MAX_VALUE);

                        for (int pixel = 1; pixel < (NUMBER_PIXELS + 1); pixel++)
                            manual_one_number.add(((Double.parseDouble (number_pixels[0][pixel])) / MAX_PIXEL * MAX_VALUE) + MIN_VALUE);

                        neural_network.trainNeuralNetwork (manual_one_number, target_one_number);
                        manual_one_number = new ArrayList();
                        target_one_number = new ArrayList();
                    }
                }
                inputStream.close(); //fecha arquivo
            } catch (FileNotFoundException exception) { //dispar exceção caso não consiga abrir o arquivo
                exception.printStackTrace();
            }
        }
    }

    private static void testNeuralNetWork() {
        String test_file_name = "data/mnist_test.csv";
        File test_file = new File(test_file_name);

        try {
            Scanner inputStream = new Scanner(test_file);
            String[][] one_character = new String[1][NUMBER_PIXELS + 1];

            while (inputStream.hasNext()) {
                for (int example = 0; example < TEST_EXAMPLES_TOTAL; example++) {
                    for (int digit = 0; digit < DIGITS_TOTAL; digit++)
                        target_one_number.add(MIN_VALUE);

                    one_character[0] = inputStream.next().split(",");
                    target_one_number.set(Integer.parseInt(one_character[0][0]), MAX_VALUE);
                    target_numbers.add(target_one_number);

                    for (int pixel = 1; pixel < (NUMBER_PIXELS + 1); pixel++)
                        manual_one_number.add(((Double.parseDouble(one_character[0][pixel])) / (MAX_PIXEL * MAX_VALUE)) + MIN_VALUE);

                    manual_numbers.add(manual_one_number);
                    manual_one_number = new ArrayList();
                    target_one_number = new ArrayList();
                }
            }
            inputStream.close();
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    private static void evaluateNeuralNetWork() {
        int[] score_board = new int[TEST_EXAMPLES_TOTAL];

        for(int example = 0; example < TEST_EXAMPLES_TOTAL; example++) {
            RealMatrix result = neural_network.applyNeuralNetwork (manual_numbers.get(example));
            int targets_max_number = maxArrayValue(target_numbers.get(example));
            int result_max_number = maxValue(result);
            if (targets_max_number == result_max_number)
                score_board[example] = 1;
            else
                score_board[example] = 0;
        }

        double score_board_values_sum = 0;
        for(int example = 0; example < TEST_EXAMPLES_TOTAL; example++)
            score_board_values_sum += score_board[example];

        System.out.println("Performance: " + score_board_values_sum/TEST_EXAMPLES_TOTAL);
    }

    private static int maxValue(RealMatrix matrix){
        int max_value = 0;
        int rows_n = matrix.getRowDimension();
        for(int row_n = 0; row_n < rows_n; row_n++)
            if(matrix.getEntry(row_n,0) > matrix.getEntry(max_value, 0))
                max_value = row_n;

        return max_value;
    }

    private static int maxArrayValue(ArrayList<Double> vector){
        int max_value = 0;
        int vector_size = vector.size();
        for (int element_n = 0; element_n < vector_size; element_n++)
            if (vector.get(element_n) > vector.get(max_value))
                max_value = element_n;

        return max_value;
    }
}