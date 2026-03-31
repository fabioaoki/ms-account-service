package br.com.mechanic.account.controller.annotator;

import br.com.mechanic.account.constant.AnnotatorLinkJsonConstants;
import br.com.mechanic.account.constant.ApiPathConstants;
import br.com.mechanic.account.constant.OpenApiOperationDocumentationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.service.annotator.AnnotatorLinkServiceBO;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkCreateRequest;
import br.com.mechanic.account.service.request.TopicAnnotatorLinkResumeUpdateRequest;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkAnnotatorListItemResponse;
import br.com.mechanic.account.service.response.TopicAnnotatorLinkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.ACCOUNTS_BASE_PATH)
@Tag(
        name = OpenApiOperationDocumentationConstants.Tag.ANNOTATORS_NAME,
        description = OpenApiOperationDocumentationConstants.Tag.ANNOTATORS_DESCRIPTION
)
public class AnnotatorController {

    private final AnnotatorLinkServiceBO annotatorLinkServiceBO;

    @Operation(
            summary = OpenApiOperationDocumentationConstants.Annotator.LIST_LINKS_SUMMARY,
            description = OpenApiOperationDocumentationConstants.Annotator.LIST_LINKS_DESCRIPTION
    )
    @GetMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE + ApiPathConstants.TOPIC_ANNOTATOR_LINKS_SEGMENT
    )
    public ResponseEntity<List<TopicAnnotatorLinkAnnotatorListItemResponse>> listTopicAnnotatorLinksForAnnotator(
            @PathVariable Long accountId,
            @RequestParam(name = AnnotatorLinkJsonConstants.TOPIC_ID, required = false) Long topicId,
            @RequestParam(name = AnnotatorLinkJsonConstants.TOPIC_OWNER_ACCOUNT_ID, required = false)
            Long topicOwnerAccountId,
            @RequestParam(name = AnnotatorLinkJsonConstants.TOPIC_STATUS, required = false)
            TopicStatusEnum topicStatus
    ) {
        return ResponseEntity.ok(annotatorLinkServiceBO.listLinksForAnnotatorView(
                accountId,
                topicId,
                topicOwnerAccountId,
                topicStatus
        ));
    }

    @Operation(
            summary = OpenApiOperationDocumentationConstants.Annotator.CREATE_LINK_SUMMARY,
            description = OpenApiOperationDocumentationConstants.Annotator.CREATE_LINK_DESCRIPTION
    )
    @PostMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
                    + ApiPathConstants.ANNOTATOR_LINK_SEGMENT
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TopicAnnotatorLinkResponse> createTopicAnnotatorLink(
            @PathVariable Long accountId,
            @PathVariable Long topicId,
            @Valid @RequestBody TopicAnnotatorLinkCreateRequest request
    ) {
        TopicAnnotatorLinkResponse body = annotatorLinkServiceBO.createLink(accountId, topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = OpenApiOperationDocumentationConstants.Annotator.UPDATE_RESUME_SUMMARY)
    @PutMapping(
            ApiPathConstants.ACCOUNT_ID_PATH_VARIABLE
                    + ApiPathConstants.TOPICS_SEGMENT
                    + ApiPathConstants.TOPIC_ID_PATH_VARIABLE
                    + ApiPathConstants.ANNOTATOR_LINK_SEGMENT
                    + ApiPathConstants.ANNOTATOR_LINK_RESUME_SEGMENT
    )
    public ResponseEntity<TopicAnnotatorLinkResponse> updateTopicAnnotatorLinkResume(
            @PathVariable Long accountId,
            @PathVariable Long topicId,
            @Valid @RequestBody TopicAnnotatorLinkResumeUpdateRequest request
    ) {
        TopicAnnotatorLinkResponse body = annotatorLinkServiceBO.updateLinkResume(accountId, topicId, request);
        return ResponseEntity.ok(body);
    }
}
