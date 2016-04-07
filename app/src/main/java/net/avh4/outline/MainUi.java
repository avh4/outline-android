package net.avh4.outline;

import net.avh4.outline.android.AndroidFilesystem;
import net.avh4.outline.events.CompleteItem;
import net.avh4.outline.events.UncompleteItem;
import net.avh4.outline.features.importing.ImportAction;
import net.avh4.outline.ui.AddDialogUi;
import net.avh4.rx.PathHistory;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.UUID;

public class MainUi {
    private final DataStore dataStore;
    private final PathHistory<OutlineNodeId> pathHistory = new PathHistory<>();
    private final Generator<OutlineNodeId> idGenerator = new IdGenerator(UUID.randomUUID().toString());
    private final Filesystem filesystem = new AndroidFilesystem();
    private final Observable<OutlineView> outlineView;
    private final Observable<String> title;

    public MainUi(DataStore dataStore) {
        this.dataStore = dataStore;
        outlineView = Observable.combineLatest(dataStore.getOutline(), pathHistory.getCurrent(),
                new Func2<Outline, PathHistory.HistoryFrame<OutlineNodeId>, OutlineView>() {
                    @Override
                    public OutlineView call(Outline outline, PathHistory.HistoryFrame<OutlineNodeId> history) {
                        return new OutlineView(outline, history.getCurrent(), history.getParent());
                    }
                });
        title = Observable.concat(Observable.<String>just(null),
                Observable.combineLatest(dataStore.getOutline(), pathHistory.getCurrent(),
                        new Func2<Outline, PathHistory.HistoryFrame<OutlineNodeId>, String>() {
                            @Override
                            public String call(Outline outline, PathHistory.HistoryFrame<OutlineNodeId> history) {
                                OutlineNodeId node = history.getCurrent();
                                if (node.isRootNode()) {
                                    return null;
                                } else {
                                    return outline.getNode(node).getText();
                                }
                            }
                        }));

        Observable<Outline> initialFocus = dataStore.getOutline().first();
        initialFocus.subscribe(new Action1<Outline>() {
            @Override
            public void call(Outline outline) {
                pathHistory.push(outline.getRoot());
            }
        });
    }

    public Observable<String> getTitle() {
        return title;
    }

    public Observable<OutlineView> getOutlineView() {
        return outlineView;
    }

    public void enter(OutlineNodeId node) {
        pathHistory.push(node);
    }

    public boolean back() {
        return pathHistory.pop();
    }

    AppAction importAction(String filename) {
        return new ImportAction(dataStore, idGenerator, filesystem, filename);
    }

    public AppAction completeAction(final OutlineNodeId itemId) {
        return new AppAction() {
            @Override
            public void run(OnError e) {
                dataStore.processEvent(new CompleteItem(itemId));
            }
        };
    }

    AppAction uncompleteAction(final OutlineNodeId itemId) {
        return new AppAction() {
            @Override
            public void run(OnError e) {
                dataStore.processEvent(new UncompleteItem(itemId));
            }
        };
    }

    public Single<AddDialogUi> showAddDialog() {
        return pathHistory.getCurrent().first().map(new Func1<PathHistory.HistoryFrame<OutlineNodeId>, AddDialogUi>() {
            @Override
            public AddDialogUi call(PathHistory.HistoryFrame<OutlineNodeId> outlineNodeIdHistoryFrame) {
                OutlineNodeId addTo = outlineNodeIdHistoryFrame.getCurrent();
                return new AddDialogUi(dataStore, addTo, idGenerator);
            }
        }).toSingle();
    }
}
