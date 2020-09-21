package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.*;

public class BackgroundPoller {

  private DBService dbService;

  public BackgroundPoller(DBService dbService) {
    this.dbService = dbService;
  }

  public Future<List<String>> pollServices(Map<String, String> serviceMap, Vertx vertx) {
    //TODO
    System.out.println("poll running...");

    DBConnector connector = new DBConnector(vertx);
    List <String> listUrl = new ArrayList<>();

    connector.query("SELECT URL FROM SERVICE;").setHandler(done -> {
      if (done.succeeded()) {
        List<JsonObject> jsonRows = done.result().getRows();
        for (JsonObject jsonRow: jsonRows) {
          listUrl.add(jsonRow.getString("URL"));
        }
      }
    });

    String strUpdate = "UPDATE SERVICE SET STATUS = ? WHERE URL = ?";
    for (String url: listUrl) {
      WebClient.create(vertx).getAbs(url).send(res -> {
        if (res.succeeded()) {
          JsonArray params = new JsonArray();
          params.add("OK").add(url);
          connector.query(strUpdate, params).setHandler(done -> {
            if (done.succeeded()) {
              System.out.println("status OK update successful");
            } else {
              done.cause().printStackTrace();
            }
          });
        } else {
          JsonArray params = new JsonArray();
          params.add("FAIL").add(url);
          connector.query(strUpdate, params).setHandler(done -> {
            if (done.succeeded()) {
              System.out.println("status FAIL update successful");
            } else {
              done.cause().printStackTrace();
            }
          });
        }
      });
    }

    return Future.failedFuture("TODO");
  }
}
