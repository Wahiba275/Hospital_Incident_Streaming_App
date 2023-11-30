# Hospital_Incident_Streaming_App


## Table of Contents

- 📝 [Introduction](#introduction)
- 🚀 [Principales Caractéristiques](#principales-caractéristiques)
- 🏛️ [Architecture](#architecture)
- 📋 [Cas d'Utilisation](#cas-dutilisation)
- 🏥 [Hospital Incidents Streaming App](#hospital-incidents-streaming-app)
- 📈 [Résultats](#résultats)
- 🏁 [Conclusion](#conclusion)

## 📝Introduction

Spark Streaming est un module de traitement des données en temps réel dans le framework Apache Spark. Il permet aux développeurs de créer des applications de traitement des données en continu, en ingérant et en traitant les flux de données en temps réel avec une latence minimale. Cette technologie est largement utilisée dans divers domaines tels que l'analyse en temps réel, la détection d'anomalies, la surveillance, etc.

## 🚀Principales Caractéristiques

1. **Extensibilité:**
   - Spark Streaming s'intègre parfaitement avec le framework Spark, permettant une évolutivité horizontale. Il exploite la même architecture RDD (Resilient Distributed Datasets) pour garantir une tolérance aux pannes et une distribution des données.

2. **Sources de Données Prises en Charge:**
   - Spark Streaming peut ingérer des données à partir de diverses sources telles que Kafka, Flume, HDFS, Twitter, et bien d'autres. Cela offre une flexibilité considérable pour les développeurs lors de la conception de pipelines de traitement des données en continu.

3. **Intégration avec Spark Core:**
   - Le module Spark Streaming est étroitement intégré avec Spark Core, ce qui signifie que les développeurs peuvent combiner le traitement en temps réel avec le traitement par lots dans la même application Spark. Cela offre une approche unifiée pour traiter les données à la fois en temps réel et par lots.

4. **Traitement de Fenêtres:**
   - Spark Streaming prend en charge le traitement basé sur des fenêtres temporelles, facilitant la création d'analyses basées sur des fenêtres glissantes ou des fenêtres fixes.

5. **API Conviviale:**
   - L'API Spark Streaming est conçue pour être similaire à l'API de traitement par lots de Spark, simplifiant ainsi la transition des développeurs du traitement par lots au traitement en temps réel.

## 🏛️Architecture

L'architecture de Spark Streaming est basée sur le concept de micro-batch. Les flux de données en continu sont divisés en petits lots, puis traités par le moteur Spark Core. Ces lots peuvent être traités en parallèle, offrant une scalabilité horizontale.

## 📋Cas d'Utilisation

1. **Analyse en Temps Réel:**
   - Spark Streaming est souvent utilisé pour l'analyse en temps réel des données provenant de sources telles que les réseaux sociaux, les capteurs IoT, etc. Cela permet aux entreprises de prendre des décisions plus rapides en fonction des dernières informations disponibles.

2. **Surveillance et Alerte:**
   - Les entreprises utilisent Spark Streaming pour surveiller en temps réel les métriques, les journaux, et déclencher des alertes immédiates en cas de comportement anormal.

3. **Traitement de Flux d'Événements:**
   - Les applications de traitement de flux d'événements, comme le traitement des transactions financières en temps réel, bénéficient de Spark Streaming pour son traitement efficace des flux de données.
# 🏥Hospital Incidents Streaming App

Ce programme Java utilise Apache Spark pour effectuer un traitement en continu sur un flux de données représentant des incidents hospitaliers.

```java
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
```
Dans cette section du code, nous effectuons l'initialisation de la session Spark (SparkSession) et définissons le schéma des données qui seront traitées par le programme Spark. La session Spark est essentielle pour l'utilisation des fonctionnalités de Spark, et nous lui attribuons un nom distinct, "Hospital incidents streaming app", pour une identification claire dans l'interface utilisateur de Spark. De plus, nous spécifions le mode maître en local avec autant de threads que de cœurs disponibles sur la machine.

En ce qui concerne le schéma des données, nous utilisons la classe StructType pour décrire la structure des informations que nous allons manipuler. Nous créons des objets StructField pour chaque champ du schéma, en précisant le nom du champ, son type de données, et si la valeur peut être nulle. Dans cet exemple, le schéma comprend cinq champs : "id" de type Long, "titre" de type String, "description" de type String, "service" de type String, et "date" de type String. Cette étape préliminaire est cruciale pour assurer la cohérence des données traitées tout au long du processus de streaming.

```java
        Dataset<Row> incidents = ss
                .readStream()
                .option("header", "true")
                .schema(schema)
                .csv("src/main/resources");

```
Cette portion de code est responsable de la lecture en continu des données à partir d'un flux CSV à l'aide de Spark
## Afficher d’une manière continue le nombre d’incidents par service

```java
        // compter le nombre d'incidents par service
        Dataset<Row> incidentsCountByService = incidents.groupBy("service").count();
       StreamingQuery query = incidentsCountByService
                .writeStream()
                .outputMode("update")
                .format("console")
                .trigger(Trigger.ProcessingTime("5000 milliseconds"))
                .start();
```

## Afficher d’une manière continue les deux année ou il a y avait plus d’incidents
```java
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
```

Ces lignes de code marquent la fin du traitement en continu du flux de données. En utilisant les méthodes awaitTermination(), le programme Spark attend que les deux requêtes (query et query1) se terminent avant de poursuivre ou de se terminer lui-même.
```java
       //la fin du streaming
        query.awaitTermination();
        query1.awaitTermination();
```
**Note:** Les résultats sont continuellement mis à jour en fonction de la configuration du déclencheur.

## 📈Résultats
Les résultats varient entre les lots de traitement, avec des différences notables dans le nombre d'incidents par service et par année. Ces variations peuvent être dues à l'arrivée de nouvelles données, des opérations de fenêtrage temporel, le parallélisme de traitement de Spark, et des nuances dans l'ordre de traitement des données. Une compréhension approfondie de la logique de l'application est essentielle pour interpréter ces divergences.
1. ***Batch 0***
   
![Nom de l'image](/streaming/batch00.PNG)

2. ***Batch 1***
   
![Nom de l'image](/streaming/batch11.PNG)

3. ***Batch 2***
   
![Nom de l'image](/streaming/batch2.PNG)


## 🏁Conclusion

En un an, Spark Streaming a émergé comme une technologie puissante pour le traitement des données en temps réel. Son intégration transparente avec Spark Core, sa flexibilité dans l'ingestion de données, et son modèle de programmation convivial en font un choix populaire parmi les développeurs pour les applications de traitement en continu. Avec une communauté active et un support continu, Spark Streaming reste une solution de pointe pour les besoins d'analyse en temps réel.
