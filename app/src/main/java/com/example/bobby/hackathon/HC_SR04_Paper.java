package com.example.bobby.hackathon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Florian on 12.01.2017.
 */

public class HC_SR04_Paper extends Thread{

    DataOutputStream os = null;
    DataInputStream is = null;
    BufferedReader bfr = null;

    private long startzeit = 0, endzeit = 0, dauer;
    private float finaled;
    ArrayList<Integer> list;
    SortedMap<Integer,Integer> map;
    private int  MIN;
    LED lc_led;

    public HC_SR04_Paper(LED lc_led){
        this.lc_led = lc_led;

        finaled = 200.0f;
        list = new ArrayList<Integer>();
        MIN = 0;

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
        } catch (Exception e) {
            e.printStackTrace();
        }




    }//Konstruktor

    private void createPins() throws Throwable {

        //Ordner der die GPIO Datei enthält zum export angeben
        os.writeBytes("echo 249 > /sys/class/gpio/export\n");
        os.writeBytes("echo 247 > /sys/class/gpio/export\n");
        // direction der GPIO Pins festlegen
        os.writeBytes("echo out > /sys/class/gpio/gpio249/direction\n");
        os.writeBytes("echo in > /sys/class/gpio/gpio247/direction\n");
    }
    private long getTriggerTime()throws Throwable{
        int timeout1 = 2100, timeout0 = 2100;
        // misst die Dauer, für die ein Signal vom HC_ST04 empfangen wird
        boolean abbruch = false;
        //wartet bis etwas empfangen wird und startet dann den Timer
        while(!abbruch){
            os.writeBytes("cat /sys/class/gpio/gpio247/value\n");
            String in = bfr.readLine();
            if(in.equals("1")) {
                startzeit = System.nanoTime();
                abbruch = true;
            } else {
                if(timeout1 > 0) {
                    timeout1--;
                } else {
                    System.out.println("Timeout while waiting for 1");
                    abbruch = true;
                }
            }
        }
        abbruch = false;

        //wartet bis kein Signal mehr gesendet wird und stoppt dann den Timer
        while(!abbruch) {
            os.writeBytes("cat /sys/class/gpio/gpio247/value\n");
            String in = bfr.readLine();
            if(in.equals("0")) {
                endzeit = System.nanoTime();
                abbruch = true;
            } else {
                if(timeout0 > 0) {
                    timeout0--;
                } else {
                    System.out.println("Timeout while waiting for 0");
                    abbruch = true;
                }
            }
        }
        //System.out.println("Die Dauer des Signals beträgt: " + (endzeit - startzeit));
        dauer = (endzeit - startzeit) / 1000;
        return dauer;
    }

    private float getdistance()throws Throwable{
        long  localtime = 0;
        // setzt Signal für den Trigger
        os.writeBytes("echo 1 > /sys/class/gpio/gpio249/value\n");
        // Trigger braucht 0,01 ms
        Thread.sleep(0, 10000);
        os.writeBytes("echo 0 > /sys/class/gpio/gpio249/value\n");

        localtime = getTriggerTime();

        return localtime * 340.29f / (20000); //Distanz ausgeben
    }

    public int setMinimum()throws Throwable{
        map = new TreeMap<Integer,Integer>();

        //Bevor der Sensor zuverlässig misst, muss mit der minimalen Menge an Tüchern der Sensor eingestellt werden
        //Dieser Wert wird dann als Untergrenzen festgehalten -> Median berechnen
        int cm;
        int array[] = new int[5];
        int sum = 0;
       //Map<Integer,Integer> tempi;
        int tempint;
        for(int i=0;i<5;i++){
        cm = getFuellMenge();
            System.out.println("Gemesse:" + cm);
        sum = sum + cm;

           /* if(map.get(cm)!= null){
                map.put(cm,map.get(cm)+1);
            }else{
                map.put(cm,1);
            }
        */


        }

        return sum/5;

    }

    public int getFuellMenge()throws Throwable{
        int distance;
        int sum = 0;
        list.clear();
        while (list.size()<15){
           distance = (int) getdistance();
            Thread.sleep(100);
            //Wert darf nicht höher als 50cm sein, da Papierspender nicht so hoch sein kann
            if(distance<50){
                list.add(distance);
            }
        }


        for(int i=0;i<list.size();i++){
            sum = sum + list.get(i);
        }
        return sum/list.size();
    }

    public void run(){



        try {
            createPins();
            if(MIN==0){
                MIN = setMinimum();
                System.out.println("Minimum: " +MIN);
            }
            while (true){

               int cm = getFuellMenge();
                new PaperToDatabase().execute("2", Integer.toString(cm));


                if(cm>= MIN-1){
                    System.out.println("" + cm + "cm es ist leer ");
                    lc_led.LEDon();
                }else{
                    lc_led.LEDOff();
                    System.out.println("Es werden " + cm + "cm gemessen und in die DB gepostet");
                }


                Thread.sleep(10000);
            }


        } catch(Exception e){
            e.printStackTrace();
        }
        catch(Throwable t){
            t.printStackTrace();
        }

    }

}
