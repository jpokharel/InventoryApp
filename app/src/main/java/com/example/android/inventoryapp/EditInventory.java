package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

public class EditInventory extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDIT_INVENTORY_LOADER = 0;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private Button mIncrementButton;
    private Button mDecrementButton;
    private Button mSaveButton;
    private boolean mDataChanged = false; //To track if any change was made.
    private Uri currentUri; //To store the item's URI.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDataChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory);

        Intent intent = getIntent();
        currentUri = intent.getData();

        //Validate the URI action
        if (currentUri == null) {
            setTitle("Add Item");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(EDIT_INVENTORY_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mIncrementButton = (Button) findViewById(R.id.increase_button);
        mDecrementButton = (Button) findViewById(R.id.decrease_button);
        mSaveButton = (Button) findViewById(R.id.save_inventory_button);

        //Set up touch listeners to inform user about any unsaved changes.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataChanged = true;
                String qtyString = String.valueOf(mQuantityEditText.getText());
                if(qtyString == null || TextUtils.isEmpty(qtyString))
                    mQuantityEditText.setText("1");
                else
                    mQuantityEditText.setText(String.valueOf(Integer.parseInt(qtyString) + 1));
            }
        });
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataChanged = true;
                String qtyString = String.valueOf(mQuantityEditText.getText());
                int qty = 0;
                if(qtyString == null || TextUtils.isEmpty(qtyString))
                    Toast.makeText(getApplicationContext(), "Invalid Quantity!", Toast.LENGTH_SHORT).show();
                else
                    qty = Integer.parseInt(qtyString);
                if (qty < 1)
                    Toast.makeText(getApplicationContext(), "No more items left!", Toast.LENGTH_SHORT).show();
                    else
                        mQuantityEditText.setText(String.valueOf(qty - 1));
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDataChanged) {
                    saveInventory();
                    mDataChanged = false;
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            //This implies that the action is add but not update.
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventory();
                finish();
                break;
            case R.id.action_delete:
                deleteInventory();
                showDeleteConfirmationDialog();
                break;
            case R.id.action_delete_all:
                deleteAllInventories();
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                if (!mDataChanged) {
                    NavUtils.navigateUpFromSameTask(EditInventory.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditInventory.this);
                            }
                        };
                // Notify the unsaved changes through dialog box.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveInventory() {
        ContentValues contentValues = new ContentValues();
        String name = mNameEditText.getText().toString().trim();
        String qtyString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int quantity = 0, price = 0;

        if (currentUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(qtyString)
                && TextUtils.isEmpty(priceString))
            return;
        if (!TextUtils.isEmpty(qtyString))
            quantity = Integer.parseInt(qtyString);
        if (!TextUtils.isEmpty(priceString))
            price = Integer.parseInt(priceString);

        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, name);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, price);

        if (currentUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
            if (newUri == null)
                Toast.makeText(this, R.string.error_inserting_data, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.data_inserted, Toast.LENGTH_SHORT).show();
        } else {
            int rowsUpdated = getContentResolver().update(currentUri, contentValues, null, null);
            if (rowsUpdated == 0)
                Toast.makeText(this, R.string.error_updating_data, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.data_updated, Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteInventory() {
        int mRowsDeleted = 0;
        if (currentUri != null)

            mRowsDeleted = getContentResolver().delete(
                    InventoryContract.InventoryEntry.CONTENT_URI,
                    null,
                    null
            );
        if (mRowsDeleted == 0)
            Toast.makeText(getApplicationContext(), R.string.deletion_failed, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.deletion_successful, Toast.LENGTH_SHORT).show();

        //Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE
        };

        return new CursorLoader(getApplicationContext(), currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getColumnCount() < 1)
            return;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));

            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
    }

    private void deleteAllInventories() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("EditInventoryActivity", rowsDeleted + " rows deleted from Inventory database");
    }

    @Override
    public void onBackPressed() {
        if (!mDataChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        // Notify the unsaved changes through dialog box.
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.changes_not_saved);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_item);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
