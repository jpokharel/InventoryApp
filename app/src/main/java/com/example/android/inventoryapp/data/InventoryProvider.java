package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventoryapp.R;

/**
 * Created by jiwanpokharel89 on 12/2/2017..
 */

public class InventoryProvider extends ContentProvider{

    private static final int INVENTORIES = 100;
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORIES);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    InventoryDbHelper inventoryDbHelper;

    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SQLiteDatabase db = inventoryDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown Query Format: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri + "with match: " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                return insertInventory(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        boolean insertData = true;

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null || TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                insertData = false;
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            int quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity < 1) {
                Toast.makeText(getContext(), R.string.quantity_cannot_be_zero, Toast.LENGTH_SHORT).show();
                insertData = false;
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            int price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price <= 0) {
                Toast.makeText(getContext(), R.string.price_cannot_be_zero, Toast.LENGTH_SHORT).show();
                insertData = false;
            }
        }


        if (values.size() < 1)
            return null;

        long insertValue = -1;
        if (insertData) {
            SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
            insertValue = db.insert(InventoryContract.InventoryEntry.TABLE_NAME,
                    null,
                    values);
        }

        if (insertValue == -1) {
            Log.e("InsertInventory()", "Failed to insert inventory for URI: " + uri);
            return null;
        } else
            getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, insertValue);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int numOfDeletion = 0;
        switch (match) {
            case INVENTORIES:
                numOfDeletion = db.delete(InventoryContract.InventoryEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numOfDeletion = db.delete(InventoryContract.InventoryEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion cannot be done for URI: " + uri);
        }

        if (numOfDeletion > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return numOfDeletion;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        boolean updateData = true;

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null || TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), R.string.name_cannot_be_empty, Toast.LENGTH_SHORT).show();
                updateData = false;
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            int quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity < 1) {
                Toast.makeText(getContext(), R.string.quantity_cannot_be_zero, Toast.LENGTH_SHORT).show();
                updateData = false;
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            int price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price <= 0) {
                Toast.makeText(getContext(), R.string.price_cannot_be_zero, Toast.LENGTH_SHORT).show();
                updateData = false;
            }
        }

        if (values.size() < 1)
            return 0;

        int rowsUpdated = 0;
        // Get writable database
        if (updateData) {
            SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
            rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
