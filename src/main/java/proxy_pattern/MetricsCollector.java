package proxy_pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsCollector {
    private Map<String, List<RequestInfo>> requestMetrics;

    public MetricsCollector() {
        this.requestMetrics = new HashMap<>();
    }

    public void recordRequest(RequestInfo requestInfo) {
        requestMetrics.putIfAbsent(requestInfo.getApiName(), new ArrayList<>());
        requestMetrics.get(requestInfo.getApiName()).add(requestInfo);
    }
}
