package net.avh4.outline;

import net.avh4.outline.android.AndroidFilesystem;
import net.avh4.outline.features.importing.ImportAction;
import net.avh4.rx.History;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.UUID;

public class MainUi {
    private final DataStore dataStore;
    private final History<OutlineNode> history = new History<>();
    private final Generator<OutlineNodeId> idGenerator = new IdGenerator(UUID.randomUUID().toString());
    private final Filesystem filesystem = new AndroidFilesystem();
    private final Observable<OutlineView> outlineView;
    private final Observable<String> title;

    public MainUi(DataStore dataStore) {
        this.dataStore = dataStore;
        outlineView = Observable.combineLatest(dataStore.getOutline(), history.getCurrent(),
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

        Observable<Outline> initialFocus = dataStore.getOutline().first();
        initialFocus.subscribe(new Action1<Outline>() {
            @Override
            public void call(Outline outline) {
                history.push(outline.getRoot());
            }
        });
    }

    public Observable<String> getTitle() {
        return title;
    }

    public Observable<OutlineNode> getCurrent() {
        return history.getCurrent();
    }

    public Observable<OutlineView> getOutlineView() {
        return outlineView;
    }

    public void enter(OutlineNode node) {
        history.push(node);
    }

    public void back() {
        history.pop();
    }

    public Observable<OutlineNode> getCurrentParent() {
        return history.getParent();
    }

    public AppAction importAction(String filename) {
        return new ImportAction(dataStore, idGenerator, filesystem, filename);
    }

    public AppAction addAction(final OutlineNodeId parent, final String text) {
        return new AppAction() {
            @Override
            public void run(OnError e) {
                OutlineNodeId itemId = idGenerator.next();
                dataStore.addItem(parent, itemId, text);
            }
        };
    }
}
