package org.avlasov.sparkexample.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.apache.log4j.BasicConfigurator;
import org.avlasov.sparkexample.util.SparkDataMapper;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.hamcrest.collection.IsCollectionWithSize;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CassandraExampleTest {

    private static EmbeddedCassandraService cassandra;

    private static Cluster cluster;
    private static Session session;
    private CassandraExample cassandraExample =
            new CassandraExample("../data/movie-ratings.txt", "../data/movie-data.txt", "../data/users.txt", new SparkDataMapper());

    public static void beforeClass() throws Exception {
        BasicConfigurator.configure();

        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(DatabaseDescriptor.getNativeTransportPort()).build();
        session = cluster.connect();
    }

    public void uploadUsers() {
        cassandraExample.uploadUsers();

        ResultSet execute = session.execute("SELECT * FROM users;");

        List<Row> all = execute.all();
        assertThat(all, IsCollectionWithSize.hasSize(943));

        Optional<Row> first = all.stream()
                .filter(row -> row.getInt(0) == 10)
                .findFirst();

        assertTrue(first.isPresent());

        assertEquals(10, first.get().getInt(0));
        assertEquals(53, first.get().getInt(1));
        assertEquals("M", first.get().getString(2));
        assertEquals("lawyer", first.get().getString(3));
        assertEquals("90703", first.get().getString(4));
    }

    public static void classTearDown() {
        session.close();
        cluster.close();
    }

}