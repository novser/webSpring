import org.apache.http.client.utils.URLEncodedUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Request {
    private Method method;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, List<String>> params = new HashMap<>();
    private Map<String, List<String>> queryParams = new HashMap<>();
    private String body = null;
    private String queryString = null;


    public Request() {
    }

    public Request(String method, String body, String queryString) {
        setMethod(method);
        setBody(body);
        setQueryString(queryString);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(String name) {
        if ("GET".equals(name)) {
            method = Method.GET;
        } else if ("POST".equals(name)) {
            method = Method.POST;
        }
    }

    public void setParam(String name, String value) {
        List<String> parameters = params.computeIfAbsent(name, k -> new ArrayList<>());
        parameters.add(value);
    }

    public void setQueryParam(String name, String value) {
        List<String> parameters = queryParams.computeIfAbsent(name, k -> new ArrayList<>());
        parameters.add(value);
    }

    public void setHeader (String name, String value) {
        headers.put(name, value);
    }

    public void setBody(String body) {
        String[] parts = body.split("\r\n\r\n");
        if (parts.length > 1) {
            this.body = parts[1];
        }
        parsingBodyParam();
        parsingHeaders(parts[0]);
    }

    private void  parsingBodyParam() {
        if (body != null) {
            Map<String, String> result = Arrays.stream(body.split("\r\n"))
                    .map(element -> element.split(":"))
                    .collect(Collectors.toMap(element -> element[0], element -> element[1]));

            result.forEach(this::setParam);
        }
    }
    private void parsingHeaders(String str) {
        str.replace("HTTP/1.1", "");
        Map<String, String> result = Arrays.stream(body.split("\r\n"))
                .map(element -> element.split(":"))
                .collect(Collectors.toMap(element -> element[0], element -> element[1]));

        result.forEach(this::setHeader);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
//        queryString = queryString.replace("?", " DELIMITER ");
//        String[] parts = queryString.split("DELIMITER");

        URLEncodedUtils.parse(queryString);


//        if (parts.length > 1) {
//            parsingQueryBodyParam(parts[1]);
//        }
    }

//    private void parsingQueryBodyParam(String str) {
//        Map<String, String> result = Arrays.stream(body.split("&"))
//                .map(element -> element.split(":"))
//                .collect(Collectors.toMap(element -> element[0], element -> element[1]));
//
//        result.forEach(this::setQueryParam);
//    }


}
