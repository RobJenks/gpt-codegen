package org.rj.modelgen.llm.audit;

import org.apache.commons.io.FileUtils;
import org.rj.modelgen.llm.session.SessionState;
import org.rj.modelgen.llm.state.ModelInterfaceState;
import org.rj.modelgen.llm.state.ModelInterfaceStateMachine;
import org.rj.modelgen.llm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModelInterfaceStateMachineAuditLog {
    private static final Logger LOG = LoggerFactory.getLogger(ModelInterfaceStateMachineAuditLog.class);
    private String recordLocation;

    public ModelInterfaceStateMachineAuditLog() {
        this.recordLocation = "audit";
    }

    public void recordAudit(ModelInterfaceStateMachine model, ModelInterfaceState state, String sessionId, String identifier, String content) {
        if (model == null || state == null || sessionId == null || identifier == null || content == null) {
            LOG.error("Cannot record audit log entry; invalid parameters provided");
            return;
        }

        final var filename = generateAuditRecordFilename(model, sessionId, identifier, content);
        try {
            FileUtils.writeStringToFile(new File(filename), content, Charset.defaultCharset());
        }
        catch (Exception ex) {
            LOG.error("Failed to write audit log entry to disk ({})", filename);
        }
    }

    private String generateAuditRecordFilename(ModelInterfaceStateMachine model, String sessionId, String identifier, String content) {
        return Path.of(
            recordLocation,
            String.format("%d-%s-%s-%s.%s",
                System.currentTimeMillis(),
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                sessionId,
                identifier,
                getAppropriateFileExtension(content))
        ).toString();
    }

    private String getAppropriateFileExtension(String content) {
        if (Util.tryParseJson(content).isOk()) {
            return "json";
        }

        return "txt";
    }


    public String getRecordLocation() {
        return recordLocation;
    }

    public void setRecordLocation(String recordLocation) {
        this.recordLocation = recordLocation;
    }

    public ModelInterfaceStateMachineAuditLog withRecordLocation(String recordLocation) {
        setRecordLocation(recordLocation);
        return this;
    }

}
