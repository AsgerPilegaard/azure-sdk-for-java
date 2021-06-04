// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 * These tests will not run in LIVE or RECORD as they cannot get the content url.
 */
public class DownloadContentAsyncTests extends CallingServerTestBase {
    private static final String METADATA_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata";
    private static final String VIDEO_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video";
    private static final String CONTENT_URL_404 = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadMetadataAsync(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataAsync");

        try {
            Flux<ByteBuffer> content = conversationAsyncClient.downloadStream(new URI(METADATA_URL));
            byte[] contentBytes = FluxUtil.collectBytesInByteBufferStream(content).block();
            assertThat(contentBytes, is(notNullValue()));
            String metadata = new String(contentBytes, StandardCharsets.UTF_8);
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadVideoAsync(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadVideoAsync");

        try {
            Response<Flux<ByteBuffer>> response = conversationAsyncClient.downloadStreamWithResponse(new URI(VIDEO_URL), null).block();
            assertThat(response, is(notNullValue()));
            byte[] contentBytes = FluxUtil.collectBytesInByteBufferStream(response.getValue()).block();
            assertThat(contentBytes, is(notNullValue()));
            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")), is(equalTo(contentBytes.length)));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContent404Async(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadContent404Async");
        Response<Flux<ByteBuffer>> response = conversationAsyncClient
                .downloadStreamWithResponse(new URI(CONTENT_URL_404), null).block();
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThrows(CallingServerResponseException.class,
            () -> FluxUtil.collectBytesInByteBufferStream(response.getValue()).block());
    }

    private ConversationAsyncClient setupAsyncClient(ConversationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected ConversationClientBuilder addLoggingPolicy(ConversationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

}
