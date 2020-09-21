package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;

public class DBService {

    private DBConnector connector;

    DBService(DBConnector connector) {
        this.connector = connector;
    }

    public HashMap<String, String> getServicesFromDbIntoMap () {
        HashMap<String, String> services = new HashMap<>();

        connector.query("SELECT URL, NAME, DATE_ADD, STATUS FROM SERVICE;").setHandler(done -> {
            if (done.succeeded()) {
                List<JsonObject> jsonRows = done.result().getRows();
                for (JsonObject jsonRow: jsonRows) {
                    services.put(jsonRow.getString("URL"), "URL: " + jsonRow.getString("URL") +
                            " NAME: " + jsonRow.getString("NAME") +
                            " DATE of ADD: " + jsonRow.getString("DATE_ADD") +
                            " STATUS: " + jsonRow.getString("STATUS"));
                }
            }
        });
        return services;
    }
}
