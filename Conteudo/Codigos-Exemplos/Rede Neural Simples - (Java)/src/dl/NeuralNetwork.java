package dl;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

public class NeuralNetwork {

    private final int nodes_nLi, nodes_nLh, nodes_nLo; // L : Layer
    private final double learning_rate;
    private RealMatrix Wih, Who; // W : Weights between two Layers

    public NeuralNetwork (int nodes_nLi, int nodes_nLh, int nodes_nLo, double learning_rate) {
        this.nodes_nLi = nodes_nLi;
        this.nodes_nLh = nodes_nLh;
        this.nodes_nLo = nodes_nLo;
        this.learning_rate = learning_rate;
        Wih = generate_randomW(nodes_nLh, nodes_nLi);
        Who = generate_randomW(nodes_nLo, nodes_nLh);
    }

    private RealMatrix generate_randomW(int rows, int columns) {
        RealMatrix randomW = MatrixUtils.createRealMatrix(rows, columns);
        for(int row = 0; row < rows; row++)
            for(int column = 0; column < columns; column++)
                randomW.setEntry(row, column, ThreadLocalRandom.current().nextDouble(-0.5,0.5));

        return randomW;
    }

    public void trainNeuralNetwork(List input_data, List target_data) {
        RealMatrix outputLi = listToVector(input_data).transpose();
        RealMatrix outputLh = applyNeuralLayer(outputLi, Wih);
        RealMatrix outputLo = applyNeuralLayer(outputLh, Who);
        RealMatrix targetLo = listToVector(target_data).transpose();
        RealMatrix errorLo = targetLo.subtract(outputLo);
        Who = updateW(Who, outputLh, outputLo, errorLo);
        RealMatrix errorLh = Who.transpose().multiply(errorLo);
        Wih = updateW(Wih, outputLi, outputLh, errorLh);
    }

    private RealMatrix listToVector(List list){
        int list_size = list.size();
        RealMatrix vector = MatrixUtils.createRealMatrix(1, list_size);

        for(int column = 0; column < list_size; column++)
            vector.setEntry(0, column, (double)list.get(column));

        return vector;
    }

    public RealMatrix applyNeuralNetwork(List input_data){
        RealMatrix outputLi = listToVector(input_data).transpose();
        RealMatrix outputLh = applyNeuralLayer(outputLi, Wih);
        RealMatrix outputLo = applyNeuralLayer(outputLh, Who);
        return outputLo;
    }

    private RealMatrix applyNeuralLayer (RealMatrix outputLL, RealMatrix W) {
        return activationFunction(W.multiply(outputLL)); // LL : left Layer
    }

    private RealMatrix activationFunction(RealMatrix inputRL) {
        int rows = inputRL.getRowDimension();
        int columns = inputRL.getColumnDimension();
        RealMatrix outputRL = MatrixUtils.createRealMatrix(rows, columns);

        for(int row = 0; row < rows; row++)
            for(int column = 0; column < columns; column++){
                double element_inputRL = inputRL.getEntry(row,column);
                double element_outputRL = 1.0 / (1.0 + Math.exp(-element_inputRL)); // sigmoid
                outputRL.setEntry(row, column, element_outputRL);
            }

        return outputRL;
    }

    private RealMatrix updateW(RealMatrix W, RealMatrix outputLL, /* LL : left Layer*/ RealMatrix outputRL, RealMatrix errorRL) { // RL : wright Layer
        int delta_rows = errorRL.getRowDimension();
        int delta_columns = errorRL.getColumnDimension();
        RealMatrix deltaW = MatrixUtils.createRealMatrix(delta_rows, delta_columns);

        for(int row = 0; row < delta_rows; row++)
            for(int column = 0; column < delta_columns; column++){
                double element_errorRL = errorRL.getEntry(row,column);
                double element_outputRL = outputRL.getEntry(row,column);
                deltaW.setEntry(row, column, element_errorRL * element_outputRL * (1.0 - element_outputRL));
            }

        deltaW = deltaW.multiply(outputLL.transpose()).scalarMultiply(learning_rate);
        return W.add(deltaW);
    }
}
