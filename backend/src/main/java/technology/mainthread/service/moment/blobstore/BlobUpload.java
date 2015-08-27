package technology.mainthread.service.moment.blobstore;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BlobUpload - receives Multi-part request and returns blob key in json
 */
public class BlobUpload extends HttpServlet {

    private final BlobstoreService blobstoreService;

    public BlobUpload() {
        this(BlobstoreServiceFactory.getBlobstoreService());
    }

    public BlobUpload(BlobstoreService blobstoreService) {
        this.blobstoreService = blobstoreService;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys = blobs.get("moment");

        if (blobKeys != null && !blobKeys.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");

            JSONObject json = new JSONObject();
            json.put("blob-key", blobKeys.get(0).getKeyString());

            PrintWriter out = res.getWriter();
            out.print(json.toString());
            out.flush();
            out.close();
        }
    }
}
