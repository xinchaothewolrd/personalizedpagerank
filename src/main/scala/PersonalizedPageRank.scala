import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object PersonalizedPageRank {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("PersonalizedPageRank")
      .setMaster("local[*]")

    val sc = new SparkContext(conf)

    val links = sc.parallelize(Seq(
      ("A", Seq("B", "C")),
      ("B", Seq("A", "C", "D")),
      ("C", Seq("A", "B")),
      ("D", Seq("B", "C"))
    ))

    val sourceNode = "A"
    val dampingFactor = 0.15
    val iterations = 10

    var ranks = links.map { case (node, _) =>
      if (node == sourceNode) (node, 1.0) else (node, 0.0)
    }

    for (i <- 1 to iterations) {
      val contribs = links.join(ranks).flatMap {
        case (node, (neighbors, rank)) =>
          val size = neighbors.size
          neighbors.map(dest => (dest, rank / size))
      }

      ranks = contribs.reduceByKey(_ + _).mapValues { v =>
        (1 - dampingFactor) * v
      }.map {
        case (node, rank) =>
          if (node == sourceNode) (node, rank + dampingFactor) else (node, rank)
      }
    }

    ranks.collect().sortBy(-_._2).foreach(println)
    sc.stop()
  }
}
