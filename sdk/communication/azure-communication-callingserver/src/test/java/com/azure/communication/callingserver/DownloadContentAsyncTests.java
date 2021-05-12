// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            conversationAsyncClient.downloadTo(baos, new URI(METADATA_URL), null, null).block();
            String metadata = baos.toString(StandardCharsets.UTF_8);
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadMetadataStreamingAsync(HttpClient httpClient) throws URISyntaxException {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataStreamingAsync");

        try {
            Flux<ByteBuffer> stream = conversationAsyncClient.downloadStreaming(new URI(METADATA_URL)).block();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertThat(stream, is(notNullValue()));
            stream.subscribe(byteBuffer -> {
                try {
                    baos.write(byteBuffer.array());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            String metadata = baos.toString(StandardCharsets.UTF_8);
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Response<Void> response = conversationAsyncClient.downloadToWithResponse(baos, new URI(VIDEO_URL), null, null).block();
            assertThat(response, is(notNullValue()));
            assertThat(response.getHeaders().getValue("Content-Type"), is(equalTo("application/octet-stream")));
            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")), is(equalTo(baos.size())));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContent404Async(HttpClient httpClient) {
        ConversationClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        ConversationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadContent404Async");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommunicationErrorException ex = assertThrows(CommunicationErrorException.class,
            () -> conversationAsyncClient
                .downloadTo(baos, new URI(CONTENT_URL_404), null, null).block());
        assertThat(ex.getResponse().getStatusCode(), is(equalTo(404)));
    }

    private ConversationAsyncClient setupAsyncClient(ConversationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected ConversationClientBuilder addLoggingPolicy(ConversationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

}
