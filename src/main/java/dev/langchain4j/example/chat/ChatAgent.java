package dev.langchain4j.example.chat;
// import org.eclipse.microprofile.config.inject.ConfigProperty;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatAgent {

    // @Inject
    // @ConfigProperty(name = "azure.api.key")
    // private String AZURE_API_KEY;

    // @Inject
    // @ConfigProperty(name = "chat.model.deployent")
    // private String CHAT_MODEL_DEPLOYMENT;

    // @Inject
    // @ConfigProperty(name = "chat.model.endpoint")
    // private String CHAT_MODEL_ENDPOINT;

    // @Inject
    // @ConfigProperty(name = "chat.memory.max.messages")
    // private Integer MAX_MESSAGES;

    interface Assistant {
       String chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }

    private Assistant assistant = null;



    public Assistant getAssistant() {
        if (assistant == null) {
            AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_API_KEY"))
                .deploymentName(System.getenv("AZURE_DEPLOYMENT_NAME"))
                .endpoint(System.getenv("AZURE_ENDPOINT"))
                .build();
            assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(
                    sessionId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
        }
        return assistant;
    }

    public String chat(String sessionId, String message) {
        return getAssistant().chat(sessionId, message).trim();
    }

}
