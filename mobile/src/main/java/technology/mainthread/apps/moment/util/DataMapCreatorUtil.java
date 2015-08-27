package technology.mainthread.apps.moment.util;

import android.graphics.Bitmap;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.ByteArrayOutputStream;

import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;

public class DataMapCreatorUtil {

    public static PutDataMapRequest createNewMomentDataMap(MomentResponse moment, Bitmap drawing) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        drawing.compress(Bitmap.CompressFormat.PNG, 0 /*Ignored*/, byteStream);
        Asset asset = Asset.createFromBytes(byteStream.toByteArray());

        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PATH_MOMENT_NOTIFICATION);
        dataMap.getDataMap().putLong(Constants.KEY_MOMENT_ID, moment.getMomentId());
        dataMap.getDataMap().putLong(Constants.KEY_SENDER_ID, moment.getSenderId());
        dataMap.getDataMap().putString(Constants.KEY_SENDER_NAME, moment.getSenderName());
        dataMap.getDataMap().putAsset(Constants.KEY_DRAWING, asset);
        dataMap.getDataMap().putLong(Constants.KEY_FORCE_UPDATE, System.currentTimeMillis());
        return dataMap;
    }

    public static PutDataMapRequest createFriendAddedBackDataMap(long friendId, String name) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PATH_FRIEND_ADDED_BACK);
        dataMap.getDataMap().putLong(Constants.KEY_SENDER_ID, friendId);
        dataMap.getDataMap().putString(Constants.KEY_SENDER_NAME, name);
        dataMap.getDataMap().putLong(Constants.KEY_FORCE_UPDATE, System.currentTimeMillis());
        return dataMap;
    }

}
