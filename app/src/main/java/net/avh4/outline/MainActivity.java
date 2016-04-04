package net.avh4.outline;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nononsenseapps.filepicker.FilePickerActivity;
import net.avh4.android.OnItemCheckedChangedListener;
import net.avh4.android.ThrowableDialog;
import net.avh4.outline.ui.actions.NotImplementedAction;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import rx.functions.Action1;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_CODE_FILE = 0x9247;
    private DataStore dataStore;
    private MainUi ui;

    private PMap<Integer, AppAction> menuActions;
    private AppAction.OnError errorHandler = new AppAction.OnError() {
        @Override
        public void onError(Throwable err) {
            ThrowableDialog.show(MainActivity.this, err);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventStore eventStore = new EventStore(getApplicationContext());
        dataStore = new DataStore(eventStore);
        ui = new MainUi(dataStore);
        menuActions = HashTreePMap.<Integer, AppAction>empty()
                .plus(R.id.action_import,
                        new AppAction() {
                            @Override
                            public void run(OnError e) {
                                Intent i = new Intent(MainActivity.this, FilePickerActivity.class);
                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                                startActivityForResult(i, RESULT_CODE_FILE);
                            }
                        })
                .plus(R.id.action_settings, new NotImplementedAction(this));

        final MaterialDialog loadingDialog = new MaterialDialog.Builder(this)
                .content(R.string.dialog_loading_initial)
                .progress(true, 0)
                .cancelable(false)
                .show();

        new AsyncTask<Void, Void, IOException>() {
            @Override
            protected IOException doInBackground(Void... params) {
                try {
                    dataStore.initialize();
                    return null;
                } catch (IOException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(IOException error) {
                loadingDialog.dismiss();
                if (error == null) {
                    onDataLoaded();
                } else {
                    ThrowableDialog.show(MainActivity.this, error);
                }
            }
        }.execute();

        ui.getTitle().subscribe(new Action1<String>() {
            @Override
            public void call(String title) {
                if (title == null) {
                    setTitle(R.string.app_name);
                } else {
                    setTitle(title);
                }
            }
        });
    }

    private void onDataLoaded() {
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui.getCurrent().first().subscribe(new Action1<OutlineNode>() {
                    @Override
                    public void call(OutlineNode parent) {
                        showAddDialog(parent.getId());
                    }
                });
            }
        });

        ListView listView = (ListView) findViewById(R.id.list);
        assert listView != null;
        final OutlineAdapter adapter = new OutlineAdapter(this, ui.getOutlineView());
        adapter.setOnItemCheckedChangedListener(new OnItemCheckedChangedListener() {
            @Override
            public void onItemCheckedChanged(int position, boolean isChecked) {
                OutlineNode node = adapter.getItem(position);
                if (isChecked) {
                    ui.completeAction(node.getId()).run(errorHandler);
                } else {
                    ui.uncompleteAction(node.getId()).run(errorHandler);
                }
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OutlineNode node = adapter.getItem(position);
                ui.enter(node);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                OutlineNode node = adapter.getItem(position);
                showItemActionDialog(node.getId());
                return true;
            }
        });

        ui.getCurrentParent().subscribe(new Action1<OutlineNode>() {
            @Override
            public void call(OutlineNode parent) {
                if (parent == null) {
                    toolbar.setNavigationIcon(null);
                } else {
                    toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                }
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.back();
            }
        });
    }

    private void showItemActionDialog(final OutlineNodeId node) {
        new MaterialDialog.Builder(MainActivity.this)
                .items(getString(R.string.action_item_delete))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        dataStore.deleteItem(node);
                    }
                })
                .show();
    }

    private void showAddDialog(final OutlineNodeId parent) {
        new MaterialDialog.Builder(MainActivity.this)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        ui.addAction(parent, input.toString()).run(errorHandler);
                    }
                })
                .canceledOnTouchOutside(false)
                .negativeText(android.R.string.cancel)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        AppAction action = menuActions.get(id);
        if (action != null) {
            action.run(errorHandler);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_FILE && resultCode == Activity.RESULT_OK) {
            assert data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false) == false;
            Uri uri = data.getData();
            if (uri.getScheme().equals("file")) {
                ui.importAction(uri.getPath()).run(errorHandler);
            } else {
                errorHandler.onError(new RuntimeException("Expected a file Uri from the file picker, but got: " + uri));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
