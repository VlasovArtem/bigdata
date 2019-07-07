package org.avlasov.hadoopexample.counter.words;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FileWordsReducer extends Reducer<Text, Text, Text, Text> {

    private Text docId;

    public FileWordsReducer() {
        docId = new Text();
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        Set<String> uniqueDocIds = new HashSet<>();

        for (Text value : values) {
            uniqueDocIds.add(value.toString());
        }
        docId.set(new Text(StringUtils.join(uniqueDocIds, ", ")));
        context.write(key, docId);
    }
}
