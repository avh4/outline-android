package net.avh4.rx;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observer;

import static org.mockito.Mockito.*;

public class HistoryTest {

    private History<String> subject;
    @Mock
    private Observer<String> currentObserver;
    @Mock
    private Observer<String> parentObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new History<>();

        subject.getCurrent().subscribe(currentObserver);
        subject.getParent().subscribe(parentObserver);
    }

    @Test
    public void initialState() throws Exception {
        verifyZeroInteractions(currentObserver);
        verifyZeroInteractions(parentObserver);
    }

    @Test
    public void firstPush() {
        subject.push("AAA");

        verify(currentObserver).onNext("AAA");
        verify(parentObserver).onNext(null);
    }

    @Test
    public void secondPush() {
        subject.push("AAA");
        reset(currentObserver, parentObserver);
        subject.push("BBB");

        verify(currentObserver).onNext("BBB");
        verify(parentObserver).onNext("AAA");
    }
}