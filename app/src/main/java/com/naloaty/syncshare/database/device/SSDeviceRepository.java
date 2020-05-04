package com.naloaty.syncshare.database.device;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.naloaty.syncshare.database.SSDatabase;

import java.util.List;

import io.reactivex.Single;

public class SSDeviceRepository {

    private static final String TAG = "SSDeviceRepo";

    /*
     * TODO: AsyncTask get() method DOES NOT asynchronous (replace by rx)
     */

    private SSDeviceDao ssDeviceDao;
    private LiveData<List<SSDevice>> allDevices;

    public SSDeviceRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        ssDeviceDao = database.ssDeviceDao();
        allDevices = ssDeviceDao.getAllDevices();
    }

    public void insert(SSDevice device) {
        new InsertDeviceAT(ssDeviceDao).execute(device);
    }

    public void update(SSDevice device) {
        new UpdateDeviceAT(ssDeviceDao).execute(device);
    }

    //Decides whether to add or update
    public void publish(SSDevice device) {
        SSDevice foundedDevice = findDeviceDep(device.getDeviceId());

        if (foundedDevice != null){
            device.setId(foundedDevice.getId());
            device.setTrusted(foundedDevice.isTrusted());
            device.setAccessAllowed(foundedDevice.isAccessAllowed());
            update(device);
        }
        else
        {
            insert(device);
        }
    }

    public void delete(SSDevice device) {
        new DeleteDeviceAT(ssDeviceDao).execute(device);
    }

    public LiveData<List<SSDevice>> getAllDevices () {
        return allDevices;
    }

    public int getDeviceCount() {
        try
        {
            return new GetDeviceCountAT(ssDeviceDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getDeviceCount() exception: " + e.getMessage());
            return new Integer(0);
        }

    }

    @Deprecated
    public SSDevice findDeviceDep(String deviceId) {
        try{
            return new FindDeviceAT(ssDeviceDao).execute(deviceId).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findDeviceDep() exception: " + e.getMessage());
            return null;
        }
    }

    public Single<SSDevice> findDevice(String deviceId) {
        return ssDeviceDao.findDevice(deviceId);
    }

    public static class InsertDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public InsertDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.insert(ssDevices[0]);
            return null;
        }
    }

    public static class UpdateDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public UpdateDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.update(ssDevices[0]);
            return null;
        }
    }

    public static class DeleteDeviceAT extends AsyncTask<SSDevice, Void, Void> {

        private SSDeviceDao ssDeviceDao;

        public DeleteDeviceAT (SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Void doInBackground(SSDevice... ssDevices) {
            ssDeviceDao.delete(ssDevices[0]);
            return null;
        }
    }

    public static class FindDeviceAT extends AsyncTask<String, Void, SSDevice> {

        private SSDeviceDao ssDeviceDao;

        public FindDeviceAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected SSDevice doInBackground(String... strings) {
            return ssDeviceDao.findDeviceDep(strings[0]);
        }
    }

    public static class GetDeviceCountAT extends AsyncTask<Void, Void, Integer> {

        private SSDeviceDao ssDeviceDao;

        public GetDeviceCountAT(SSDeviceDao ssDeviceDao) {
            this.ssDeviceDao = ssDeviceDao;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return ssDeviceDao.getDeviceCount();
        }
    }
}
