package com.vadimicus.bluetoothtest.utils;

/**
 * Created by vadimicus on 20.04.2018.
 */



public class Receiver {

    private String id;
    private int currency_id;
    private long amount;
    private String userCode;

    public Receiver(
            String id,
            int currency_id,
            long amount,
            String userCode
    ){
        this.id = id;
        this.currency_id = currency_id;
        this.amount = amount;
        this.userCode = userCode;
    }

    public String getId(){return this.id;}
    public int getCurrencyId(){return this.currency_id;}
    public long getAmount(){return this.amount;}
    public String getUserCode(){return this.userCode;}

    public void setId(String id){this.id = id;}
    public void setCurrencyId(int currency_id){this.currency_id= currency_id;}
    public void setAmount(long amount){this.amount = amount;}

}
