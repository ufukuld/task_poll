package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);

    connector.query("CREATE TABLE IF NOT EXISTS SERVICE " +
            "(URL VARCHAR(128) NOT NULL," +
            "NAME VARCHAR(128) NOT NULL," +
            "DATE_ADD DATE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "STATUS VARCHAR(10))").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("completed db migrations");
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
