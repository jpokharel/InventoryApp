package com.example.android.inventoryapp;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static int INVENTORY_LOADER = 0;
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditInventory.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_item);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(inventoryCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditInventory.class);
                Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI,id);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_page_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete_all:
                deleteAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE};
        return new CursorLoader(this,
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        inventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }
}
