package DistributedSystem.miaosha.kafka;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

public class kafkaConsumer {

    private ExecutorService threadPool;

    private List<kafkaConsumeTask> consumeTaskList;

    public kafkaConsumer(int threadNum) throws Exception {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .build();
        threadPool = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS, new
                LinkedBlockingDeque<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        consumeTaskList = new ArrayList<kafkaConsumeTask>(threadNum);

        for (int i = 0; i < threadNum; i++) {
            kafkaConsumeTask consumeTask = new kafkaConsumeTask(i);
            consumeTaskList.add(consumeTask);
        }
    }

    public void execute() {
        for (kafkaConsumeTask task : consumeTaskList) {
            threadPool.submit(task);
        }
    }
}