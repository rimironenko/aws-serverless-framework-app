package com.home.amazon.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.amazon.serverless.model.Book;
import com.home.amazon.serverless.utils.DependencyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PutItemFunctionTest {

    private static final String TEST_TABLE_NAME = "TestTable";
    private static final String TEST_PARTITION_KEY_VALUE = "123";

    @Mock
    private DynamoDbEnhancedClient client;

    @Mock
    private DynamoDbTable<Book> table;

    @Mock
    private APIGatewayProxyRequestEvent request;

    @Mock
    private Context context;


    @Test
    public void shouldPutItemIfBodyIsValid() throws JsonProcessingException {
        Book testBook = new Book();
        testBook.setIsbn(TEST_PARTITION_KEY_VALUE);
        when(client.table(eq(TEST_TABLE_NAME), any(TableSchema.class))).thenReturn(table);
        when(request.getBody()).thenReturn(new ObjectMapper().writeValueAsString(testBook));
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put(Book.PARTITION_KEY, TEST_PARTITION_KEY_VALUE);
        when(request.getPathParameters()).thenReturn(pathParameters);

        try (MockedStatic<DependencyFactory> dependencyFactoryMockedStatic = mockStatic(DependencyFactory.class)) {
            dependencyFactoryMockedStatic.when(DependencyFactory::dynamoDbEnhancedClient).thenReturn(client);
            dependencyFactoryMockedStatic.when(DependencyFactory::tableName).thenReturn(TEST_TABLE_NAME);
            PutItemFunction handler = new PutItemFunction();
            APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
            verify(table).putItem(eq(testBook));
            assertEquals(PutItemFunction.STATUS_CODE_CREATED, response.getStatusCode());
        }

    }

    @Test
    public void shouldNotPutItemIfIsbnIsDifferent() throws JsonProcessingException {
        Book testBook = new Book();
        testBook.setIsbn(TEST_PARTITION_KEY_VALUE);
        when(request.getBody()).thenReturn(new ObjectMapper().writeValueAsString(testBook));
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put(Book.PARTITION_KEY, "1234");
        when(request.getPathParameters()).thenReturn(pathParameters);

        try (MockedStatic<DependencyFactory> dependencyFactoryMockedStatic = mockStatic(DependencyFactory.class)) {
            dependencyFactoryMockedStatic.when(DependencyFactory::dynamoDbEnhancedClient).thenReturn(client);
            dependencyFactoryMockedStatic.when(DependencyFactory::tableName).thenReturn(TEST_TABLE_NAME);
            PutItemFunction handler = new PutItemFunction();
            APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
            verifyNoInteractions(table);
            assertEquals(PutItemFunction.STATUS_CODE_BAD_REQUEST, response.getStatusCode());
        }

    }

    @Test
    public void shouldNotPutItemIfBodyIsNotValid() {
        when(request.getBody()).thenReturn("");

        try (MockedStatic<DependencyFactory> dependencyFactoryMockedStatic = mockStatic(DependencyFactory.class)) {
            dependencyFactoryMockedStatic.when(DependencyFactory::dynamoDbEnhancedClient).thenReturn(client);
            dependencyFactoryMockedStatic.when(DependencyFactory::tableName).thenReturn(TEST_TABLE_NAME);
            PutItemFunction handler = new PutItemFunction();
            APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
            verifyNoInteractions(table);
            assertEquals(PutItemFunction.STATUS_CODE_NO_CONTENT, response.getStatusCode());
        }
    }

    @Test
    public void shouldNotPutItemIfBodyIsMissed() {
        try (MockedStatic<DependencyFactory> dependencyFactoryMockedStatic = mockStatic(DependencyFactory.class)) {
            dependencyFactoryMockedStatic.when(DependencyFactory::dynamoDbEnhancedClient).thenReturn(client);
            dependencyFactoryMockedStatic.when(DependencyFactory::tableName).thenReturn(TEST_TABLE_NAME);
            PutItemFunction handler = new PutItemFunction();
            APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
            verifyNoInteractions(table);
            assertEquals(PutItemFunction.STATUS_CODE_NO_CONTENT, response.getStatusCode());
        }
    }

}