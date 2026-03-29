package br.com.mechanic.account.service.request;

import br.com.mechanic.account.constant.TopicCreateRequestJsonConstants;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Atualização parcial: apenas propriedades presentes no JSON são consideradas (via setters Jackson).
 * {@link br.com.mechanic.account.service.topic.TopicService#update} aplica regras por tipo de tópico.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicUpdateRequest {

    private String title;
    private String context;
    private AccountProfileTypeEnum profileType;
    private LocalDateTime endDate;

    private boolean titlePresent;
    private boolean contextPresent;
    private boolean profileTypePresent;
    private boolean endDatePresent;

    public TopicUpdateRequest() {
    }

    @JsonProperty(TopicCreateRequestJsonConstants.TITLE)
    public void setTitle(String title) {
        this.title = title;
        this.titlePresent = true;
    }

    @JsonProperty(TopicCreateRequestJsonConstants.CONTEXT)
    public void setContext(String context) {
        this.context = context;
        this.contextPresent = true;
    }

    @JsonProperty(TopicCreateRequestJsonConstants.PROFILE_TYPE)
    public void setProfileType(AccountProfileTypeEnum profileType) {
        this.profileType = profileType;
        this.profileTypePresent = true;
    }

    @JsonProperty(TopicCreateRequestJsonConstants.END_DATE)
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        this.endDatePresent = true;
    }

    public String getTitle() {
        return title;
    }

    public String getContext() {
        return context;
    }

    public AccountProfileTypeEnum getProfileType() {
        return profileType;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public boolean isContextPresent() {
        return contextPresent;
    }

    public boolean isProfileTypePresent() {
        return profileTypePresent;
    }

    public boolean isEndDatePresent() {
        return endDatePresent;
    }
}
