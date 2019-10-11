package nextstep.mvc;

import nextstep.web.annotation.RequestMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;

public class HandlerKey {
    private PathPattern pathPattern;
    private RequestMethod requestMethod;

    public HandlerKey(PathPattern pathPattern, RequestMethod requestMethod) {
        this.pathPattern = pathPattern;
        this.requestMethod = requestMethod;
    }

    public boolean matchPattern(String path) {
        return pathPattern.matches(PathContainer.parsePath(path));
    }

    @Override
    public String toString() {
        return "HandlerKey [url=" + pathPattern + ", requestMethod=" + requestMethod + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((requestMethod == null) ? 0 : requestMethod.hashCode());
        result = prime * result + ((pathPattern == null) ? 0 : pathPattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HandlerKey other = (HandlerKey) obj;
        if (requestMethod != other.requestMethod)
            return false;
        if (pathPattern == null) {
            return other.pathPattern == null;
        } else return pathPattern.equals(other.pathPattern);
    }
}
