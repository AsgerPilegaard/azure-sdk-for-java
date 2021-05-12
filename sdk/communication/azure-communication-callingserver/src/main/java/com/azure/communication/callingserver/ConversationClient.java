// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.JoinCallResponse;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import reactor.core.publisher.Flux;

import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Sync Client that supports server call operations.
 */
@ServiceClient(builder = CallClientBuilder.class)
public final class ConversationClient {
    private final ConversationAsyncClient conversationAsyncClient;

    ConversationClient(ConversationAsyncClient conversationAsyncClient) {
        this.conversationAsyncClient = conversationAsyncClient;
    }

    /**
     * Join a call
     *
     * @param conversationId The conversation id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @return JoinCallResponse for a successful JoinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JoinCallResponse joinCall(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        return conversationAsyncClient.joinCall(conversationId, source, joinCallOptions).block();
    }

    /**
     * Join a call
     *
     * @param conversationId The conversation id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful JoinCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JoinCallResponse> joinCallWithResponse(String conversationId, CommunicationIdentifier source, JoinCallOptions joinCallOptions, Context context) {
        return conversationAsyncClient.joinCallWithResponse(conversationId, source, joinCallOptions, context).block();
    }

    /**
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void addParticipant(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri) {
        return conversationAsyncClient.addParticipant(conversationId, participant, alternateCallerId, operationContext, callBackUri).block();
    }

    /**
     * Invite participants to a Conversation.
     *
     * @param conversationId The conversation id.
     * @param participant Invited participant.
     * @param alternateCallerId alternateCallerId of Invited participant.
     * @param operationContext operationContext.
     * @param callBackUri callBackUri to get notifications.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(String conversationId, CommunicationIdentifier participant, String alternateCallerId, String operationContext, String callBackUri, Context context) {
        return conversationAsyncClient.addParticipantWithResponse(conversationId, participant, alternateCallerId, operationContext, callBackUri, context).block();
    }

    /**
     * Remove participant from the Conversation.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(String conversationId, String participantId) {
        return conversationAsyncClient.removeParticipant(conversationId, participantId).block();
    }

    /**
     * Remove participant from the Conversation.
     *
     * @param conversationId The conversation id.
     * @param participantId Participant id.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(String conversationId, String participantId, Context context) {
        return conversationAsyncClient.removeParticipantWithResponse(conversationId, participantId, context).block();
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResponse startRecording(String conversationId, String recordingStateCallbackUri) {
        return conversationAsyncClient.startRecording(conversationId, recordingStateCallbackUri).block();
    }

    /**
     * Start recording
     *
     * @param conversationId The conversation id.
     * @param recordingStateCallbackUri The uri to send state change callbacks.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful startRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResponse> startRecordingWithResponse(String conversationId,
            String recordingStateCallbackUri, Context context) {
        return conversationAsyncClient.startRecordingWithResponse(conversationId, recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void stopRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.stopRecording(conversationId, recordingId).block();
    }

    /**
     * Stop recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful stopRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.stopRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void pauseRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.pauseRecording(conversationId, recordingId).block();
    }

    /**
     * Pause recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful pauseRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.pauseRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void resumeRecording(String conversationId, String recordingId) {
        return conversationAsyncClient.resumeRecording(conversationId, recordingId).block();
    }

    /**
     * Resume recording
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful resumeRecording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String conversationId, String recordingId, Context context) {
        return conversationAsyncClient.resumeRecordingWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingStateResponse getRecordingState(String conversationId, String recordingId) {
        return conversationAsyncClient.getRecordingState(conversationId, recordingId).block();
    }

    /**
     * Get recording state
     *
     * @param conversationId The conversation id.
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful getRecordingState request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingStateResponse> getRecordingStateWithResponse(String conversationId,
            String recordingId, Context context) {
        return conversationAsyncClient.getRecordingStateWithResponse(conversationId, recordingId, context).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResponse playAudio(String conversationId, String audioFileUri, String audioFileId, String callbackUri, String operationContext) {

        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(false).setAudioFileId(audioFileId).setCallbackUri(callbackUri).setOperationContext(operationContext);
        return conversationAsyncClient.playAudio(conversationId, playAudioRequest).block();
    }

    /**
     * Play audio in a call.
     *
     * @param conversationId The conversation id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri The callback Uri to receive PlayAudio status notifications.
     * @param operationContext The value to identify context of the operation.
     * @param context A {@link Context} representing the request context.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResponse> playAudioWithResponse(String conversationId, String audioFileUri, String audioFileId, String callbackUri, String operationContext, Context context) {

        //Currently we do not support loop on the audio media for out-call, thus setting the loop to false
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(false).setAudioFileId(audioFileId).setCallbackUri(callbackUri).setOperationContext(operationContext);
        return conversationAsyncClient.playAudioWithResponse(conversationId, playAudioRequest, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it into the {@link OutputStream} passed as parameter.
     * @param output - A stream where to write the downloaded content.
     * @param endpoint - ACS URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *               the whole content will be downloaded.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(OutputStream output, URI endpoint, HttpRange range,
                           ParallelDownloadOptions parallelDownloadOptions) {
        conversationAsyncClient.downloadTo(output, endpoint, range, parallelDownloadOptions).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param output - A stream where to write the downloaded content.
     * @param endpoint - ACS URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *              the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(OutputStream output, URI endpoint, HttpRange range,
                           ParallelDownloadOptions parallelDownloadOptions, Context context) {
        return conversationAsyncClient.downloadToWithResponse(output, endpoint, range, parallelDownloadOptions, context)
            .block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param path - File location.
     * @param endpoint - ACS URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(Path path, URI endpoint, HttpRange range,
                           ParallelDownloadOptions parallelDownloadOptions,
                           boolean overwrite) {
        conversationAsyncClient.downloadTo(path, endpoint, range,
            parallelDownloadOptions, overwrite).block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param path - File location.
     * @param endpoint - ACS URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(Path path, URI endpoint, HttpRange range,
                                                 ParallelDownloadOptions parallelDownloadOptions,
                                                 boolean overwrite, Context context) {
        return conversationAsyncClient.downloadToWithResponse(path, endpoint, range,
            parallelDownloadOptions, overwrite, context).block();
    }

    /**
     * Download the content located in {@code endpoint}, e.g. Recording video, from the ACS endpoint passed as
     * parameter.
     * @param endpoint - ACS URL where the content is located.
     * @return The byte stream of the requested content.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Flux<ByteBuffer> downloadStreaming(URI endpoint) {
        return conversationAsyncClient.downloadStreaming(endpoint).block();
    }

    /**
     * Download the content located in {@code endpoint}, e.g. Recording video, from the ACS endpoint passed as
     * parameter.
     * @param endpoint - ACS URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Flux<ByteBuffer>> downloadStreamingWithResponse(URI endpoint, HttpRange range, Context context) {
        return conversationAsyncClient.downloadStreamingWithResponse(endpoint, range, context).block();
    }
}
