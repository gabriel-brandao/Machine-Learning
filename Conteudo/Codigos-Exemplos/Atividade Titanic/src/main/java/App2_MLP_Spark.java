import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.mllib.feature.StandardScalerModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import scala.Option;
import scala.Some;
import scala.Tuple2;
import scala.Tuple3;
import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;

public class App2_MLP_Spark {
    private static SparkSession spark;
    private static MultivariateStatisticalSummary summary = null;
    private static double mean_fare_column;  //media coluna taxa
    private static double mean_age_column; //media coluna idade
    private static StandardScalerModel scaler;
    private static Dataset<Row> trainingData; //dados e teste e validação
    private static Dataset<Row> validationData;

    public static void main(String[] args) {
        spark = createSparkSession();
        spark.sparkContext().setLogLevel("ERROR");          // para reduzir mensagens de INFO

        Dataset<Row> trainingDataSet = doETL_TrainingData();
        Dataset<Row> scaled_trainingDataset = scaleTrainingData(trainingDataSet);
        
        MultilayerPerceptronClassificationModel model = trainNetwork(scaled_trainingDataset);
        evaluateNetwork(model);

        Dataset<Row> resultDF = testNetwork(model);

        resultDF.write().format("com.databricks.spark.csv").option("header", true).save("/result/result.csv");
    }

