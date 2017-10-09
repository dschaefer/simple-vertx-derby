package vertx.test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class TestServer {

	public static void main(String[] args) throws Exception {
		VertxOptions options = new VertxOptions();
		options.setBlockedThreadCheckInterval(1000 * 60 * 60);
		Vertx vertx = Vertx.factory.vertx(options);

		CountService counter = new CountService(vertx);
		WebService web = new WebService(vertx);

		counter.start(res -> {
			if (res.succeeded()) {
				web.start(res.result(), res2 -> {
					if (res2.succeeded()) {
						System.out.println("started.");
					} else {
						res2.cause().printStackTrace(System.err);
					}
				});
			} else {
				res.cause().printStackTrace(System.err);
			}
		});
	}

}
