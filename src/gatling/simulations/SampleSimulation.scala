import com.datastax.gatling.{DseSessionFactory, MoreDseCqlStatements}
import io.gatling.core.Predef._
import com.datastax.gatling.plugin.DsePredef._
import com.datastax.gatling.plugin.utils.ResultSetUtils.{asyncResultSetToIterator => toIter}
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder
import com.datastax.oss.driver.api.querybuilder.{QueryBuilder, SchemaBuilder}

import collection.JavaConverters._

/**
 * Adapted from the GatlingCql sample at https://github.com/gatling-cql/GatlingCql
 */
class SampleSimulation extends Simulation {

  val keyspace = "test"
  val table_name = "test_table"
  val session = DseSessionFactory.build("127.0.0.1")
  val cqlConfig = dseProtocolBuilder.session(session)

  session.execute(
    SchemaBuilder.createKeyspace(keyspace).ifNotExists()
      .withReplicationOptions(
        Map("class" -> "SimpleStrategy".asInstanceOf[Object], "replication_factor" -> "1").asJava)
      .build())
  session.execute(
    SchemaBuilder.createTable(keyspace, table_name).ifNotExists()
      .withPartitionKey("id",DataTypes.TIMEUUID)
      .withClusteringColumn("num", DataTypes.INT)
      .withStaticColumn("str", DataTypes.TEXT)
      .build())
  val prepared =
    session.prepare(
      QueryBuilder.insertInto(keyspace, table_name)
        .value("id",QueryBuilder.now())
        .value("num", QueryBuilder.bindMarker())
        .value("str", QueryBuilder.bindMarker())
    .build())

  val random = new util.Random
  val feeder = Iterator.continually(
    Map(
      "randomString" -> random.nextString(20),
      "randomNum" -> random.nextInt()
    ))
  val enhancedFeeder =
    feeder.map((base:Map[String,Any]) => {
      val query = s"select * from test.test_table where num = ${base.get("randomNum").get} allow filtering"
      base + ("query" -> new SimpleStatementBuilder(query))
  })

  val scn = scenario("Sample")
    .repeat(10) {
      feed(enhancedFeeder)
        .exec(
          cql("Prepared insert")
            .executeStatement(prepared)
            .withParams(List("randomNum", "randomString"))
            .check(warnings.transform(_.size).is(0))
        )
        .exec(
          MoreDseCqlStatements.simpleStatementFromSession("Simple select","query")
            // The existing API would implement a check using something like this:
            //.check(warnings.transform(_.size).is(0))
            // Another way to implement the same test would be to work directly off the ResultSet:
            .check(resultSet.transform(_.getExecutionInfo.getWarnings.size).is(0))

            // The old allRows check can also now be expressed in terms of the result set only.  We
            // no longer need this:
            //.check(allRows.transform(_.size).is(1))
            // since we now have:
            .check(resultSet.transform(toIter(_).toSeq.size).is(1))

            // Even the above change may be too much since the conversion to a Seq likely requires a full realization
            // of the iterator before determining a size.  Especially for single-page queries we can leverage the
            // ResultSet more efficiently
            .check(resultSet.transform(_.hasMorePages).is(false))
            .check(resultSet.transform(_.remaining()).is(1))
        )
    }

  setUp(scn.inject(atOnceUsers(10)))
      .protocols(cqlConfig)

  after(session.close)
}
