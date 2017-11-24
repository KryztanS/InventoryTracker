package com.example.android.inventorytracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * ContentProvider for the Inventory
 */
public class InventoryProvider extends ContentProvider {

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mInventoryDbHelper;

    /** URI Matcher code for the content URI for the items table and a single item */
    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mInventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mInventoryDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projections, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projections, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for query: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Invalid uri for insert: " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {
        String itemName = contentValues.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (itemName == null) {
            throw new IllegalArgumentException("Item requires a valid name.");
        }
        Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Item requires a valid price.");
        }
        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity.");
        }

        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.v(LOG_TAG, "Failed to insert item with uri: " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid uri for update: " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
            String itemName = contentValues.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if (itemName == null) {
                throw new IllegalArgumentException("Item requires a valid name.");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires a valid price.");
            }
        }
        if (contentValues.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires a valid quantity.");
            }
        }
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for delete: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Invalid uri: " + uri + " with match: " + match);
        }
    }
}
