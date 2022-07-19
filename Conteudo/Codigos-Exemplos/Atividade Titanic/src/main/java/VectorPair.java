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

public class VectorPair {
    private double label;
    private Vector features;

    public VectorPair(double label, Vector features) {
        this.label = label;
        this.features = features;
    }
    public VectorPair() {}
    public Vector getFeatures() { return this.features; }
    public void setFeatures(Vector features) { this.features = features; }
    public double getLabel() { return this.label; }
    public void setLabel(double label) { this.label = label; }
}
