package com.isuwang.dapeng.remoting;

import java.util.concurrent.CompletableFuture;

/**
 * Created by tangliu on 2016/6/3.
 */
public class AsyncRequestWithTimeout {

    public AsyncRequestWithTimeout(String seqid, long timeout, CompletableFuture future) {

        this.seqid = seqid;
        this.timeout = System.currentTimeMillis() + timeout;
        this.future = future;
    }

    private long timeout;

    private String seqid;

    private CompletableFuture<?> future;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getSeqid() {
        return seqid;
    }

    public void setSeqid(String seqid) {
        this.seqid = seqid;
    }

    public CompletableFuture getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture future) {
        this.future = future;
    }
}
