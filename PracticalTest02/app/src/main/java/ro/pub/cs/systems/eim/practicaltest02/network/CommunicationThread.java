package ro.pub.cs.systems.eim.practicaltest02.network;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.TimeInformation;


public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String command = bufferedReader.readLine();;
            if (command == null || command.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            String result = null;

            if (command.equals("set")) {
                String hour = bufferedReader.readLine();
                result = hour;
                serverThread.setData(result);
            }

            if (command.equals("reset")) {
                result = new String(":");
                serverThread.setData(result);
            }

            if (command.equals("poll")) {
                if (serverThread.getData().equals(":") || serverThread.getData().equals("")) {
                    result = new String("Set an alarm first");
                }

                TimeInformation timerInformation = null;
                String wordLink = "http://www.oraexacta.net";
                Document doc = Jsoup.connect(wordLink).get();

                Element getHour = doc.select("div[id=timediv]").first();

                Log.d("Myhour", getHour.text());

                String clock = getHour.text();

                String hour = clock.split(":")[0];
                String minutes = clock.split(":")[1];
                String seconds = clock.split(":")[2];
                String all = hour + ":"  + minutes +  ":" + seconds;

                String activated = null;

                if (!serverThread.getData().equals(":") && !serverThread.getData().equals("")) {
                    int hourToCompare = Integer.parseInt(serverThread.getData().split(":")[0]);
                    int minuteToCompare = Integer.parseInt(serverThread.getData().split(":")[1]);

                    int hourCurrent = Integer.parseInt(hour);
                    int minuteCurrent = Integer.parseInt(minutes);

                    if (hourToCompare < hourCurrent) {
                        activated = "yes";
                    } else {
                        activated = "no";
                    }
                    if (hourCurrent == hourToCompare) {
                        if (minuteToCompare < minuteCurrent)
                            activated = "yes";
                        else
                            activated = "no";
                    }
                    result = new String(activated);
                }
            }

            Log.d(Constants.TAG,"[CommandThread] " + result);
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}