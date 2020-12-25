package utilty;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author Ahmad kisswani
 */
public class Global {

    public final static int minArgSize = 23;
    public final static Object nullVal = null;
    public final static int null_int_Val = 0;
    public final static double null_double_Val = 0.0;
    private String youtubeUrlField;
    public URL downloadURL;
    private String fileName;

    public Global() {
        try {
            downloadURL = new URL(getURLS(sendHTTPRequest.getValue()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setYoutubeUrlField(String youtubeUrlField) {
        this.youtubeUrlField = youtubeUrlField;
    }

    public String getYoutubeUrlField() {
        return youtubeUrlField;
    }

    final private Service< StringBuilder> sendHTTPRequest = new Service< StringBuilder>() {
        @Override
        protected Task< StringBuilder> createTask() {
            return new Task< StringBuilder>() {
                @Override
                protected StringBuilder call() {
                    String response;
                    StringBuilder res = new StringBuilder();
                    StringBuilder refinedres = new StringBuilder();
                    try {
                        URL url = new URL("https://www.youtube.com/get_video_info?&video_id=" + getVideoID(getYoutubeUrlField()));
                        System.out.println(url.toString());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        System.out.println(conn.getResponseMessage());
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((response = in.readLine()) != null) {
                            res.append(response);
                        }
                        refinedres.append(URLDecoder.decode(URLDecoder.decode(res.toString(), "UTF-8"), "UTF-8"));
                        in.close();
                        return refinedres;
                    } catch (MalformedURLException ex) {
                    } catch (IOException ex) {
                    }
                    return null;
                }
            };
        }
    };

    public Service<StringBuilder> getSendHTTPRequest() {
        return sendHTTPRequest;
    }

    public Service<Boolean> getVideoDownload() {
        return VideoDownload;
    }

    /*Method to extract the video id from the url.  
 if the url does not contain 'v=' parameter  
 then it will not work. It will accept only  
 standard url*/
    private String getVideoID(String url) {
        int index = url.indexOf("v=");
        String id = "";
        index += 2;
        for (int i = index; i < url.length(); i++) {
            id += url.charAt(i);
        }
        return id;
    }

    Service< Boolean> VideoDownload = new Service< Boolean>() {
        @Override
        protected Task< Boolean> createTask() {
            return new Task< Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    long length;
                    boolean completed = false;
                    int count = 0;
                    try (BufferedInputStream bis = new BufferedInputStream(downloadURL.openStream()); FileOutputStream fos = new FileOutputStream(getFileName().length() == 0 ? "video.mp4" : getFileName().concat(".mp4"))) {
                        length = downloadURL.openConnection().getContentLength();
                        int i = 0;
                        final byte[] data = new byte[1024];
                        while ((count = bis.read(data)) != -1) {
                            i += count;
                            fos.write(data, 0, count);
                            updateProgress(i, length);
                        }
                        completed = true;
                    } catch (IOException ex) {
                    }
                    return completed;
                }
            };
        }
    };

    /*This methid receives refined response as a paramter and extract the url from the  
 response which will be used to download the video from the youtube*/
    private String getURLS(StringBuilder response) {
        StringBuilder temp1 = new StringBuilder();
        String[] temp2, temp3, temp4;
        try {
            int index = response.indexOf("url_encoded_fmt_stream_map");
            for (int i = index; i < response.length(); i++) {
                temp1.append(response.charAt(i));
            }
            temp2 = temp1.toString().split("&url=");
            if (temp2.length > 0) {
                temp3 = temp2[1].split(";");
                if (temp3.length > 0) {
                    temp4 = temp3[0].split(",");
                    if (temp4.length > 0) {
                        return temp4[0];
                    } else {
                        return temp3[0];
                    }
                } else {
                    return temp2[1];
                }
            }
        } catch (Exception e) {
            Alert msg = new Alert(AlertType.INFORMATION);
            msg.setTitle("Message form youtube Downloader");
            msg.setContentText("Error in downloading");
            msg.showAndWait();
        }
        return null;
    }

}
