package technology.mainthread.service.moment.data.response;

import java.util.List;

import technology.mainthread.service.moment.data.MomentType;

public class MomentResponse {

    private long momentId;

    private long senderId;

    private String senderName;

    private List<String> recipientNames;

    private String servingUrl;

    private MomentType momentType;


    public long getMomentId() {
        return momentId;
    }

    public MomentResponse setMomentId(long momentId) {
        this.momentId = momentId;
        return this;
    }

    public long getSenderId() {
        return senderId;
    }

    public MomentResponse setSenderId(long senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getSenderName() {
        return senderName;
    }

    public MomentResponse setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public List<String> getRecipientNames() {
        return recipientNames;
    }

    public MomentResponse setRecipientNames(List<String> recipientNames) {
        this.recipientNames = recipientNames;
        return this;
    }

    public String getServingUrl() {
        return servingUrl;
    }

    public MomentResponse setServingUrl(String servingUrl) {
        this.servingUrl = servingUrl;
        return this;
    }

    public MomentType getMomentType() {
        return momentType;
    }

    public MomentResponse setMomentType(MomentType momentType) {
        this.momentType = momentType;
        return this;
    }
}
