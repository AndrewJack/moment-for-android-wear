package technology.mainthread.service.moment.fake;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.InputSettings;
import com.google.appengine.api.images.OutputSettings;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;

import java.util.Collection;
import java.util.concurrent.Future;

public class FakeImagesService implements ImagesService {

    public static final String SERVING_URL = "servingUrl";

    public FakeImagesService() {
    }

    @Override
    public Image applyTransform(Transform transform, Image image) {
        return null;
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image) {
        return null;
    }

    @Override
    public Image applyTransform(Transform transform, Image image, OutputEncoding outputEncoding) {
        return null;
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputEncoding outputEncoding) {
        return null;
    }

    @Override
    public Image applyTransform(Transform transform, Image image, OutputSettings outputSettings) {
        return null;
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputSettings outputSettings) {
        return null;
    }

    @Override
    public Image applyTransform(Transform transform, Image image, InputSettings inputSettings, OutputSettings outputSettings) {
        return null;
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, InputSettings inputSettings, OutputSettings outputSettings) {
        return null;
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l) {
        return null;
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l, OutputEncoding outputEncoding) {
        return null;
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l, OutputSettings outputSettings) {
        return null;
    }

    @Override
    public int[][] histogram(Image image) {
        return new int[0][];
    }

    @Override
    public String getServingUrl(BlobKey blobKey) {
        return SERVING_URL;
    }

    @Override
    public String getServingUrl(BlobKey blobKey, boolean b) {
        return SERVING_URL;
    }

    @Override
    public String getServingUrl(BlobKey blobKey, int i, boolean b) {
        return SERVING_URL;
    }

    @Override
    public String getServingUrl(BlobKey blobKey, int i, boolean b, boolean b2) {
        return SERVING_URL;
    }

    @Override
    public String getServingUrl(ServingUrlOptions servingUrlOptions) {
        return SERVING_URL;
    }

    @Override
    public void deleteServingUrl(BlobKey blobKey) {

    }
}
