package com.vadimicus.bluetoothtest.utils;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

/**
 * Created by vadimicus on 05.04.2018.
 */

public class Utils {

    public static String generateCode (){
        //Code should be 6 digits, generated randomly

        Random random = new Random();

        String toOut = "";

        for (int i = 0; i< 8; i++){
            toOut += String.valueOf(random.nextInt(10));
        }
//        return String.valueOf(random.nextInt(100000000));
//        return String.valueOf(random.nextInt(1000000));
        return toOut;
    }

    public static AdvertiseData buildAdvertiseData(String code) {

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("00000000-0000-0000-0000-000000"+code));

//        pUuid = new ParcelUuid(UUID.randomUUID());

        dataBuilder.setIncludeTxPowerLevel(true);
//        dataBuilder.addServiceData(pUuid, "Data".getBytes(Charset.forName("UTF-8")));
//        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.addServiceUuid(pUuid);
        return dataBuilder.build();
    }


    public static AdvertiseSettings buildAdvertiseSettings() {

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

}
