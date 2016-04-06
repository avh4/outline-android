package net.avh4.rx;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observer;

import static org.mockito.Mockito.*;

public class PathHistoryTest {

    private PathHistory<String> subject;
    @Mock
    private Observer<PathHistory.HistoryFrame<String>> currentObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new PathHistory<>();

        subject.getCurrent().subscribe(currentObserver);
    }

    @Test
    public void initialState() throws Exception {
        verifyZeroInteractions(currentObserver);
    }

    @Test
    public void firstPush() {
        subject.push("AAA");

        verify(currentObserver).onNext(new PathHistory.HistoryFrame<>(null, "AAA"));
    }

    @Test
    public void secondPush() {
        subject.push("AAA");
        reset(currentObserver);
        subject.push("BBB");

        verify(currentObserver).onNext(new PathHistory.HistoryFrame<>("AAA", "BBB"));
    }
}