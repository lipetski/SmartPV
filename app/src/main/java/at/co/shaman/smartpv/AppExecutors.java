package at.co.shaman.smartpv;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private final Executor _diskIO;
    private final Executor _networkIO;
    private final Executor _mainThread;

    private AppExecutors( Executor diskIO, Executor networkIO, Executor mainThread ) {
        _diskIO = diskIO;
        _networkIO = networkIO;
        _mainThread = mainThread;
    }

    public AppExecutors() {
        this( Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3), new MainThreadExecutor());
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command ) {
            mainThreadHandler.post( command );
        }
    }

    public Executor diskIO() {
        return _diskIO;
    }

    public Executor networkIO() {
        return _networkIO;
    }

    public Executor mainThread() {
        return _mainThread;
    }


}
