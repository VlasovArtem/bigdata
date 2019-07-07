package org.avlasov.sparkexample.mongodb;

import org.avlasov.entity.User;
import org.avlasov.sparkexample.util.SparkDataMapper;

import java.util.List;

public class MainMongoDBExample {

    public static void main(String[] args) {
        MongoDBExample mongoDBExample = new MongoDBExample(args[0], args[1], args[2], new SparkDataMapper());

        mongoDBExample.uploadUsers();

        List<User> userWhereAgeLess = mongoDBExample.getUserWhereAgeLess(20);

        for (User whereAgeLess : userWhereAgeLess) {
            System.out.println(whereAgeLess);
        }

    }

}
