package com.prueba.miguelforero.helpers;

/**
 * Created by Miguel Forero on 09/09/2016.
 */
public class TodoHelper {

    public Boolean isOnlineNet() {

        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
