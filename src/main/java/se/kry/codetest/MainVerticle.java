package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.*;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, String> services = new HashMap<>();
  //TODO use this
  private DBConnector connector;
  private DBService dbService;
  private BackgroundPoller poller;

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    dbService = new DBService(connector);
    poller = new BackgroundPoller(dbService);
    Router router = Router.router(vertx);

    services = dbService.getServicesFromDbIntoMap();

    router.route().handler(BodyHandler.create());
    services.put("https://www.kry.se", "UNKNOWN");
    vertx.setPeriodic(1000 * 10, timerId -> poller.pollServices(services, vertx));
    setRoutes(router);
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8080, result -> {
        if (result.succeeded()) {
          System.out.println("KRY code test service started");
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }

  private void setRoutes(Router router) {
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put("name", service.getKey())
                              .put("status", service.getValue()))
              .collect(Collectors.toList());
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });

    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      Service newService = new Service();
      newService.setUrl(jsonBody.getString("url"));
      newService.setName(jsonBody.getString("name"));
      newService.setStatus("FAIL");

      saveNewService(newService);

      req.response()
          .putHeader("content-type", "text/plain")
          .end("OK");
    });

    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      deleteService(jsonBody.getString("name"));

      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
  }

  private void deleteService(String name) {
    String strDelete = "DELETE FROM SERVICE WHERE NAME = ?";
    JsonArray params = new JsonArray();
    params.add(name);
    connector.query(strDelete, params).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("delete successful");
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void saveNewService(Service service) {
    String strInsert = "INSERT INTO SERVICE (URL, NAME, STATUS) VALUES (?,?,?)";
    JsonArray params = new JsonArray();
    params.add(service.getUrl()).add(service.getName()).add(service.getStatus());
    connector.query(strInsert, params).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("add successful");
      } else {
        done.cause().printStackTrace();
      }
    });
  }
}



