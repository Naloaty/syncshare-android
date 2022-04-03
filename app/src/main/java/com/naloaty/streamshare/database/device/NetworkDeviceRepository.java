package com.naloaty.streamshare.database.device;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.naloaty.streamshare.database.SSDatabase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * This class represents extra abstract layer above {@link NetworkDeviceDao} of {@link NetworkDevice} in a StreamShare database.
 * It allows you to retrieve information from the database.
 * @see NetworkDevice
 * @see NetworkDeviceDao
 */
public class NetworkDeviceRepository {

    private static final String TAG = "NetworkDeviceRepo";

    /*
     * TODO: AsyncTask get() method DOES NOT asynchronous (replace by ReactiveX objects)
     */

    private NetworkDeviceDao networkDeviceDao;
    private LiveData<List<NetworkDevice>> allDevices;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public NetworkDeviceRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        networkDeviceDao = database.NetworkDeviceDao();
        allDevices = networkDeviceDao.getAllDevices();
    }

    private void clearDisposables() {
        disposables.clear();
    }

    /**
     * Inserts network information about device into the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}.
     */
    public void insert(NetworkDevice networkDevice) {
        Completable completable = networkDeviceDao.insert(networkDevice);
        disposables.add(completable
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableCompletableObserver(){
                    @Override
                    public void onComplete() {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s inserted with success",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress()));

                        clearDisposables();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s insert error %s",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress(),
                                e.getMessage()));

                        clearDisposables();
                    }
                }));

    }

    /**
     * Updates network information about device in the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}
     */
    public void update(NetworkDevice networkDevice) {
        Completable completable = networkDeviceDao.update(networkDevice);
        disposables.add(completable
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableCompletableObserver(){
                    @Override
                    public void onComplete() {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s updated with success",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress()));

                        clearDisposables();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s update error %s",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress(),
                                e.getMessage()));
                        clearDisposables();
                    }
                }));

    }

    /**
     * Deletes network information about device from the database.
     * @param networkDevice Network information about device. Instance of {@link NetworkDevice}
     */
    public void delete(NetworkDevice networkDevice) {
        Completable completable = networkDeviceDao.delete(networkDevice);
        disposables.add(completable
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableCompletableObserver(){
                    @Override
                    public void onComplete() {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s deleted with success",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress()));

                        clearDisposables();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, String.format("Network device SN: %s IP: %s delete error %s",
                                networkDevice.getServiceName(),
                                networkDevice.getIpAddress(),
                                e.getMessage()));

                        clearDisposables();
                    }
                }));

    }

    /**
     * Returns all discovered network devices from database.
     * @return A list containing all discovered network devices and wrapped into LiveData object.
     */
    public LiveData<List<NetworkDevice>> getAllDevices() {
        return allDevices;
    }

    /**
     * Deletes all records from database.
     */
    public void deleteAllDevices() {
        new DeleteAllDevicesAT(networkDeviceDao).execute();
    }

    /**
     * Searches for the required network device in the database. You can specify only one of three parameters.
     * @param ipAddress Device ip address.
     * @param deviceId Device StreamShare identifier.
     * @param serviceName Device service name. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Returns network information about the device, if found, as instance of {@link NetworkDevice}
     */
    public NetworkDevice findDeviceDep(String ipAddress, String deviceId, String serviceName) {
        try {
            return new FindDeviceAT(networkDeviceDao).execute(ipAddress, deviceId, serviceName).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findDeviceDep() exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Searches for the required network device in the database. You can specify only one of three parameters.
     * @param ipAddress Device ip address.
     * @param deviceId Device StreamShare identifier.
     * @param serviceName Device service name. See {@link com.naloaty.streamshare.util.DNSSDHelper}.
     * @return Returns network information about the device, if found, as ReactiveX single object.
     */
    public Single<NetworkDevice> findDevice(String ipAddress, String deviceId, String serviceName) {
        return networkDeviceDao.findDevice(ipAddress, deviceId, serviceName);
    }

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    public int getDeviceCount() {
        try {
            return new GetDeviceCountAT(networkDeviceDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getDeviceCount() exception: " + e.getMessage());
            return 0;
        }

    }

    /**
     * Returns all discovered network devices from database.
     * @return A list containing all discovered network devices.
     */
    public List<NetworkDevice> getAllDevicesList() {
        try
        {
            return new GetAllDevicesListAT(networkDeviceDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getAllDevicesList() exception: " + e.getMessage());
            return null;
        }

    }


    public static class DeleteAllDevicesAT extends AsyncTask<Void, Void, Void> {
        private NetworkDeviceDao networkDeviceDao;

        private DeleteAllDevicesAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            networkDeviceDao.deleteAllDevices();
            return null;
        }
    }

    public static class FindDeviceAT extends AsyncTask<String, Void, NetworkDevice> {
        private NetworkDeviceDao networkDeviceDao;

        public FindDeviceAT(NetworkDeviceDao networkDeviceDao){
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected NetworkDevice doInBackground(String... strings) {
            return networkDeviceDao.findDeviceDep(strings[0], strings[1], strings[2]);
        }
    }

    public static class GetDeviceCountAT extends AsyncTask<Void, Void, Integer> {

        private NetworkDeviceDao networkDeviceDao;

        public GetDeviceCountAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return networkDeviceDao.getDeviceCount();
        }
    }

    public static class GetAllDevicesListAT extends AsyncTask<Void, Void, List<NetworkDevice>> {
        private NetworkDeviceDao networkDeviceDao;

        public GetAllDevicesListAT(NetworkDeviceDao networkDeviceDao) {
            this.networkDeviceDao = networkDeviceDao;
        }

        @Override
        protected List<NetworkDevice> doInBackground(Void... voids) {
            return networkDeviceDao.getAllDevicesList();
        }
    }
}
