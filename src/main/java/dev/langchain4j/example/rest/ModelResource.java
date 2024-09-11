package dev.langchain4j.example.rest;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;


import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;


import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("model")
public class ModelResource {

    @Inject
    @ConfigProperty(name = "azure.api.key")
    private String AZURE_API_KEY;

    @Inject
    @ConfigProperty(name = "chat.model.deployent")
    private String CHAT_MODEL_DEPLOYMENT;

    @Inject
    @ConfigProperty(name = "chat.model.endpoint")
    private String CHAT_MODEL_ENDPOINT;

    private ChatLanguageModel chatLanguageModel = null;




    private ChatLanguageModel getLanguageModel() {
        if (chatLanguageModel == null) {
            chatLanguageModel = AzureOpenAiChatModel.builder()
                .apiKey(AZURE_API_KEY)
                .deploymentName(CHAT_MODEL_DEPLOYMENT)
                .endpoint(CHAT_MODEL_ENDPOINT)
                .build();
        }
        return chatLanguageModel;


    }
    

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("language")
    @Operation(
        summary = "Use the language model.",
        description = "Provide a sequence of words to a large language model.",
        operationId = "languageModelAsk" )
    public String languageModelAsk(@QueryParam("question") String question) {

        ChatLanguageModel model = getLanguageModel();

        String answer;
        try {
            answer = model.generate(question);
        } catch (Exception e) {
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chat")
    @Operation(
        summary = "Use the chat model.",
        description = "Assume you are talking with an agent that is knowledgeable about " +
                      "Large Language Models. Ask any question about it.",
        operationId = "chatModelAsk" )
    public List<String> chatModelAsk(@QueryParam("userMessage") String userMessage) {

        ChatLanguageModel model = AzureOpenAiChatModel.builder()
            .apiKey(AZURE_API_KEY)
            .deploymentName(CHAT_MODEL_DEPLOYMENT)
            .endpoint(CHAT_MODEL_ENDPOINT)
            .build();

        SystemMessage systemMessage =
            systemMessage("You are very knowledgeble about Large Language Models. Be friendly. Give concise answers.");

        AiMessage aiMessage = model.generate(
            systemMessage,
            userMessage(userMessage)
        ).content();

        return List.of(
            "System: " + systemMessage.text(),
            "Me:     " + userMessage,
            "Agent:  " + aiMessage.text().trim());

    }


}
