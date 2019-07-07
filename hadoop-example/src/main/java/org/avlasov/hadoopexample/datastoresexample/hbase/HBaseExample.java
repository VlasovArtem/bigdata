package org.avlasov.hadoopexample.datastoresexample.hbase;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.rest.client.Client;
import org.apache.hadoop.hbase.rest.client.Cluster;
import org.apache.hadoop.hbase.rest.client.RemoteAdmin;
import org.apache.hadoop.hbase.rest.client.RemoteHTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.BasicConfigurator;
import org.avlasov.entity.Rating;
import org.avlasov.util.DataMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HBaseExample {

    private static final String RATINGS_TABLE_NAME = "ratings";
    private final String family;
    private final DataMapper dataMapper;
    private final String ratingsFilePath;
    private final Configuration configuration;
    private final Client client;

    public HBaseExample(DataMapper dataMapper, String ratingsFilePath, String name, int port) {
        this.dataMapper = dataMapper;
        this.ratingsFilePath = ratingsFilePath;
        BasicConfigurator.configure();
        configuration = HBaseConfiguration.create();
        Cluster cluster = new Cluster();
        cluster.add(name, port);
        client = new Client(cluster);
        family = "rating";
        createTable();
    }

    public void uploadRatings() {
        try (RemoteHTable ratingsTable = new RemoteHTable(client, RATINGS_TABLE_NAME)) {
            List<Put> ratingsPut = readRatingsFromFile().stream()
                    .map(mapRatingToPut())
                    .collect(Collectors.toList());
            ratingsTable.put(ratingsPut);
            ratingsTable.flushCommits();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Rating> readRatings(int userId) {
        try (RemoteHTable remoteHTable = new RemoteHTable(client, RATINGS_TABLE_NAME)) {
            Result result = remoteHTable.get(new Get(String.valueOf(userId).getBytes()));
            return result.getFamilyMap(family.getBytes())
                    .entrySet().stream()
                    .map(entry -> mapCellToRating(userId, entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Rating mapCellToRating(int userId, byte[] movieId, byte[] rating) {
        return Rating.builder()
                .userId(userId)
                .movieId(Integer.parseInt(Bytes.toString(movieId)))
                .rating(Integer.parseInt(Bytes.toString(rating)))
                .build();
    }

    private void createTable() {
        try {
            RemoteAdmin remoteAdmin = new RemoteAdmin(client, configuration);
            if (remoteAdmin.isTableAvailable(RATINGS_TABLE_NAME)) {
                remoteAdmin.deleteTable(RATINGS_TABLE_NAME);
            }
            remoteAdmin.createTable(createTableDescriptor());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HTableDescriptor createTableDescriptor() {
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(RATINGS_TABLE_NAME));
        hTableDescriptor.addFamily(new HColumnDescriptor(family));
        return hTableDescriptor;
    }

    private List<Rating> readRatingsFromFile() {
        try (FileInputStream fileInputStream = new FileInputStream(new File(ratingsFilePath))) {
            return IOUtils
                    .readLines(fileInputStream,  Charset.defaultCharset())
                    .stream()
                    .map(dataMapper::mapRating)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }
    }

    private Function<Rating, Put> mapRatingToPut() {
        return rating -> {
            Put put = new Put(String.valueOf(rating.getUserId()).getBytes());
            put.addColumn(family.getBytes(),
                    String.valueOf(rating.getMovieId()).getBytes(),
                    String.valueOf(rating.getRating()).getBytes());
            return put;
        };
    }

    public static void main(String[] args) {
        HBaseExample hBaseExample = new HBaseExample(new DataMapper(), args[0], "localhost", 8000);
        hBaseExample.uploadRatings();
        List<Rating> ratings = hBaseExample.readRatings(0);
        System.out.println(ratings);
    }

}
