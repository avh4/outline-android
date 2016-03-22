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
import com.afollestad.materialdialogs.MaterialDialog;
import net.avh4.android.ThrowableDialog;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final DataStore store = new DataStore(UUID.randomUUID().toString());
    private ReplaySubject<OutlineNodeId> focus = ReplaySubject.createWithSize(1);

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
                focus.first().subscribe(new Action1<OutlineNodeId>() {
                    @Override
                    public void call(OutlineNodeId parent) {
                        showAddDialog(parent);
                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        ListView listView = (ListView) findViewById(R.id.list);
        assert listView != null;
        final OutlineAdapter adapter = new OutlineAdapter(this, store.getOutline(), focus);
        listView.setAdapter(adapter);
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
                focus.onNext(outline.getRoot());
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
                        store.addItem(parent, input.toString());
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
