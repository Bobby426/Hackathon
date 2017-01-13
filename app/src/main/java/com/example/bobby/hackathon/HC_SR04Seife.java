package com.example.bobby.hackathon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Jochen on 12.01.2017.
 */

public class HC_SR04Seife extends Thread {

    DataOutputStream os = null;
    DataInputStream is = null;
    BufferedReader bfr = null;

    private long startzeit = 0, endzeit = 0, dauer;
    private float finaled, oldd;
    ArrayList<Integer> list;



    //Konstruktor
    public HC_SR04Seife() {

        finaled = 200.0f;


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
    }

    private void createPins() throws Throwable {

        //Ordner der die GPIO Datei enthält zum export angeben
        os.writeBytes("echo 218 > /sys/class/gpio/export\n");
        os.writeBytes("echo 224 > /sys/class/gpio/export\n");
        // direction der GPIO Pins festlegen
        os.writeBytes("echo out > /sys/class/gpio/gpio218/direction\n");
        os.writeBytes("echo in > /sys/class/gpio/gpio224/direction\n");
    }

    private long getTriggerTime()throws Throwable{
        int timeout1 = 2100, timeout0 = 2100;
        // misst die Dauer, für die ein Signal vom HC_ST04 empfangen wird
        boolean abbruch = false;
        //wartet bis etwas empfangen wird und startet dann den Timer
        while(!abbruch){
            os.writeBytes("cat /sys/class/gpio/gpio224/value\n");
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
            os.writeBytes("cat /sys/class/gpio/gpio224/value\n");
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
        os.writeBytes("echo 1 > /sys/class/gpio/gpio218/value\n");
        // Trigger braucht 0,01 ms
        Thread.sleep(0, 10000);
        os.writeBytes("echo 0 > /sys/class/gpio/gpio218/value\n");

        localtime = getTriggerTime();

        return localtime * 340.29f / (20000); //Distanz ausgeben
    }
    public void run() {
        System.out.println("Das Modul HC_SR04_Seife wird ausgeführt");

        list = new ArrayList<Integer>();

        try{
            createPins();

            int counter =0;
            int distance; // gemessene Distanz
            int referenz; // Referenz Messwert
            int maxheight = 500; // in cm
            boolean canMeasure = true;
            while (true){
                //Für jeden Durchlauf müssen folgende Variablen zurückgesetzt werden
                int sum = 0;
                distance = 0;
                //messen
                distance = (int)getdistance();

                //Referenzwerte messen
                if(list.size()<20){
                    list.add(distance);
                    if(list.size()==19){
                        System.out.println("Referenzwerte gesammelt");
                    };
                }
                else {

                    for(int i = 0;i<list.size();i++){
                        sum= sum + list.get(i);
                        System.out.println();

                    }
                    referenz = sum/list.size();
                    /* if(canMeasure==false && (distance > referenz-10 && distance < referenz+10)){
                         canMeasure = true;
                     }*/
                    int milliliter;
                    canMeasure=true;

                    //Wird nicht kalibriert weil er vor der Seifenfüllung kalibriert werden müsste.
                    //Methode trotzdem implementiert

                    if(canMeasure ==true && (distance <=5)){
                        //Besucher
                        milliliter = 1000;
                        if(milliliter<=1000 && milliliter>=0){
                            //System.out.println("Aktueller Messwert: " + distance);
                            System.out.println("Füllstand: "+milliliter);
                            System.out.println("Status: grün");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 5) && (distance <= 7)){
                        //Besucher
                        milliliter = 900;
                        if(milliliter<=1000 && milliliter>=0){
                            //System.out.println("Aktueller Messwert: " + distance);
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: grün");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 8) && (distance <= 9)){
                        //Besucher
                        milliliter = 800;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: grün");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 10) && (distance <= 12)){
                        //Besucher
                        milliliter = 700;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: grün");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 12) && (distance <= 13)){
                        //Besucher
                        milliliter = 600;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: grün");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 13) && (distance <= 14)){
                        //Besucher
                        milliliter = 500;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: gelb");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 14) && (distance <= 15)){
                        //Besucher
                        milliliter = 400;
                        if(milliliter<=1000 && milliliter>=0) {
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: gelb");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 15) && (distance <= 16)){
                        //Besucher
                        milliliter = 300;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Füllstand: " + milliliter);
                            System.out.println("Status: gelb");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 16) && (distance <= 17)){
                        //Besucher
                        milliliter = 200;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Aktueller Messwert: " + milliliter);
                            System.out.println("Füllstand: " + "gelb");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 17) && (distance <=18)){
                        //Besucher
                        milliliter = 100;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Aktueller Messwert: " + milliliter);
                            System.out.println("Füllstand: " + "gelb");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }

                    else if(canMeasure ==true && (distance > 18)){
                        //Besucher
                        milliliter = 0;
                        if(milliliter<=1000 && milliliter>=0){
                            System.out.println("Aktueller Messwert: " + milliliter);
                            System.out.println("Füllstand: " + "rot");
                            new SeifeToDatabase().execute("3", Integer.toString(milliliter));
                        }
                        canMeasure= false;
                        Thread.sleep(1900);
                    }
                }
                Thread.sleep(80);
            }//while

        }
        catch (Throwable t) {
            t.printStackTrace();
        }

    }//run()

}//class

