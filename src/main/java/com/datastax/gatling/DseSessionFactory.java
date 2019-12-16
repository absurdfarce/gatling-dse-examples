package com.datastax.gatling;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;

/**
 * Simple factory class to avoid Session references in DseSession builder when
 * attempting similar ops directly in Scala.
 */
public class DseSessionFactory {

    public static DseSession build(String host) {
        return DseCluster.builder().addContactPoint(host).build().connect();
    }
}
