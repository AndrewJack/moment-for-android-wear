package technology.mainthread.apps.moment.common.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Friend;

public class SyncFriendTable implements FriendsTable {

    private static final String COLUMN_INDEX = "indexColumn";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FRIEND_ID = "friendId";
    private static final String COLUMN_DISPLAY_NAME = "displayName";
    private static final String COLUMN_PROFILE_IMAGE_URL = "profileImageUrl";

    private static final String[] PROJECTION = new String[]{
            COLUMN_INDEX,
            COLUMN_ID,
            COLUMN_FRIEND_ID,
            COLUMN_DISPLAY_NAME,
            COLUMN_PROFILE_IMAGE_URL
    };

    public static String getCreateTableQuery(String tableName) {
        return "CREATE TABLE " + tableName + " ("
                + COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ID + " INTEGER, "
                + COLUMN_FRIEND_ID + " INTEGER, "
                + COLUMN_DISPLAY_NAME + " TEXT, "
                + COLUMN_PROFILE_IMAGE_URL + " TEXT "
                + ");";
    }

    private final SQLiteOpenHelper dbHelper;
    private final String tableName;

    public SyncFriendTable(SQLiteOpenHelper dbHelper, String tableName) {
        this.dbHelper = dbHelper;
        this.tableName = tableName;
    }

    private Cursor get(String whereClause, String[] params) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(tableName, PROJECTION, whereClause, params, null, null, COLUMN_DISPLAY_NAME + " ASC");
    }

    @Override
    public void add(Friend friend) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, friend.getRecordId());
        values.put(COLUMN_FRIEND_ID, friend.getFriendId());
        values.put(COLUMN_DISPLAY_NAME, friend.getDisplayName());
        values.put(COLUMN_PROFILE_IMAGE_URL, friend.getProfileImageUrl());

        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    @Override
    public Friend get(long index) {
        Cursor cursor = get(COLUMN_INDEX + "=?", new String[]{String.valueOf(index)});
        if (cursor.moveToFirst()) {
            return fromCursor(cursor);
        }
        return null;
    }

    @Override
    public List<Friend> getAll() {
        List<Friend> responseList = new ArrayList<>();
        Cursor cursor = get(null, null);

        if (cursor.moveToFirst()) {
            do {
                responseList.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return responseList;
    }

    @Override
    public void addOrUpdate(Friend friend) {
        if (get(friend.getIndex()) == null) {
            add(friend);
        } else {
            update(friend);
        }
    }

    @Override
    public void update(Friend friend) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INDEX, friend.getIndex());
        values.put(COLUMN_ID, friend.getRecordId());
        values.put(COLUMN_FRIEND_ID, friend.getFriendId());
        values.put(COLUMN_DISPLAY_NAME, friend.getDisplayName());
        values.put(COLUMN_PROFILE_IMAGE_URL, friend.getProfileImageUrl());
        db.update(tableName, values, COLUMN_INDEX + "=?", new String[]{Long.toString(friend.getIndex())});
        db.close();
    }

    @Override
    public boolean delete(long index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = db.delete(tableName, COLUMN_INDEX + "=?",
                new String[]{Long.toString(index)}) > 0;
        db.close();
        return result;
    }

    @Override
    public boolean deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(tableName, null, null) > 0;
    }

    @Override
    public boolean hasFriends() {
        Cursor cursor = get(null, null);
        return cursor.getCount() != 0;
    }

    private Friend fromCursor(Cursor cursor) {
        return Friend.builder()
                .index(cursor.getInt(0))
                .recordId(cursor.getLong(1))
                .friendId(cursor.getLong(2))
                .displayName(cursor.getString(3))
                .profileImageUrl(cursor.getString(4))
                .build();
    }
}
