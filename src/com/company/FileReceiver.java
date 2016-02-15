package com.company;

import com.company.Util.ChatUtils;
import com.company.Util.Constants;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileReceiver extends SwingWorker<Void, String> {
    private final static int PORT = 1338;
    private String filename;
    private ChatClient client;
    private long size;
    private final static int BUFF_SIZE = 8*1024;

    public FileReceiver(String filename, String size, ChatClient client) {
        this.filename = filename;
        this.client = client;
        this.size = Long.parseLong(size);
    }

    @Override
    protected Void doInBackground() throws IOException {
        Socket socket = new Socket(client.getAddress(), PORT);
        InputStream in = socket.getInputStream();

        File downloadDir = new File(Constants.DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdir();
        }
        FileOutputStream fout = new FileOutputStream(Constants.DOWNLOAD_PATH + "/" + filename);

        try {
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            long startTime = System.nanoTime();
            long count = 0;
            while ((len = in.read(buff)) != -1) {
                count += len;
                if (count % (1024*2) == 0 || count == size) {
                    //Using publish to make a gui update
                    publish("Downloading: " + ChatUtils.getPercent(count, size) + " %" +
                            ChatUtils.getMegabyteDifference(count, size) + ChatUtils.getDownloadRate(startTime, count) + " - [" + filename + "]");
                }
                fout.write(buff, 0, len);
            }
            fout.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            fout.close();
            socket.close();
        }
        return null;
    }
    @Override
    protected void process(final List<String> chunks) {
        for (final String item : chunks) {
            client.setTitle(item);
        }
    }
    @Override
    protected void done() {
        client.setTitle("fmIRC+ - " + client.getNickname());
    }
}
