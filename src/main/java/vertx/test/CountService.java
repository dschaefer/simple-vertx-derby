package vertx.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class CountService {

	private final Vertx vertx;

	private SQLConnection conn;
	private int count;

	public CountService(Vertx vertx) throws Exception {
		this.vertx = vertx;
	}

	private void getInitialCount(Handler<AsyncResult<CountService>> done) {
		String get = "SELECT value FROM store WHERE name = 'count'";
		conn.query(get, res -> {
			if (res.succeeded()) {
				for (JsonArray row : res.result().getResults()) {
					count = row.getInteger(0);
				}
				done.handle(Future.succeededFuture(this));
			} else {
				done.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	private void createTable(Handler<AsyncResult<CountService>> done) {
		String create = "CREATE TABLE store (name VARCHAR(50), value INTEGER)";
		conn.execute(create, res -> {
			if (res.succeeded()) {
				String insert = "INSERT INTO store VALUES ('count', 0 )";
				conn.execute(insert, res2 -> {
					if (res2.succeeded()) {
						getInitialCount(done);
					} else {
						done.handle(Future.failedFuture(res.cause()));
					}
				});
			} else if (res.cause().getMessage().contains("already exists in Schema")) {
				getInitialCount(done);
			} else {
				done.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public void start(Handler<AsyncResult<CountService>> done) {
		JsonObject config = new JsonObject();
		config.put("url", "jdbc:derby:test.db;create=true");
		config.put("max_pool_size", 1);
		JDBCClient client = JDBCClient.createShared(vertx, config);
		client.getConnection(res -> {
			if (res.succeeded()) {
				conn = res.result();
				createTable(done);
			} else {
				done.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	public synchronized int increment() {
		int currentCount = ++count;
		String update = String.format("UPDATE store SET value = %d WHERE name = 'count'", currentCount);
		conn.execute(update, res -> {
			if (res.failed()) {
				res.cause().printStackTrace(System.err);
			}
		});
		return currentCount;
	}

}
