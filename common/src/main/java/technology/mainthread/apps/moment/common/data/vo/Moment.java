package technology.mainthread.apps.moment.common.data.vo;

import java.util.List;

public class Moment {

    private final int id;
    private final List<Long> recipients;
    private final String fileName;
    private final MomentType momentType;
    private final int retries;

    private Moment(Builder builder) {
        id = builder.id;
        recipients = builder.recipients;
        fileName = builder.fileName;
        momentType = builder.momentType;
        retries = builder.retries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Moment copy) {
        Builder builder = new Builder();
        builder.id = copy.id;
        builder.recipients = copy.recipients;
        builder.fileName = copy.fileName;
        builder.momentType = copy.momentType;
        builder.retries = copy.retries;
        return builder;
    }

    public int getId() {
        return id;
    }

    public List<Long> getRecipients() {
        return recipients;
    }

    public String getFileName() {
        return fileName;
    }

    public MomentType getMomentType() {
        return momentType;
    }

    public int getRetries() {
        return retries;
    }

    public static final class Builder {
        private int id;
        private List<Long> recipients;
        private String fileName;
        private MomentType momentType;
        private int retries;

        private Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder recipients(List<Long> recipients) {
            this.recipients = recipients;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder momentType(MomentType momentType) {
            this.momentType = momentType;
            return this;
        }

        public Builder retries(int retries) {
            this.retries = retries;
            return this;
        }

        public Moment build() {
            return new Moment(this);
        }
    }
}
