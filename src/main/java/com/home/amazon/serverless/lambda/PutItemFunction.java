package com.home.amazon.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.amazon.serverless.model.Book;
import com.home.amazon.serverless.utils.DependencyFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Collections;
import java.util.Map;

public class PutItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static final int STATUS_CODE_NO_CONTENT = 204;
    static final int STATUS_CODE_CREATED = 201;
    static final int STATUS_CODE_BAD_REQUEST = 400;
    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<Book> bookTableSchema;

    public PutItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        bookTableSchema = TableSchema.fromBean(Book.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String body = request.getBody();
        int statusCode = STATUS_CODE_NO_CONTENT;
        if (body != null && !body.isEmpty()) {
            Book item;
            try {
                item = new ObjectMapper().readValue(body, Book.class);
                if (item != null) {
                    Map<String, String> pathParameters = request.getPathParameters();
                    if (arePathParametersValid(pathParameters, item)) {
                        DynamoDbTable<Book> booksTable = dbClient.table(tableName, bookTableSchema);
                        booksTable.putItem(item);
                        statusCode = STATUS_CODE_CREATED;
                    } else {
                        statusCode = STATUS_CODE_BAD_REQUEST;
                    }
                }
            } catch (JsonProcessingException e) {
                context.getLogger().log("Failed to deserialize JSON: " + e);
            }

        }
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.emptyMap());
    }

    private boolean arePathParametersValid(Map<String, String> pathParameters, Book item) {
        if (pathParameters == null) {
            return false;
        }
        String itemPartitionKey = pathParameters.get(Book.PARTITION_KEY);
        if (itemPartitionKey == null || itemPartitionKey.isEmpty()) {
            return false;
        }
        return itemPartitionKey.equals(item.getIsbn());
    }

}