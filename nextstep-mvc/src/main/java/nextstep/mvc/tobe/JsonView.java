package nextstep.mvc.tobe;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.web.support.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

public class JsonView implements View {

    private final ObjectMapper mapper;

    public JsonView() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        PrintWriter writer = response.getWriter();
        if (model.isEmpty()) {
            writer.print("");
            return;
        }
        if (model.size() == 1) {
            writer.print(tryWriteValueAsString(model.values().toArray()[0]));
            return;
        }
        writer.print(tryWriteValueAsString(model));
    }

    private String tryWriteValueAsString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
