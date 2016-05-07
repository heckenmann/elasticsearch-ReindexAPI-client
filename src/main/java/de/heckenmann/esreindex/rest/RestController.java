package de.heckenmann.esreindex.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

/**
 * @author heckenmann
 */
public class RestController {

    private static final Logger LOG = Logger.getLogger(RestController.class.getName());

    private Client client;

    private String url;

    public RestController() {
        ClientConfig clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);
    }

    public WebTarget getCurrentWebTarget() {
        WebTarget webTarget = client.target(getUrl());
        return webTarget;
    }

    /**
     * Gibt alle Indizes der elasticsearch zurück.
     *
     * @return
     */
    public String[] getIndices() {
        return getValuesFromResponse(readPath("_stats", HttpMethod.GET, null), "indices");
    }

    /**
     * Gibt die Felder für einen Index zurück.
     */
    public String[] getFields(String index) {
        String response = readPath(index + "/_mapping", HttpMethod.GET, null);
        String[] types = getValuesFromResponse(response, index + ".mappings");
        ArrayList<String> fields = new ArrayList<>();
        for (String s : types) {
            IntStream.of(50).forEach(i -> fields.addAll(Arrays.asList(getValuesFromResponse(response, index + ".mappings." + s + ".properties"))));
        }
        Collections.sort(fields);
        return fields.toArray(new String[fields.size()]);
    }

    public boolean startReindexing(String source, String dest, Map<String, String> mappings) {
        Map<String, Object> requestBody = new HashMap<String, Object>() {
            {
                put("source", new HashMap<String, Object>() {
                    {
                        put("index", source);
                    }
                });
                put("dest", new HashMap<String, Object>() {
                    {
                        put("index", dest);
                    }
                });
                put("script", new HashMap<String, Object>() {
                    {
                        put("inline", generateInlineScript(mappings));
                    }
                });
            }
        };
        readPath("_reindex", HttpMethod.POST, requestBody);
        return true;
    }

    private String generateInlineScript(Map<String, String> mappings) {
        final String replaceTemplate = "ctx._source.NEW = ctx._source.remove(\"OLD\")";
        StringBuilder sb = new StringBuilder(100);
        mappings.keySet().stream().forEach((oldField) -> {
            String newField = mappings.get(oldField);
            if (!oldField.equals(newField)) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(replaceTemplate.replace("OLD", oldField).replace("NEW", newField));
            }
        });
        return sb.toString();
    }

    private String readPath(String path, String method, Map<String, Object> body) {
        WebTarget statsWebTarget = getCurrentWebTarget().path(path);
        Invocation.Builder invocationBuilder = statsWebTarget.request(MediaType.APPLICATION_JSON);
        Response response = null;
        if (method == null || HttpMethod.GET == method) {
            response = invocationBuilder.get();
        } else if (HttpMethod.POST == method) {
            try {
                String bodyString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body);
                LOG.info(bodyString);
                Entity<String> e = Entity.entity(bodyString, MediaType.APPLICATION_JSON);
                response = invocationBuilder.post(e);
            } catch (JsonProcessingException e) {
                LOG.severe(e.getMessage());
            }
        }
        return response.readEntity(String.class);
    }

    private String[] getValuesFromResponse(String response, String path) {
        String[] result = null;
        ObjectMapper om = new ObjectMapper();
        try {
            Map<String, Object> map = om.readValue(response, new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> current = map;
            for (String s : path.split("\\.")) {
                if (current.get(s) == null) {
                    current = new HashMap<>();
                    break;
                } else {
                    current = (Map<String, Object>) current.get(s);
                }

            }
            if (current == null) {
                result = new String[0];
            } else {
                result = current.keySet().stream().sorted().collect(Collectors.toList()).toArray(new String[current.size()]);
            }
        } catch (IOException e) {
            result = new String[0];
            LOG.severe(e.getMessage());
        }
        return result;
    }

    /*
     * GETTER & SETTER
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
