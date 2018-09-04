package net.landzero.xlog.logback;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.landzero.xlog.utils.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class XLogRedisAppender extends XLogBaseAppender {

    public static final String LIST_KEY = "xlog";

    private static final Gson GSON = new Gson();

    /**
     * public variables
     */

    @NotNull
    private ArrayList<String> hosts = new ArrayList<>();

    @NotNull
    public ArrayList<String> getHosts() {
        return hosts;
    }

    public void addHost(@Nullable String url) {
        url = Strings.normalize(url);
        if (url != null) {
            this.hosts.add(url);
        }
    }

    /**
     * internal variables and methods
     */
    private String source = null;

    private void initSource() {
        if (getEnv() == null || getTopic() == null || getProject() == null) {
            return;
        }
        this.source = String.format("/var/log/%s/%s/%s.log", getEnv(), getTopic(), getProject());
    }

    @NotNull
    private ArrayList<JedisPool> jedisPools = new ArrayList<>();

    @NotNull
    private AtomicInteger index = new AtomicInteger();

    @NotNull
    private String hostname = "localhost";

    private void initHostname() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            this.hostname = "localhost";
        }
    }

    private void initJedisPools() {
        this.lock.lock();
        unsafeInitJedisPools();
        this.lock.unlock();
    }

    private void closeJedisPools() {
        this.lock.lock();
        unsafeCloseJedisPools();
        this.lock.unlock();
    }

    private void unsafeInitJedisPools() {
        unsafeCloseJedisPools();
        for (String url : getHosts()) {
            this.jedisPools.add(new JedisPool(url));
        }
    }

    private void unsafeCloseJedisPools() {
        for (JedisPool pool : this.jedisPools) {
            pool.close();
        }
        this.jedisPools = new ArrayList<>();
    }

    @NotNull
    private Jedis getJedis() {
        return getJedis(this.jedisPools.size());
    }

    @NotNull
    private Jedis getJedis(int retry) {
        // if retry is too small, returns null
        if (retry < 1) {
            throw new JedisConnectionException("failed to find a reachable redis instance");
        }
        // roll the round robin
        int index = this.index.addAndGet(1);
        if (index < 0) {
            index = -index;
        }
        // find a pool and get a jedis
        try {
            return this.jedisPools.get(index % this.jedisPools.size()).getResource();
        } catch (Exception ignored) {
        }
        return getJedis(retry - 1);
    }

    @NotNull
    private String createMessage(@NotNull String message) {
        JsonObject root = new JsonObject();
        JsonObject beat = new JsonObject();
        beat.addProperty("hostname", this.hostname);
        root.add("beat", beat);
        root.addProperty("source", this.source);
        root.addProperty("message", message);
        return GSON.toJson(root);
    }

    @Override
    public void start() {
        if (this.hosts.size() == 0) {
            addError("no 'url' is specified");
            return;
        }
        initSource();
        if (this.source == null) {
            addError("failed to prepare source, check if 'env', 'topic' or 'project' field is missing");
            return;
        }
        initHostname();
        initJedisPools();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        closeJedisPools();
    }

    @Override
    protected void appendString(@NotNull String string) {
        try (Jedis jedis = getJedis()) { // use try-with-resource
            jedis.rpush(LIST_KEY, createMessage(string));
        } catch (Exception ignored) {
        }
    }

}
