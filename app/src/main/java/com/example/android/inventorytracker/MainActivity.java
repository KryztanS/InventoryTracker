package com.example.android.inventorytracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;

/**
 * Displays list of items in the inventory.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the items data loader */
    private static final int INVENTORY_LOADER = 0;

    /** Adapter for the ListView */
    private InventoryCursorAdapter mInventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up FAB to open EditorActivity
        FloatingActionButton addFab = findViewById(R.id.add_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView
        ListView mainListView = findViewById(R.id.main_list_view);
        // Set EmptyView
        TextView emptyView = findViewById(R.id.empty_view);
        mainListView.setEmptyView(emptyView);
        // Set Adapter
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        mainListView.setAdapter(mInventoryCursorAdapter);
        // Set OnItemClickListener
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Content URI of the clicked item
                Uri clickedItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                // Intent to open EditorActivity for editing clicked item
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(clickedItemUri);
                startActivity(intent);
            }
        });

        // Initialize Loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Activates when "Delete ALl" menu is selected
            case R.id.action_delete_all:
                deleteAllItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Deletes all items in the inventory.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Deleted all " + rowsDeleted + " items.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Queries Inventory data on a background thread
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Updates Adapter with cursor containing Inventory data
        mInventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Called when the data needs to be deleted
        mInventoryCursorAdapter.swapCursor(null);
    }
}
