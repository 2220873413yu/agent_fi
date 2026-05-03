package com.xms.common.config.redis.delayqueue.listener;

import com.xms.common.config.redis.delayqueue.message.FastJsonCodec;
import com.xms.common.config.redis.delayqueue.message.RedissonMessage;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.decoder.MultiDecoder;
import org.redisson.command.CommandAsyncExecutor;

import java.util.List;
import java.util.Objects;

/**
 * @author GT63S
 */
@Slf4j
public class SimpleRedissonListenerContainer extends AbstractRedissonListenerContainer {

    private RedisCommand<Object> LPOP_VALUE = new RedisCommand<>("LPOP", new MessageDecoder<>());
    private AsyncMessageProcessingConsumer takeMessageTask;

    public SimpleRedissonListenerContainer(ContainerProperties containerProperties) {
        super(containerProperties);
    }

    @Override
    protected void doStart() {
        this.takeMessageTask = new AsyncMessageProcessingConsumer();
        this.getTaskExecutor().execute(this.takeMessageTask);
    }

    @Override
    protected void doStop() {
        this.takeMessageTask.stop();
    }

    private static class MessageDecoder<T> implements MultiDecoder<T> {

        @Override
        public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
            return codec.getValueDecoder();
        }

        @Override
        public T decode(List<Object> parts, State state) {
            if (parts == null) {
                return null;
            }
            return (T) parts.get(0);
        }
    }

    private final class AsyncMessageProcessingConsumer implements Runnable {

        private volatile Thread currentThread = null;

        private volatile ConsumerStatus status = ConsumerStatus.CREATED;

        @Override
        public void run() {
            if (this.status != ConsumerStatus.CREATED) {
                log.info("consumer currentThread [{}] will exit, because consumer status is {},expected is CREATED", this.currentThread.getName(), this.status);
                return;
            }
            final String queue =     SimpleRedissonListenerContainer.this.getContainerProperties().getQueue();
            final Redisson redisson = (Redisson)     SimpleRedissonListenerContainer.this.getRedissonClient();
            final RBlockingQueue<Object> blockingQueue = redisson.getBlockingQueue(queue, FastJsonCodec.INSTANCE);
			CommandAsyncExecutor commandExecutor = redisson.getCommandExecutor();
            this.currentThread = Thread.currentThread();
            this.status = ConsumerStatus.RUNNING;
            final long maxWaitMillis = 10;
            long emptyFetchTimes = 0;
            for (; ; ) {
                try {
                    RFuture<RedissonMessage> asyncResult = commandExecutor.writeAsync(blockingQueue.getName(), blockingQueue.getCodec(), LPOP_VALUE, blockingQueue.getName());
                    RedissonMessage redissonMessage = commandExecutor.get(asyncResult);
                    if (Objects.isNull(redissonMessage)) {
                        Thread.sleep(Math.min(++emptyFetchTimes * 2, maxWaitMillis));
                    } else {
                        //reset counting
                        emptyFetchTimes = 0;
                            SimpleRedissonMessageListenerAdapter redissonListener = (    SimpleRedissonMessageListenerAdapter)     SimpleRedissonListenerContainer.this.getRedissonListener();
                        redissonListener.onMessage(redissonMessage);
                    }
                } catch (InterruptedException | RedisException e) {
                    //ignore
                } catch (Exception e) {
                    log.error("error occurred while take message from redisson", e);
                }
                if (this.status == ConsumerStatus.STOPPED) {
                    log.info("consumer currentThread [{}] will exit, because of STOPPED status", this.currentThread.getName());
                    break;
                }
            }
            this.currentThread = null;
        }

        private void stop() {
            if (this.currentThread != null) {
                this.status = ConsumerStatus.STOPPED;
                this.currentThread.interrupt();
            }
        }
    }

}