    private static SparkSession createSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .config("spark.sql.warehouse.dir", "spark")
                .appName("App2_MLP_Spark")
                .getOrCreate();
    }

    private static Dataset<Row> readTrainingData() {
        Dataset<Row> input_data = spark.sqlContext()
                .read()
                .format("com.databricks.spark.csv")
                .option("header", "true")
                .option("inferSchema", "true")          //numero é lido como tipo numerico
                .load("data/train.csv");
        input_data.show();
        return input_data;
    }

    private static UDF1<String, Option<Integer>> normEmbarked = (String d) -> { //funcao definida pelo usuario1
        if (d == null) 
            return Option.apply(null);
         else 
          if (d.equals("S")) 
            return Some.apply(0);
           else 
             if (d.equals("C")) 
                return Some.apply(1);
              else
                 return Some.apply(2);
    };

    private static UDF1<String, Option<Integer>> normSex = (String d) -> {  //funcao definida pelo usuario1
        if (d == null) 
           return Option.apply(null);
         else 
           if (d.equals("male")) 
               return Some.apply(0);
             else 
                 return Some.apply(1);
    };

    private static Dataset<Row> doETL_TrainingData() {
        Dataset<Row> input_data = readTrainingData();
        spark.sqlContext().udf().register("normSex", normSex, DataTypes.IntegerType);
        spark.sqlContext().udf().register("normEmbarked", normEmbarked, DataTypes.IntegerType);
        
        Dataset<Row> numeric_relevant_columns = input_data.select(
                col("Survived"),
                col("Fare"),
                callUDF("normSex", col("Sex")).alias("Sex"), //normaliza string
                col("Age"),
                col("Pclass"),
                col("Parch"),
                col("SibSp"),
                callUDF("normEmbarked", col("Embarked")).alias("Embarked")); //normaliza char

        numeric_relevant_columns.show();

        // convert DataSet into JavaRDD to calculate mean values of columns
        JavaRDD<Vector> data_statistic
                = numeric_relevant_columns.rdd().toJavaRDD().map(row ->
                Vectors.dense(row.<Double>getAs("Fare"), row.isNullAt(3) ? 0d : row.<Double>getAs("Age")));

        summary = Statistics.colStats(data_statistic.rdd());

        mean_fare_column = summary.mean().apply(0);
        mean_age_column = summary.mean().apply(1);

        UDF1<String, Option<Double>> normFare = (String d) -> { //substitui nulo pela media, string para double
            if (d == null) 
              return Some.apply(mean_fare_column);
             else 
                return Some.apply(Double.parseDouble(d));
        };

        UDF1<String, Option<Double>> normAge = (String d) -> {  //substitui nulo pela media, string para double
            if (d == null)
               return Some.apply(mean_age_column);
              else 
                return Some.apply(Double.parseDouble(d));
        };

        spark.sqlContext().udf().register("normFare", normFare, DataTypes.DoubleType); //insere novas colunas
        spark.sqlContext().udf().register("normAge", normAge, DataTypes.DoubleType);
        
        Dataset<Row> without_nulls_numeric_relevant_columns
                = numeric_relevant_columns.select(
                col("Survived"),
                callUDF("normFare", col("Fare").cast("string")).alias("Fare"), //pseudonimo
                col("Sex"),
                callUDF("normAge", col("Age").cast("string")).alias("Age"),
                col("Pclass"),
                col("Parch"),
                col("SibSp"),
                col("Embarked"));
        without_nulls_numeric_relevant_columns.show();
        return without_nulls_numeric_relevant_columns;
    }

    private static Tuple3 <Double, Double, Double> flattenPclass (double value) { //padroniza classe economica
        Tuple3 <Double, Double, Double> result;
        if (value == 1) 
          result = new Tuple3<>(1d, 0d, 0d);
        else 
          if (value == 2) 
            result = new Tuple3<>(0d, 1d, 0d);
          else 
             result = new Tuple3<>(0d, 0d, 1d);

        return result;
    }

    private static Tuple3 <Double, Double, Double> flattenEmbarked (double value) { //padroniza porta de embarque
        Tuple3 <Double, Double, Double> result;
        if (value == 0) 
          result = new Tuple3<>(1d, 0d, 0d);
         else 
            if (value == 1) 
             result = new Tuple3<>(0d, 1d, 0d);
            else 
               result = new Tuple3<>(0d, 0d, 1d);
        
               return result;
    }

    private static Tuple2<Double, Double> flattenSex(double value) {            //padroniza sexo
        Tuple2<Double, Double> result;
        if (value == 0) 
           result = new Tuple2<>(1d, 0d);
        else 
          result = new Tuple2<>(0d, 1d);

        return result;
    }

    public static Vector getScaledVector(double fare, double age, double pclass, double sex, double embarked, StandardScalerModel scaler) {
        Vector scaledContinous = scaler.transform(Vectors.dense(fare, age));
        
        Tuple3<Double, Double, Double> pclassFlat = flattenPclass(pclass);
        Tuple3<Double, Double, Double> embarkedFlat = flattenEmbarked(embarked);
        Tuple2<Double, Double> sexFlat = flattenSex(sex);

        return Vectors.dense(scaledContinous.apply(0),
                scaledContinous.apply(1),
                sexFlat._1(), sexFlat._2(),
                pclassFlat._1(), pclassFlat._2(), pclassFlat._3(),
                embarkedFlat._1(), embarkedFlat._2(), embarkedFlat._3());
    }

    private static Dataset<Row> scaleTrainingData (Dataset<Row> trainingDataSet) {
        Vector standard_deviation = Vectors.dense(Math.sqrt(summary.variance().apply(0)), Math.sqrt(summary.variance().apply(1)));
       
        Vector mean = Vectors.dense(summary.mean().apply(0),summary.mean().apply(1));

        scaler = new StandardScalerModel(standard_deviation, mean);
        Encoder<Integer> integerEncoder = Encoders.INT();
        Encoder<Double> doubleEncoder = Encoders.DOUBLE();
        Encoders.BINARY();
        Encoder<Vector> vectorEncoder = Encoders.kryo(Vector.class);
        Encoders.tuple(integerEncoder, vectorEncoder);
        Encoders.tuple(doubleEncoder, vectorEncoder);

        JavaRDD<VectorPair> scaledRDD = trainingDataSet.toJavaRDD().map(row -> {
            VectorPair vectorPair = new VectorPair();
            vectorPair.setLabel(new Double(row.<Integer>getAs("Survived")));
            vectorPair.setFeatures(getScaledVector(
                    row.<Double>getAs("Fare"),
                    row.<Double>getAs("Age"),
                    row.<Integer>getAs("Pclass"),
                    row.<Integer>getAs("Sex"),
                    row.isNullAt(7) ? 0d : row.<Integer>getAs("Embarked"), scaler));
            return vectorPair;
        });

        Dataset<Row> scaled_trainingDataset = spark.createDataFrame(scaledRDD, VectorPair.class);
        scaled_trainingDataset.show();
        return scaled_trainingDataset;
    }

    private static MultilayerPerceptronClassificationModel trainNetwork(Dataset<Row> scaled_trainingDataset) {
        Dataset<Row> scaledData2 = MLUtils.convertVectorColumnsToML(scaled_trainingDataset);
        Dataset<Row> data = scaledData2.toDF("features", "label");
        Dataset<Row>[] datasets = data.randomSplit(new double[]{0.80, 0.20}, 12345L);
        trainingData = datasets[0];
        validationData = datasets[1];

        int[] layers = new int[] {10, 16, 32, 2};

        MultilayerPerceptronClassifier mlp = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(128)
                .setSeed(1234L)
                .setTol(1E-8)
                .setMaxIter(1000);

        MultilayerPerceptronClassificationModel model = mlp.fit(trainingData);  //treina
        return model;
    }

    private static void evaluateNetwork (MultilayerPerceptronClassificationModel model) {
       
        Dataset<Row> predictions = model.transform(validationData);
        predictions.show();
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                                                        .setLabelCol("label").setPredictionCol("prediction");
        MulticlassClassificationEvaluator evaluator1 = evaluator.setMetricName("accuracy");
        MulticlassClassificationEvaluator evaluator2 = evaluator.setMetricName("weightedPrecision");
        MulticlassClassificationEvaluator evaluator3 = evaluator.setMetricName("weightedRecall");
        MulticlassClassificationEvaluator evaluator4 = evaluator.setMetricName("f1");

        double accuracy = evaluator1.evaluate(predictions);
        double precision = evaluator2.evaluate(predictions);
        double recall = evaluator3.evaluate(predictions);
        double f1 = evaluator4.evaluate(predictions);
        System.out.println("Accuracy = " + accuracy);
        System.out.println("Precision = " + precision);
        System.out.println("Recall = " + recall);
        System.out.println("F1 = " + f1);
        System.out.println("Test Error = " + (1 - accuracy));
    }

    private static Dataset<Row> getTestData() {
        spark.sqlContext().udf().register("normSex", normSex, DataTypes.IntegerType);
        spark.sqlContext().udf().register("normEmbarked", normEmbarked, DataTypes.IntegerType);
        
        Dataset<Row> testData = spark.sqlContext()
                .read()
                .format("com.databricks.spark.csv")
                .option("header", "true")
                .option("inferSchema", "true")
                .load("data/test.csv");
        
                return testData.select(
                col("PassengerId"),
                col("Fare"),
                callUDF("normSex", col("Sex")).alias("Sex"),
                col("Age"),
                col("Pclass"),
                col("Parch"),
                col("SibSp"),
                callUDF("normEmbarked", col("Embarked")).alias("Embarked"));
    }
    private static Dataset<Row> testNetwork(MultilayerPerceptronClassificationModel model) {

        Dataset<Row> testData = getTestData();
        testData.show();
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("Age", mean_age_column);
        m.put("Fare", mean_fare_column);
        Dataset<Row> testData2 = testData.na().fill(m);
        testData2.show();

        JavaRDD<VectorPair> testRDD = testData2.javaRDD().map(row -> {
            VectorPair vectorPair = new VectorPair();
            vectorPair.setLabel(row.<Integer>getAs("PassengerId"));
            vectorPair.setFeatures(getScaledVector(
                    row.<Double>getAs("Fare"),
                    row.<Double>getAs("Age"),
                    row.<Integer>getAs("Pclass"),
                    row.<Integer>getAs("Sex"),
                    row.<Integer>getAs("Embarked"),
                    scaler));
            return vectorPair;
        });

        Dataset<Row> scaledTestData
                = spark.createDataFrame(testRDD, VectorPair.class);
        Dataset<Row> finalTestData
                = MLUtils.convertVectorColumnsToML(scaledTestData)
                .toDF("features", "PassengerId");
        trainingData.show();
        finalTestData.show();
        Dataset<Row> resultDF = model.transform(finalTestData)
                .select("PassengerId", "prediction");
        resultDF.show();
        return resultDF;
    }
}



