package com.datastax.gatling

import com.datastax.driver.core.{SimpleStatement => SimpleS}
import com.datastax.gatling.plugin.model.{DseCqlAttributes, DseCqlAttributesBuilder, DseCqlSimpleStatement, DseCqlStatement}
import io.gatling.commons.validation._
import io.gatling.core.session._

object MoreDseCqlStatements {
  def simpleStatementFromSession(tag:String, key:String): DseCqlAttributesBuilder = {
    DseCqlAttributesBuilder(
      DseCqlAttributes(
        tag,
        new DseCqlSimpleStatementFromSession(key),
        cqlStatements = Seq()
      ))
  }
}

class DseCqlSimpleStatementFromSession(key: String)
  extends DseCqlStatement {

    def buildFromSession(gatlingSession: Session): Validation[SimpleS] = {
      gatlingSession.attributes.get(key).get.asInstanceOf[SimpleS].success
    }
}
