package technology.mainthread.service.moment.data.record;

import com.google.appengine.api.blobstore.BlobKey;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.List;

import technology.mainthread.service.moment.data.MomentType;

@Entity
public class MomentRecord {

    @Id
    private Long id;

    @Index
    private Long senderId;

    private String senderName;

    @Index
    private MomentType type;

    private BlobKey blobKey;

    @Index
    private List<Long> recipients;

    @Index
    private Date created;

    public Long getId() {
        return id;
    }

    public long getSenderId() {
        return senderId;
    }

    public MomentRecord setSenderId(long senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getSenderName() {
        return senderName;
    }

    public MomentRecord setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public MomentType getType() {
        return type;
    }

    public MomentRecord setType(MomentType type) {
        this.type = type;
        return this;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public MomentRecord setBlobKey(BlobKey blobKey) {
        this.blobKey = blobKey;
        return this;
    }

    public List<Long> getRecipients() {
        return recipients;
    }

    public MomentRecord setRecipients(List<Long> recipients) {
        this.recipients = recipients;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public MomentRecord setCreated(Date created) {
        this.created = created;
        return this;
    }
}
