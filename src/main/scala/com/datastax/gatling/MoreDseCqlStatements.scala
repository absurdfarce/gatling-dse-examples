package com.datastax.gatling

import com.datastax.oss.driver.api.core.cql.{
  SimpleStatement => SimpleS,
  SimpleStatementBuilder => SimpleB
}
import com.datastax.gatling.plugin.model.{DseCqlAttributes, DseCqlAttributesBuilder, DseCqlStatement}
import io.gatling.commons.validation._
import io.gatling.core.session._

/**
 * Working around a small problem with the sample scenario.  We want a query operation after insert to confirm that
 * we have a unique value with a specific number, but this op is heavily dependent upon the random int given us by the
 * feeder.  We don't have a way to access session vals within SimpleStatements via the existing plugin API (or even
 * the recent changes) so we implement a workaround.
 *
 * Code below is designed to retrieve a stored SimpleStatement from the Gatling session.  This is very much a hack
 * since (among other reasons) we wind up with a attributes object which doesn't have a valid Statement subclass for
 * most of it's life... but it work for what we need.
 */
object MoreDseCqlStatements {
  def simpleStatementFromSession(tag:String, key:String): DseCqlAttributesBuilder[SimpleS, SimpleB] = {
    DseCqlAttributesBuilder[SimpleS, SimpleB](
      DseCqlAttributes(
        tag,
        new DseCqlSimpleStatementFromSession(key),
        cqlStatements = Seq()
      ))
  }
}

class DseCqlSimpleStatementFromSession(key: String)
  extends DseCqlStatement[SimpleS, SimpleB] {

    def buildFromSession(gatlingSession: Session): Validation[SimpleB] = {
      gatlingSession.attributes.get(key).get.asInstanceOf[SimpleB].success
    }
}
