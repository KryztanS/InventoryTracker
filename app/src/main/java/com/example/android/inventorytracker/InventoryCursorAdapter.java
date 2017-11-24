package com.example.android.inventorytracker;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Finds TextViews of the list item
        TextView itemNameTextView = view.findViewById(R.id.name_text_view);
        TextView descriptionTextView = view.findViewById(R.id.description_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        TextView inStockTextView = view.findViewById(R.id.in_stock_text_view);

        String itemName = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
        String description = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_DESCRIPTION));
        int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));

        if (TextUtils.isEmpty(itemName)) {
            itemName = "Unknown Item";
        }

        // Sets item data to the list item
        itemNameTextView.setText(itemName);
        descriptionTextView.setText(description);
        priceTextView.setText("₱ " + price);
        inStockTextView.setText("In Stock: " + quantity);
    }
}
