package technology.mainthread.apps.moment.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Moment;
import technology.mainthread.apps.moment.common.data.vo.MomentType;
import timber.log.Timber;

public class SyncMomentTable implements MomentTable {

    private static final String COLUMN_ID = "column_id";
    private static final String COLUMN_RECIPIENTS = "column_recipients";
    private static final String COLUMN_FILE_NAME = "column_file_name";
    private static final String COLUMN_MOMENT_TYPE = "column_moment_type";
    private static final String COLUMN_RETRIES = "column_retries";
    private static final String COLUMN_TIMESTAMP = "column_timestamp";

    private static final String[] PROJECTION = new String[]{
            COLUMN_ID,
            COLUMN_RECIPIENTS,
            COLUMN_FILE_NAME,
            COLUMN_MOMENT_TYPE,
            COLUMN_RETRIES,
            COLUMN_TIMESTAMP
    };

    public static String getCreateTableQuery(String tableName) {
        return "CREATE TABLE " + tableName + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECIPIENTS + " TEXT, "
                + COLUMN_FILE_NAME + " TEXT, "
                + COLUMN_MOMENT_TYPE + " INTEGER, "
                + COLUMN_RETRIES + " INTEGER, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";
    }

    private final MomentDbHelper dbHelper;
    private final String tableName;

    public SyncMomentTable(MomentDbHelper dbHelper, String tableName) {
        this.dbHelper = dbHelper;
        this.tableName = tableName;
    }

    private Cursor get(String whereClause, String[] params) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(tableName, PROJECTION, whereClause, params, null, null, COLUMN_TIMESTAMP + " ASC");
    }

    @Override
    public void add(Moment moment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPIENTS, toJsonString(moment.getRecipients()));
        values.put(COLUMN_FILE_NAME, moment.getFileName());
        values.put(COLUMN_MOMENT_TYPE, moment.getMomentType().ordinal());
        values.put(COLUMN_RETRIES, moment.getRetries());

        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    @Override
    public Moment get(long id) {
        Moment moment = null;
        Cursor cursor = get(COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            moment = fromCursor(cursor);
        }
        cursor.close();
        return moment;
    }

    @Override
    public Moment getNextInLine() {
        Moment moment = null;
        Cursor cursor = get(null, null);
        if (cursor.moveToFirst()) {
            moment = fromCursor(cursor);
        }
        cursor.close();
        return moment;
    }

    @Override
    public List<Moment> getAll() {
        List<Moment> responseList = new ArrayList<>();
        Cursor cursor = get(null, null);
        if (cursor.moveToFirst()) {
            do {
                responseList.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return responseList;
    }

    @Override
    public void update(Moment moment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, moment.getId());
        values.put(COLUMN_RECIPIENTS, toJsonString(moment.getRecipients()));
        values.put(COLUMN_FILE_NAME, moment.getFileName());
        values.put(COLUMN_MOMENT_TYPE, moment.getMomentType().ordinal());
        values.put(COLUMN_RETRIES, moment.getRetries());

        db.update(tableName, values, COLUMN_ID + "=?", new String[]{Long.toString(moment.getId())});
        db.close();
    }

    @Override
    public void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(tableName, COLUMN_ID + "=?",
                new String[]{Long.toString(id)});
        db.close();
    }

    @Override
    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(tableName, null, null);
        db.close();
    }

    @Override
    public boolean hasMoments() {
        Cursor cursor = get(null, null);
        boolean retval = cursor.getCount() != 0;
        cursor.close();
        return retval;
    }

    private Moment fromCursor(Cursor cursor) {
        return Moment.builder()
                .id(cursor.getInt(0))
                .recipients(toList(cursor.getString(1)))
                .fileName(cursor.getString(2))
                .momentType(MomentType.values()[cursor.getInt(3)])
                .retries(cursor.getInt(4))
                .build();
    }

    private String toJsonString(List<Long> recipients) {
        JSONObject json = new JSONObject();
        try {
            json.put(COLUMN_RECIPIENTS, new JSONArray(recipients));
        } catch (JSONException e) {
            Timber.d(e, "Error converting to Json");
        }
        return json.toString();
    }

    private List<Long> toList(String jsonString) {
        List<Long> list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonArray = json.optJSONArray(COLUMN_RECIPIENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getLong(i));
            }
        } catch (JSONException e) {
            Timber.e(e, "Error converting json to list");
        }
        return list;
    }

}
