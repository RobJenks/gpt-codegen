package org.rj.codegen.codegenservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String serializeOrThrow(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        }
        catch (Exception ex) {
            final var error = String.format("Failed to serialize object (%s)", ex.getMessage());

            LOG.error(error, ex);
            throw new RuntimeException(error, ex);
        }
    }

    public static int estimateTokenSize(String string) {
        if (StringUtils.isBlank(string)) return 0;

        return StringUtils.countMatches(string, ' ') + 1;
    }


}
