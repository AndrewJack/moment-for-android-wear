package technology.mainthread.apps.moment.data.bus;

public class SignInStateEvent {

    private final boolean signedIn;

    public SignInStateEvent(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public boolean isSignedIn() {
        return signedIn;
    }
}
