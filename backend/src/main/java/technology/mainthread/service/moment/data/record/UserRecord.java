package technology.mainthread.service.moment.data.record;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class UserRecord {

    @Id
    private Long id;

    @Index
    private User user;

    @Index
    private String googlePlusId;

    private String displayName;

    private String firstName;

    private String lastName;

    private String profileImageUrl;

    private List<String> devices = new ArrayList<>();

    private Date created;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UserRecord setUser(User user) {
        this.user = user;
        return this;
    }

    public String getGooglePlusId() {
        return googlePlusId;
    }

    public UserRecord setGooglePlusId(String googlePlusId) {
        this.googlePlusId = googlePlusId;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRecord setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserRecord setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public UserRecord setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserRecord setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public List<String> getDevices() {
        return devices;
    }

    public UserRecord addDevice(String device) {
        this.devices.add(device);
        return this;
    }

    public void removeDevice(String device) {
        this.devices.remove(device);
    }

    public Date getCreated() {
        return created;
    }

    public UserRecord setCreated(Date created) {
        this.created = created;
        return this;
    }
}
