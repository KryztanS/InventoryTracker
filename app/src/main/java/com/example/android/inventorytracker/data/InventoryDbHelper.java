package com.example.android.inventorytracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventorytracker.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " +
                InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_DESCRIPTION + " TEXT, " +
                InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
