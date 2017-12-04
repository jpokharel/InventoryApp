package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * Created by jiwanpokharel89 on 12/3/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameText = (TextView) view.findViewById(R.id.product_name_text_box);
        TextView priceText = (TextView) view.findViewById(R.id.product_price_text_box);
        TextView quantityText = (TextView) view.findViewById(R.id.product_quantity_text_box);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));

        nameText.setText(name);
        priceText.setText(price);
        quantityText.setText(quantity);

    }
}
