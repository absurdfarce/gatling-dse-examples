package com.datastax.gatling;


import com.datastax.dse.driver.api.core.DseSession;

import java.net.InetSocketAddress;

/**
 * Simple factory class to avoid Session references in DseSession builder when
 * attempting similar ops directly in Scala.
 */
public class DseSessionFactory {

    public static DseSession build(String host) {
        return DseSession.builder()
                .withLocalDatacenter("Cassandra")
                .addContactPoint(new InetSocketAddress(host,9042))
                .build();
    }
}
