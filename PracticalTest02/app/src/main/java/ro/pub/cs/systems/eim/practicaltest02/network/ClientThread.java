package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.TimeInformation;

public class ClientThread extends Thread {


    private String address;
    private int port;
    private TextView timerTextView;
    private Socket socket;
    public String word;
    public TimeInformation timeInformation;


    public ClientThread(String address, int port, TextView timerTextView, String word, TimeInformation timeInformation) {
        this.port = port;
        this.address = address;
        this.timerTextView = timerTextView;
        this.word = word;
        this.timeInformation = timeInformation;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.d("cuvantul in client: ", word);
            printWriter.println(word);
            printWriter.flush();

            printWriter.println(timeInformation);
            printWriter.flush();


            String timerInformation;

            while ((timerInformation = bufferedReader.readLine()) != null) {
                Log.d(Constants.TAG,"[ClientThread] " + timerInformation);
                final String finalizedTimerInformation = timerInformation;
                timerTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        String newStr = timerTextView.getText() + finalizedTimerInformation;
                        timerTextView.setText(newStr);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}

