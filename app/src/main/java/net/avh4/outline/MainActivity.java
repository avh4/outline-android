package net.avh4.outline;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nononsenseapps.filepicker.FilePickerActivity;
import io.doorbell.android.Doorbell;
import net.avh4.android.OnItemCheckedChangedListener;
import net.avh4.android.ThrowableDialog;
import net.avh4.outline.events.Move;
import net.avh4.outline.events.Reorder;
import net.avh4.outline.ui.AddDialogUi;
import net.avh4.time.AndroidTime;
import net.avh4.time.Time;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import rx.functions.Action1;

import java.io.IOException;
import java.util.ArrayList;

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
    private OutlineAdapter adapter;
    private OutlineAdapter.Mode adapterMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventStore eventStore = new EventStore(getApplicationContext());
        dataStore = new DataStore(eventStore);
        Time time = new AndroidTime();
        ui = new MainUi(dataStore, time);
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
                .plus(R.id.action_feedback,
                        new AppAction() {
                            @Override
                            public void run(OnError e) {
                                new Doorbell(MainActivity.this, 3572, OutlineApplication.credentials.doorbellPrivateKey)
                                        .setEmailHint(getString(R.string.feedback_your_email_optional))
                                        .show();
                            }
                        })
                .plus(R.id.action_reorder,
                        new AppAction() {
                            @Override
                            public void run(OnError e) {
                                if (adapter != null) {
                                    adapter.showReorder();
                                }
                            }
                        })
                .plus(R.id.action_show_checkboxes,
                        new AppAction() {
                            @Override
                            public void run(OnError e) {
                                if (adapter != null) {
                                    adapter.showCheckboxes();
                                }
                            }
                        })
        ;

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
                showAddDialog();
            }
        });

        RecyclerView listView = (RecyclerView) findViewById(R.id.list);
        assert listView != null;
        adapter = new OutlineAdapter(this, ui.getOutlineView());
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
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(listView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OutlineNode node = adapter.getItem(position);
                ui.enter(node.getId());
            }
        });
        adapter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final OutlineNode node = adapter.getItem(position);
                ui.getOutlineView().first().subscribe(new Action1<OutlineView>() {
                    @Override
                    public void call(OutlineView outlineView) {
                        showItemActionDialog(node, outlineView.getNode(), outlineView.getParent());
                    }
                });
                return true;
            }
        });
        adapter.setOnStartDragListener(
                new OnStartDragListener() {
                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                        itemTouchHelper.startDrag(viewHolder);
                    }
                });
        adapter.setOnItemsReorderedListener(new OnItemsReorderedListener() {
            @Override
            public void onItemsReordered(OutlineView current, PVector<Integer> newPositionOrder) {
                OutlineNodeId parent = current.getNode().getId();

                PVector<OutlineNodeId> newOrder = TreePVector.empty();
                for (Integer position : newPositionOrder) {
                    newOrder = newOrder.plus(current.getChild(position).getId());
                }
                dataStore.processEvent(new Reorder(parent, newOrder));
            }
        });
        adapter.getMode().subscribe(new Action1<OutlineAdapter.Mode>() {
            @Override
            public void call(OutlineAdapter.Mode mode) {
                adapterMode = mode;
                invalidateOptionsMenu();
                if (mode == OutlineAdapter.Mode.REORDER) {
                    Toast.makeText(MainActivity.this, R.string.action_drag_to_reorder, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ui.getOutlineView().subscribe(new Action1<OutlineView>() {
            @Override
            public void call(OutlineView outlineView) {
                OutlineNode parent = outlineView.getParent();
                if (parent == null) {
                    toolbar.setNavigationIcon(null);
                } else {
                    toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                    toolbar.setNavigationContentDescription(getString(R.string.action_go_up, parent.getText()));
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

    private void showItemActionDialog(final OutlineNode child, final OutlineNode parent, @Nullable final OutlineNode grandparent) {
        ArrayList<String> actionNames = new ArrayList<>();
        final ArrayList<Runnable> actionCallbacks = new ArrayList<>();

        if (parent.getChildren().size() > 1) {
            actionNames.add(getString(R.string.action_item_move_into));
            actionCallbacks.add(new Runnable() {
                @Override
                public void run() {
                    ui.getOutlineView().first().subscribe(new Action1<OutlineView>() {
                        @Override
                        public void call(OutlineView outlineView) {
                            final ArrayList<String> choiceNames = new ArrayList<>();
                            final ArrayList<OutlineNodeId> choiceIds = new ArrayList<>();
                            Outline outline = outlineView.getOutline();
                            for (OutlineNodeId childId : parent.getChildren()) {
                                if (childId.equals(child.getId())) continue;
                                OutlineNode child = outline.getNode(childId);
                                choiceNames.add(child.getText());
                                choiceIds.add(child.getId());
                            }

                            new MaterialDialog.Builder(MainActivity.this)
                                    .items(choiceNames)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                            OutlineNodeId to = choiceIds.get(which);
                                            dataStore.processEvent(new Move(child.getId(), parent.getId(), to));
                                        }
                                    }).show();
                        }
                    });
                }
            });
        }

        if (grandparent != null) {
            actionNames.add(getString(R.string.action_item_move_up, grandparent.getText()));
            actionCallbacks.add(new Runnable() {
                @Override
                public void run() {
                    dataStore.processEvent(new Move(child.getId(), parent.getId(), grandparent.getId()));
                }
            });
        }

        actionNames.add(getString(R.string.action_item_delete));
        actionCallbacks.add(new Runnable() {
            @Override
            public void run() {
                dataStore.deleteItem(child.getId());
            }
        });

        new MaterialDialog.Builder(MainActivity.this)
                .items(actionNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        actionCallbacks.get(which).run();
                    }
                })
                .show();
    }

    private void showAddDialog() {
        ui.showAddDialog().subscribe(new Action1<AddDialogUi>() {
            @Override
            public void call(final AddDialogUi addDialogUi) {
                new MaterialDialog.Builder(MainActivity.this)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                addDialogUi.submit(input.toString());
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .negativeText(android.R.string.cancel)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (adapterMode == OutlineAdapter.Mode.CHECKBOX) {
            menu.findItem(R.id.action_show_checkboxes).setVisible(false);
            menu.findItem(R.id.action_reorder).setVisible(true);
        } else if (adapterMode == OutlineAdapter.Mode.REORDER) {
            menu.findItem(R.id.action_show_checkboxes).setVisible(true);
            menu.findItem(R.id.action_reorder).setVisible(false);
        } else {
            menu.findItem(R.id.action_show_checkboxes).setVisible(false);
            menu.findItem(R.id.action_reorder).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
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

    @Override
    public void onBackPressed() {
        if (ui.back()) {
            return;
        } else {
            super.onBackPressed();
        }
    }
}
