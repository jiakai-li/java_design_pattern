package proxy_pattern;

public class RequestInfo {
    String apiName;
    long timestamp;
    long responseTime;

    public RequestInfo(String apiName, long timestamp, long responseTime) {
        this.apiName = apiName;
        this.timestamp = timestamp;
        this.responseTime = responseTime;
    }

    public String getApiName() {
        return apiName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getResponseTime() {
        return responseTime;
    }
}
