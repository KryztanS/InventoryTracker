package com.example.android.inventorytracker;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * Allows user to add a new item or edit an existing one
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the Inventory loader */
    private static final int EDITOR_INVENTORY_LOADER = 0;

    /** Content URI for the clicked pet on MainActivity */
    private Uri mClickedItemUri;

    /** EditText fields to enter item data */
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private EditText mInStockEditText;

    /** Flag that keeps track whether the item has been edited or not */
    private boolean mItemHasChanged;

    /**
     * OnTouchListener that listens when EditText fields have been selected, setting that the item
     * has been edited.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Checks the intent if an item has been clicked, to know if an item is to be edited or added
        mClickedItemUri = getIntent().getData();
        // Editing an existing item
        if (mClickedItemUri != null) {
            setTitle("Edit Item");
            // Initialize Loader to fill the EditText fields with the existing item data
            getLoaderManager().initLoader(EDITOR_INVENTORY_LOADER, null, this);
        // Adding a new item
        } else {
            setTitle("Add an Item");
            // Invalidate options menu, to set "Delete" menu as hidden
            invalidateOptionsMenu();
        }

        // EditText fields on the editor
        mNameEditText = findViewById(R.id.name_edit_text);
        mDescriptionEditText = findViewById(R.id.description_edit_text);
        mPriceEditText = findViewById(R.id.price_edit_text);
        mInStockEditText = findViewById(R.id.in_stock_edit_text);

        // Set OnTouchListener to EditText fields, to check if the item has been edited
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mInStockEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Sets the "Delete" menu to be hidden, when adding a new item
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mClickedItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Activates when "Save" menu is selected
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            // Activates when "Delete" menu is selected
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Activates when "Up" arrow button is selected
            case android.R.id.home:
                // If item has not changed, continue exiting EditorActivity
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // If item has changed, set up a dialog to warn the user or unsaved changes
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the back button is pressed, checks if the item has been edited
     */
    @Override
    public void onBackPressed() {
        // If item has not changed, continue exiting EditorActivity
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // If item has changed, set up a dialog to warn the user or unsaved changes
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Saves item, either by updating existing item or adding a new one
     */
    private void saveItem() {
        // Gets the EditText field texts
        String nameString = mNameEditText.getText().toString();
        String descriptionString = mDescriptionEditText.getText().toString();
        String priceString = mPriceEditText.getText().toString();
        String inStockString = mInStockEditText.getText().toString();

        // Do nothing if ALL fields are empty
        if (mClickedItemUri == null && TextUtils.isEmpty(nameString + descriptionString +
                priceString + inStockString)) {
            return;
        }

        // Sets default value of Price and In Stock to 0
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        int inStock = 0;
        if (!TextUtils.isEmpty(inStockString)) {
            inStock = Integer.parseInt(inStockString);
        }

        // Create new ContentValues object, putting data from the fields
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        contentValues.put(InventoryEntry.COLUMN_DESCRIPTION, descriptionString);
        contentValues.put(InventoryEntry.COLUMN_PRICE, price);
        contentValues.put(InventoryEntry.COLUMN_QUANTITY, inStock);

        // Adds new item
        if (mClickedItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues);
            if (newUri != null) {
                Toast.makeText(this, "Item saved.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving item.", Toast.LENGTH_SHORT).show();
            }
        // Updates existing item
        } else {
            int rowUpdated = getContentResolver().update(mClickedItemUri, contentValues, null, null);
            if (rowUpdated > 0) {
                Toast.makeText(this, "Item updated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating item.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Deletes item
     */
    private void deleteItem() {
        if (mClickedItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mClickedItemUri, null, null);
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error deleting item.", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Shows an AlertDialog warning the user of unsaved changes
     *
     * @param discardButtonClickListener OnClickListener that exits EditorActivity
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an AlertDialog warning the user that an item is to be deleted
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Queries the selected item's data on a background thread
        return new CursorLoader(this,
                mClickedItemUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Sets the data on the text fields
        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME)));
            mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_DESCRIPTION)));
            mPriceEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE)));
            mInStockEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY)));
        }
    }

    @Override
    // Clears out the data from the text fields
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mInStockEditText.setText("");
    }
}
