package ma.enset;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;

import static org.apache.spark.sql.functions.*;


public class HospitalIncidentStreamingApp {
    public static void main(String[] args) throws Exception {

        SparkSession ss = SparkSession.builder().appName("Hospital incidents streaming app").master("local[*]").getOrCreate();
        StructType schema = new StructType(
                new StructField[]{
                        new StructField("id", DataTypes.LongType, false, Metadata.empty()),
                        new StructField("titre", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("description", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("service", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("date", DataTypes.StringType, false, Metadata.empty()),
                }
        );

        Dataset<Row> incidents = ss
                .readStream()
                .option("header", "true")
                .schema(schema)
                .csv("src/main/resources");

        // compter le nombre d'incidents par service
        Dataset<Row> incidentsCountByService = incidents.groupBy("service").count();
       StreamingQuery query = incidentsCountByService
                .writeStream()
                .outputMode("update")
                .format("console")
                .trigger(Trigger.ProcessingTime("5000 milliseconds"))
                .start();

        // compter le nombre d'incidents par année
        Dataset<Row> incidentsByYear = incidents
                .withColumn("year", year(col("date")))
                .groupBy("year")
                .agg(count("*").as("incidentCount"))
                .orderBy(desc("incidentCount"));
        StreamingQuery query1= incidentsByYear
                .writeStream()
                .outputMode("complete")
                .format("console")
                .trigger(Trigger.ProcessingTime("5000 milliseconds"))
                .start();


        //la fin du streaming
        query.awaitTermination();
        query1.awaitTermination();
    }
}