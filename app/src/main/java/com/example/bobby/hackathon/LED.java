package com.example.bobby.hackathon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Florian on 12.01.2017.
 */

public class LED{

    DataOutputStream os = null;
    DataInputStream is = null;
    BufferedReader bfr = null;
    int GPIO; // Nummer des GPIO Pins

    public LED(int GPIO){
        this.GPIO = GPIO;
        String suPath="";
        boolean check = true;
        Process process = null;
        if (new File("/system/bin/su").exists()){
            suPath = "/system/bin/su";
        }
        if (new File("/system/xbin/su").exists()){
            suPath = "/system/xbin/su";
        }

        try {
            process = Runtime.getRuntime().exec(suPath);
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
            check = false;
        }

        try {
            //erzeugt Prozess aus der Laufzeitumgebung
            //Process process = Runtime.getRuntime().exec("getprop");
            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            bfr = new BufferedReader(new InputStreamReader(is));
            //GPIO einrichten
            LEDcreate();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable t){
            t.printStackTrace();
        }


    }

    public void LEDcreate()throws Throwable{
        os.writeBytes("echo " + GPIO + " > /sys/class/gpio/export\n");
        os.writeBytes("echo out > /sys/class/gpio/gpio"+GPIO +"/direction\n");
    }

    public void LEDon() throws Throwable{
        os.writeBytes("cat /sys/class/gpio/gpio"+GPIO +"/value\n");
        String in = bfr.readLine();
        if(in.equals("0")){
            //Ordner der die GPIO Datei enthält zum export angeben
            os.writeBytes("echo 1 > /sys/class/gpio/gpio"+GPIO +"/value\n");

        }



    };

    public void LEDOff()throws Throwable{


        os.writeBytes("cat /sys/class/gpio/gpio"+GPIO +"/value\n");
        String in = bfr.readLine();
        if(in.equals("1")){
            //Ordner der die GPIO Datei enthält zum export angeben
            os.writeBytes("echo 0 > /sys/class/gpio/gpio"+GPIO +"/value\n");

        }
    }

    public void blinken(int anzahl)throws Throwable{
        for(int i=0; i<anzahl;i++) {
            LEDon();
            // warten bis GPIO value geschrieben wurde
            Thread.sleep(250);
            LEDOff();
            Thread.sleep(700);
            System.out.println("BlinkNr: " + i);
        }

    }

}
