package dl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;

import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.EvaluationAveraging;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;


public class App2_MLP_DL4J {
    private static int label_index = 9; 
    private static int n_classes = 3; 
    private static NormalizerMinMaxScaler pre_processor;
    private static String train_path = "data/train";
    private static String test_path = "data/test";

    public static void main(String[] args)throws IOException, InterruptedException {
        SparkSession spark = createSparkSession();

        Dataset<Row> training_data = readTrainingData(spark);
        doETL_AndSave(training_data);
        MultiLayerNetwork model = trainNetwork();
        evaluateNetwork(model);
    }

    private static SparkSession createSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .config("spark.sql.warehouse.dir", "spark")
                .appName("Esculapio")
                .getOrCreate();
    }

    private static Dataset<Row> readTrainingData(SparkSession spark) {
        Dataset<Row> training_data = spark.sqlContext()
                .read()
                .format("com.databricks.spark.csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .load("data/train.csv");
        training_data.show();
        System.out.println("teste 0");
        return training_data;
    }

    private static void doETL_AndSave(Dataset<Row> training_data) {
        Map <String, Object> mean_values = new HashMap<String, Object>();
        mean_values.put("gDeSalP/Sem", 24.4);
        //mean_values.put("Fare", 32.2);
        Dataset<Row> not_null_training_data = training_data.na().fill(mean_values);
        Dataset<Row> relevant_not_null_training_data = not_null_training_data.drop ("rp", "sexo", "planoSaude");
        
        relevant_not_null_training_data.show();
        System.out.println("teste 1");

        StringIndexer tabagismoIndexer = new StringIndexer()
                .setInputCol("tabagismo")
                .setOutputCol("tabagismoIndex")
                .setHandleInvalid("skip");

        StringIndexer dificuldadeAtvBasicaIndexer = new StringIndexer()
                .setInputCol("dificAtvFisica")
                .setOutputCol("dificAtvFisicaIndex")
                .setHandleInvalid("skip");

        StringIndexer doencaAutoreferidaIndexer = new StringIndexer()
                .setInputCol("doençasAutoref")
                .setOutputCol("doençasAutorefIndex")
                .setHandleInvalid("skip");

        StringIndexer historicoDoencaCardiacaIndexer = new StringIndexer()
                .setInputCol("historico")
                .setInputCol("historicoIndex")
                .setHandleInvalid("skip");


        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{tabagismoIndexer, dificuldadeAtvBasicaIndexer, doencaAutoreferidaIndexer, historicoDoencaCardiacaIndexer});

        Dataset<Row> relevant_not_null_only_numeric_training_data
                = pipeline
                .fit(relevant_not_null_training_data)
                .transform(relevant_not_null_training_data)
                .drop("tabagismo", "dificAtvFisica", "doençasAutoref", "historico");
        relevant_not_null_only_numeric_training_data.show();
        System.out.println("teste 2");

        Dataset<Row> relevant_only_numeric_training_data
                = relevant_not_null_only_numeric_training_data
                .select("idade", "tabagismoIndex", "bebidasP/Sem", "gDeSalP/Sem", "pressaoArterial", "imc", "dificAtvFisicaIndex",
                "doençasAutorefIndex", "historicoIndex", "risco");
        relevant_only_numeric_training_data.show();
        System.out.println("Teste 3");

        Dataset<Row>[] splits
                = relevant_only_numeric_training_data.randomSplit
                (new double[]{0.7, 0.3});
        Dataset<Row> reduced_training_data = splits[0];
        Dataset<Row> test_data = splits[1];

        System.out.println("Teste 4");

        reduced_training_data
                .coalesce(1)// coalesce(1) writes DF in a single CSV
                .write()
                .format("com.databricks.spark.csv")
                .option("header", "false")
                .option("delimiter", ",")
                .save("data/train");

        System.out.println("Teste 5");

        test_data
                .coalesce(1)// coalesce(1) writes DF in a single CSV
                .write()
                .format("com.databricks.spark.csv")
                .option("header", "false")
                .option("delimiter", ",")
                .save("data/test");
    }

    private static MultiLayerNetwork trainNetwork() throws IOException, InterruptedException {
        int n_epochs = 1000; // 10000
        int seed = 123;
        int n_inputs = label_index;
        int n_outputs = n_classes;

        DenseLayer input_layer = new DenseLayer.Builder()
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .nIn(n_inputs).nOut(16)
                .build();

        DenseLayer hidden_layer_1 = new DenseLayer.Builder()
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .nIn(16).nOut(32)
                .build();

        DenseLayer hidden_layer_2 = new DenseLayer.Builder()
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .nIn(32).nOut(16)
                .build();

        OutputLayer output_layer = new OutputLayer.Builder(LossFunction.XENT)
// XENT for Binary Classification
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.SOFTMAX)
                .nIn(16).nOut(n_outputs)
                .build();

        MultiLayerConfiguration MLPconfiguration
                = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01)) // 0.0001
                .list()
                .layer(0, input_layer)
                .layer(1, hidden_layer_1)
                .layer(2, hidden_layer_2)
                .layer(3, output_layer)
                .pretrain(false).backprop(true).build();

        int batch_size_training = 128;
        DataSetIterator cleared_training_data = readCSVDataset
                (train_path, batch_size_training, label_index, n_classes);
        pre_processor = new NormalizerMinMaxScaler();
        pre_processor.fit(cleared_training_data);
        cleared_training_data.setPreProcessor(pre_processor);

        MultiLayerNetwork model
                = new MultiLayerNetwork(MLPconfiguration);
        model.init();
        System.out.println("Train model....");
        for (int i = 0; i < n_epochs; i++) {
            model.fit(cleared_training_data);
        }
        return model;
    }

    private static void evaluateNetwork(MultiLayerNetwork model)
            throws IOException, InterruptedException {
        int batch_size_test = 128;
        DataSetIterator cleared_test_data = readCSVDataset
                (test_path, batch_size_test, label_index, n_classes);
        cleared_test_data.setPreProcessor(pre_processor);
        Evaluation eval = new Evaluation(2); // for class 1
        while (cleared_test_data.hasNext()) {
            DataSet next = cleared_test_data.next();
            INDArray output = model.output(next.getFeatureMatrix());
            eval.eval(next.getLabels(), output);
        }

        System.out.println(eval.stats());
        System.out.println(" – Example finished –");
        EvaluationAveraging averaging = EvaluationAveraging.Macro;
        double MCC = eval.matthewsCorrelation(averaging);
        System.out.println("Matthews correlation coefficient: " + MCC);
    }

    private static DataSetIterator readCSVDataset
            (String csvFileClasspath, int batchSize, int label_index,
             int n_classes) throws IOException, InterruptedException {
        RecordReader record_reader = new CSVRecordReader();
        File input = new File(csvFileClasspath);
        if (input.isDirectory()) {
            File[] csvs = input.listFiles(pathname -> {
                String str = pathname.toString();
                return str.endsWith(".csv");
            });

            if (csvs != null && csvs.length > 0) {
                File file = csvs[0];
                File new_file = null;
                if (csvFileClasspath.endsWith("train")) {
                    new_file = new File("data/new_train.csv");
                } else {
                    new_file = new File("data/new_test.csv");
                }
                file.renameTo(new_file);
                record_reader.initialize(new FileSplit(new_file));
            }
        } else {
            record_reader.initialize(new FileSplit(input));
        }

        DataSetIterator iterator
                = new RecordReaderDataSetIterator
                (record_reader, batchSize, label_index, n_classes);
        return iterator;
    }
}