package com.phantom.dispatcher.server;

import com.phantom.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 处理分发消息的线程管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 15:28
 */
@Slf4j
public class ProcessorManager {

    private DispatcherConfig dispatcherConfig;

    /**
     * 阻塞队列
     */
    private List<ArrayBlockingQueue<ProcessorTask>> queues = null;

    /**
     * 处理线程集合
     */
    private List<Thread> processors = new ArrayList<>();

    /**
     * 是否停止
     */
    private volatile boolean shutdown = false;

    /**
     * 线程数量
     */
    private int threadNum;

    public ProcessorManager(DispatcherConfig dispatcherConfig) {
        this.dispatcherConfig = dispatcherConfig;
        initialize();
    }

    /**
     * 初始化,启动线程
     */
    private void initialize() {
        this.queues = new ArrayList<>();
        this.threadNum = dispatcherConfig.getThreadNum();
        for (int i = 0; i < threadNum; i++) {
            ArrayBlockingQueue<ProcessorTask> queue = new ArrayBlockingQueue<>(dispatcherConfig.getQueueSize());
            Processor processor = new Processor(queue);
            processor.start();
            processors.add(processor);
            queues.add(queue);
        }
    }

    /**
     * 根据uid进行hash分发到同一个线程去执行任务
     *
     * @param uid  用户ID
     * @param task 任务
     */
    public void addTask(String uid, ProcessorTask task) throws InterruptedException {
        int hash = Utils.toPositive(Utils.murmur2(uid.getBytes())) % threadNum;
        ArrayBlockingQueue<ProcessorTask> queue = queues.get(hash);
        queue.put(task);
    }


    private class Processor extends Thread {

        private ArrayBlockingQueue<ProcessorTask> queue;

        public Processor(ArrayBlockingQueue<ProcessorTask> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    ProcessorTask task = queue.take();
                    task.runTask();
                } catch (Exception e) {
                    log.error("运行分发任务发生异常：", e);
                }
            }
        }
    }

}
