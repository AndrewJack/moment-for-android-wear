package technology.mainthread.service.moment.data.response;

public class UserRegisteredResponse {

    private long id;

    private String status;

    public long getId() {
        return id;
    }

    public UserRegisteredResponse setId(long id) {
        this.id = id;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRegisteredResponse setStatus(String status) {
        this.status = status;
        return this;
    }
}
