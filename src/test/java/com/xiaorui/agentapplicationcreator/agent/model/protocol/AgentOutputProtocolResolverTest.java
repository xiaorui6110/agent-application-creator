package com.xiaorui.agentapplicationcreator.agent.model.protocol;

import com.xiaorui.agentapplicationcreator.agent.model.response.AgentResponse;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentOutputProtocolResolverTest {

    private final AgentOutputProtocolResolver resolver = new AgentOutputProtocolResolver();

    @Test
    void shouldInferLegacyCodeGenerationResponse() {
        String rawResponse = """
                {
                  "reply": "我已经生成了页面代码。",
                  "codeGenType": "MULTI_FILE",
                  "structuredReply": {
                    "runnable": true,
                    "files": {
                      "index.html": "<html></html>"
                    },
                    "description": "demo"
                  }
                }
                """;

        AgentResponse agentResponse = resolver.parse(rawResponse);

        assertEquals("CODE_GENERATION", agentResponse.getResponseType());
        assertEquals("multi_file", agentResponse.getCodeGenType());
        assertEquals("multi_file", agentResponse.getStructuredReply().getGenerationMode());
        assertEquals("index.html", agentResponse.getStructuredReply().getEntry());
    }

    @Test
    void shouldRejectMixedStructuredReplyAndCodeModificationPlan() {
        String rawResponse = """
                {
                  "responseType": "CODE_GENERATION",
                  "reply": "我已经生成了页面代码。",
                  "codeGenType": "single_file",
                  "structuredReply": {
                    "generationMode": "single_file",
                    "entry": "index.html",
                    "files": {
                      "index.html": "<html></html>"
                    }
                  },
                  "codeModificationPlan": {
                    "planType": "CODE_MODIFICATION",
                    "rootDir": "code_output"
                  }
                }
                """;

        assertThrows(BusinessException.class, () -> resolver.parse(rawResponse));
    }

    @Test
    void shouldRejectInvalidCodeGenType() {
        String rawResponse = """
                {
                  "responseType": "CODE_GENERATION",
                  "reply": "我已经生成了页面代码。",
                  "codeGenType": "react_app",
                  "structuredReply": {
                    "generationMode": "react_app",
                    "entry": "index.html",
                    "files": {
                      "index.html": "<html></html>"
                    }
                  }
                }
                """;

        assertThrows(BusinessException.class, () -> resolver.parse(rawResponse));
    }
}
