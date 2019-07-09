package org.avlasov.hadoopexample.words;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.BasicConfigurator;

public class FileWordsJob extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        ToolRunner.run(new FileWordsJob(), args);
    }

    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance();
        job.setJarByClass(FileWordsJob.class);
        job.setJobName("File words count");

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setMapperClass(FileWordsMapper.class);
        job.setReducerClass(FileWordsReducer.class);

        if(job.waitForCompletion(true)) {
            System.out.println("Job was successful");
            return 0;
        } else {
            System.out.println("Job was not successful");
            return 1;
        }
    }

}
