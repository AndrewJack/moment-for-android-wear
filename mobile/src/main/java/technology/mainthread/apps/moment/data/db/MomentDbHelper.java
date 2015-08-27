package technology.mainthread.apps.moment.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MomentDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MOMENT_DB";
    private final String[] tables;

    public MomentDbHelper(Context context, String... tables) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tables = tables; // tables to create
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String table : tables) {
            db.execSQL(SyncMomentTable.getCreateTableQuery(table));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}
