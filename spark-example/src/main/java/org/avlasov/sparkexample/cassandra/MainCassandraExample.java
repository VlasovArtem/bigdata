package org.avlasov.sparkexample.cassandra;

import org.avlasov.entity.User;
import org.avlasov.sparkexample.util.SparkDataMapper;

import java.util.List;

public class MainCassandraExample {

    public static void main(String[] args) {
        CassandraExample cassandraExample = new CassandraExample(args[0], args[1], args[2], new SparkDataMapper());

        cassandraExample.uploadUsers();

        List<User> userWhereAgeLess = cassandraExample.getUserWhereAgeLess(20);

        for (User whereAgeLess : userWhereAgeLess) {
            System.out.println(whereAgeLess);
        }
    }

}
