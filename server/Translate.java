package io.international_chat.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;
import com.squareup.okhttp.*;

public class Translate {

    private static final String subscriptionKey = ""; //YOUR KEY

    // Add your location, also known as region. The default is global.
    // This is required if using a Cognitive Services resource.

    private static final String location = "francecentral";

    private static HttpUrl newUrl(String from, String to) {
        return new HttpUrl.Builder().scheme("https").host("api.cognitive.microsofttranslator.com").addPathSegment("/translate").addQueryParameter("api-version", "3.0").addQueryParameter("from", from).addQueryParameter("to", to)
                //.addQueryParameter("to", "it")
                .build();
    }

    // Instantiates the OkHttpClient.
    private final OkHttpClient client = new OkHttpClient();

    // This function performs a POST request.
    private String Post(String message, HttpUrl url) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                //"[{\"Text\": \"Hello World!\"}]");
                "[{\"Text\": \"" + message + "\"}]");
        Request request = new Request.Builder().url(url).post(body).addHeader("Ocp-Apim-Subscription-Key", subscriptionKey).addHeader("Ocp-Apim-Subscription-Region", location).addHeader("Content-type", "application/json").build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    // Structure-type  "json response" :
    // [{"translations": [{"text": "Hallo Welt!", "to": "de"}]}]
    // The following function returns the translated message from a "json response", which corresponds to the "text" attribute above
    private static String getMessage(String json_text) {
        JsonElement json = JsonParser.parseString(json_text);
        // To fix encoding issues.
        byte[] message = json.getAsJsonArray().get(0).getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().getBytes(StandardCharsets.UTF_8);
        return new String(message, StandardCharsets.ISO_8859_1);
    }

    public static String translate(String message, String from, String to) {
        if (from.equals(to)) {
            // Same language, so we don't translate
            return message;
        }
        try {
            Translate translateRequest = new Translate();
            HttpUrl url = newUrl(from, to);
            String response = translateRequest.Post(message, url);
            return getMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
            return message; //message not translated
        }
    }
}
