package org.avlasov.hadoopexample.rating;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.stream.StreamSupport;

public class RatingReduce extends Reducer<IntWritable, IntWritable, IntWritable, Long> {

    @Override
    protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        long count = StreamSupport.stream(values.spliterator(), false)
                .count();
        context.write(key, count);
    }
}
