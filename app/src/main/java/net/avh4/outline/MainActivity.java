package net.avh4.outline;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import net.avh4.android.ThrowableDialog;
import net.avh4.rx.History;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final DataStore store = new DataStore();
    private final Generator<OutlineNodeId> idGenerator = new IdGenerator(UUID.randomUUID().toString());

    private final Observable<OutlineView> outlineView;
    private final History<OutlineNode> history = new History<>();
    private final Observable<String> title;

    public MainActivity() {
        outlineView = Observable.combineLatest(store.getOutline(), history.getCurrent(),
                new Func2<Outline, OutlineNode, OutlineView>() {
                    @Override
                    public OutlineView call(Outline outline, OutlineNode focus) {
                        return new OutlineView(outline, focus.getId());
                    }
                });
        title = Observable.concat(Observable.<String>just(null), history.getCurrent().map(new Func1<OutlineNode, String>() {
            @Override
            public String call(OutlineNode node) {
                if (node.isRootNode()) {
                    return null;
                } else {
                    return node.getText();
                }
            }
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MaterialDialog loadingDialog = new MaterialDialog.Builder(this)
                .content(R.string.dialog_loading_initial)
                .progress(true, 0)
                .cancelable(false)
                .show();

        new AsyncTask<Void, Void, IOException>() {
            @Override
            protected IOException doInBackground(Void... params) {
                try {
                    store.initialize(MainActivity.this);
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

        title.subscribe(new Action1<String>() {
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.getCurrent().first().subscribe(new Action1<OutlineNode>() {
                    @Override
                    public void call(OutlineNode parent) {
                        showAddDialog(parent.getId());
                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        ListView listView = (ListView) findViewById(R.id.list);
        assert listView != null;
        final OutlineAdapter adapter = new OutlineAdapter(this, outlineView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OutlineNode node = adapter.getItem(position);
                history.push(node);
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

        Observable<Outline> initialFocus = store.getOutline().first();
        initialFocus.subscribe(new Action1<Outline>() {
            @Override
            public void call(Outline outline) {
                history.push(outline.getRoot());
            }
        });

        final TextView backLabel = (TextView) findViewById(R.id.back_label);
        assert backLabel != null;
        history.getParent().subscribe(new Action1<OutlineNode>() {
            @Override
            public void call(OutlineNode parent) {
                if (parent == null) {
                    backLabel.setVisibility(View.GONE);
                } else {
                    backLabel.setText(getString(R.string.back_to_node, parent.getText()));
                    backLabel.setVisibility(View.VISIBLE);
                }
            }
        });
        backLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history.pop();
            }
        });
    }

    private void showItemActionDialog(final OutlineNodeId node) {
        new MaterialDialog.Builder(MainActivity.this)
                .items(getString(R.string.action_item_delete))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        store.deleteItem(node);
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
                        OutlineNodeId itemId = idGenerator.next();
                        store.addItem(parent, itemId, input.toString());
                    }
                })
                .canceledOnTouchOutside(false)
                .negativeText(android.R.string.cancel)
                .show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
