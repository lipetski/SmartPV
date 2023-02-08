package at.co.shaman.smartpv;

import android.app.Application;

public class SmartPV extends Application {
    private AppExecutors _executors;

    @Override
    public void onCreate() {
        super.onCreate();
        _executors = new AppExecutors();
    }

    public AppExecutors getExecutors() {
        return _executors;
    }
}
