package technology.mainthread.apps.moment.util;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;

public class CredentialUtil {

    public static AbstractGoogleJsonClient updateCredential(AbstractGoogleJsonClient.Builder builder, String accountName) {
        GoogleAccountCredential credential = (GoogleAccountCredential) builder.getHttpRequestInitializer();
        String selectedAccountName = credential.getSelectedAccountName();
        if (selectedAccountName == null || !selectedAccountName.equals(accountName)) {
            credential.setSelectedAccountName(accountName);
            builder.setHttpRequestInitializer(credential);
        }
        return builder.build();
    }

}
