package vertx.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

public class WebService {

	private final Vertx vertx;

	public WebService(Vertx vertx) {
		this.vertx = vertx;
	}

	public void start(CountService counter, Handler<AsyncResult<Void>> done) {
		HttpServer server = vertx.createHttpServer();

		server.requestHandler(request -> {
			HttpServerResponse response = request.response();
			response.putHeader("content-type", "text/plain");
			response.end("Hello World from vert.x! Count = " + counter.increment() + "\n" + getRSS());
		});

		server.listen(8090, res -> {
			if (res.succeeded()) {
				done.handle(Future.succeededFuture());
			} else {
				done.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	private String getRSS() {
		try (BufferedReader in = new BufferedReader(new FileReader(new File("/proc/self/status")))) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				if (line.startsWith("VmRSS")) {
					return line;
				}
			}
			return "RSS not found";
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}
