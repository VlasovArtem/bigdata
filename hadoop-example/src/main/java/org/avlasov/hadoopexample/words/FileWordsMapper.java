package org.avlasov.hadoopexample.words;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class FileWordsMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text documentId;
    private Text word;

    public FileWordsMapper() {
        word = new Text();
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        documentId = new Text(((FileSplit) context.getInputSplit()).getPath().getName());
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        for (String token : StringUtils.split(value.toString())) {
            word.set(token);
            context.write(word, documentId);
        }
    }
}
