package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * Created by jiwanpokharel89 on 12/3/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private TextView nameText;
    private TextView priceText;
    private TextView quantityText;
    private Button saleButton;
    private String name;
    private int quantity;
    private int price;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        nameText = (TextView) view.findViewById(R.id.product_name_text_box);
        priceText = (TextView) view.findViewById(R.id.product_price_text_box);
        quantityText = (TextView) view.findViewById(R.id.product_quantity_text_box);
        saleButton = (Button) view.findViewById(R.id.sale_button);

        name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));
        quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));


        nameText.setText(name);
        priceText.setText(String.valueOf(price));
        quantityText.setText(String.valueOf(quantity));

        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));


        final ContentResolver contentResolver = view.getContext().getContentResolver();
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity--;
                    Log.e("MainActivity", "Latest quantity after sale is: " + quantity + "for Id: " + id);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
                    Uri uri = ContentUris.withAppendedId(
                            InventoryContract.InventoryEntry.CONTENT_URI, id);

                    contentResolver.update(uri,
                            contentValues,
                            null,
                            null);
                    contentResolver.notifyChange(uri, null);
                }
            }

        });

    }


}
