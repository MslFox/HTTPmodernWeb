package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Request {
    private boolean isValid = false;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private String path;
    private String method;
    private String body;
    private List<String> headers;
    private static final List<String> allowedMethods = List.of(GET, POST);

    public Request(BufferedInputStream in) {
        try {
            createRequest(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRequest(BufferedInputStream in) throws IOException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var readBytesValue_read = in.read(buffer);
        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, readBytesValue_read);
        if (requestLineEnd == -1) return;
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) return;
        method = requestLine[0];
        if (!allowedMethods.contains(method)) return;
        path = requestLine[1];
        if (!path.startsWith("/")) return;
        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, readBytesValue_read);
        if (headersEnd == -1) return;
        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);
        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        // для GET тела нет
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                body = new String(bodyBytes);
            }
        }
        isValid = true;
      }

    public String getMethod() {
        return isValid() ? method : null;
    }

    public String getPath() {
        return isValid() ? path.split("\\?")[0] : null;
    }

    public List<String> getHeaders() {
        return isValid() ? headers : null;
    }


    public String getQueryString() {
        final var parts = path.split("\\?");
        return isValid() && parts.length > 1 ? parts[1] : "";
    }


    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(getQueryString(), StandardCharsets.UTF_8);
    }

    public List<NameValuePair> getQueryParam(String name) {
        final var searchList = new ArrayList<NameValuePair>();
        getQueryParams().forEach(nameValuePair -> {
            if (nameValuePair.getName().equals(name)) searchList.add(nameValuePair);
        });
        return searchList;
    }

    public String getBody() {
        return isValid() ? body : null;
    }

    public List<NameValuePair> getBodyParams() {
        return URLEncodedUtils.parse(getBody(), StandardCharsets.UTF_8);
    }
    public List<NameValuePair> getBodyParam(String name) {
        final var searchList = new ArrayList<NameValuePair>();
        getBodyParams().forEach(nameValuePair -> {
            if (nameValuePair.getName().equals(name)) searchList.add(nameValuePair);
        });
        return searchList;
    }

    public List<NameValuePair> getPostParams() {
        var postParams = getQueryParams();
        postParams.addAll(getBodyParams());
        return postParams;
    }

    public List<NameValuePair> getPostParam(String name) {
        final var searchList = new ArrayList<NameValuePair>();
        getPostParams().forEach(nameValuePair -> {
            if (nameValuePair.getName().equals(name)) searchList.add(nameValuePair);
        });
        return searchList;
    }

    public boolean isValid() {
        return isValid;
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream().filter(o -> o.startsWith(header)).map(o -> o.substring(o.indexOf(" "))).map(String::trim).findFirst();
    }

}

