package com.example.accelv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.opencsv.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {
    private TextView xValueText;
    private TextView yValueText;
    private TextView zValueText;
    private Button stopButton;
    ArrayList<String> savedData = new ArrayList<String>();
    private Boolean stopped = false;
    private Boolean saved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextViews
        xValueText = (TextView) findViewById(R.id.XVALUE);
        xValueText.setText("x = ");
        yValueText = (TextView) findViewById(R.id.YVALUE);
        yValueText.setText("y = ");
        zValueText = (TextView) findViewById(R.id.ZVALUE);
        zValueText.setText("z = ");

        // Get Data from watch
        Wearable.getDataClient(this).addListener(this);

        // Button to save data
        stopButton = findViewById(R.id.stopButton);

        // When we click our button, we want to record our data to a CSV and send it via email.
        // !!!Press only after all the data from our watch has been sent!!!
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                stopped = true;
                // http://opencsv.sourceforge.net/
                // Write to file using OPENCSV Library
                File saveFile = new File(getExternalFilesDir(null), "RecordedData.csv");
                try {
                    CSVWriter writer = new CSVWriter((new FileWriter(saveFile)),
                            CSVWriter.DEFAULT_SEPARATOR,
                            CSVWriter.NO_QUOTE_CHARACTER,
                            CSVWriter.NO_ESCAPE_CHARACTER,
                            CSVWriter.DEFAULT_LINE_END);
                    for (int i = 0; i < savedData.size(); i++){
                        writer.writeNext(savedData.get(i).split(","));
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // https://www.youtube.com/watch?v=VDAwbgHoYEA
                // This portion emails our file
                Uri path = FileProvider.getUriForFile(getApplicationContext(), "com.example.accelv3.fileprovider", saveFile);
                Intent fileIntent = new Intent(Intent.ACTION_SEND);
                fileIntent.setType("text/csv");
                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                startActivity(Intent.createChooser(fileIntent, "Send mail"));
            }
        });
    }



    /**
     * This method updates when we get data from out watch!
     * https://developer.android.com/training/wearables/data-layer/data-items.html#SyncData
     * @param dataEvents This object is transfered from the watch, holds our data
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (stopped == false) {
            for (DataEvent event : dataEvents) {                                       // Loop through events in our DataEventBuffer
                if (event.getType() == DataEvent.TYPE_CHANGED) {                       // If the data in the event has changed,
                    DataItem item = event.getDataItem();
                    if (item.getUri().getPath().compareTo("/accelData") == 0) {        // Check if it's the data you're looking for
                        DataMap dataFromWatch = DataMapItem.fromDataItem(item).getDataMap(); // Create a deep copy of the data
                        xValueText.setText("x = " + dataFromWatch.getStringArray("ThisIsTheKey")[0]);
                        yValueText.setText("y = " + dataFromWatch.getStringArray("ThisIsTheKey")[1]);
                        zValueText.setText("z = " + dataFromWatch.getStringArray("ThisIsTheKey")[2]);
                        //Log.d("DataFromWatch", "time in nanoseconds: " + dataFromWatch.getStringArray("ThisIsTheKey")[3]);

                        // Formatting our data into a CSV
                        int count = 0;
                        String tempString = "";
                        for (int i = 0; i < dataFromWatch.getStringArray("ThisIsTheKey").length; i++ ){
                            //Log.d("READING_DATA", dataFromWatch.getStringArray("ThisIsTheKey")[i]);
                            if (count == 3){
                                tempString+=dataFromWatch.getStringArray("ThisIsTheKey")[i];
                                savedData.add(tempString);
                                tempString = "";
                                count = 0;
                            } else{
                                tempString+=dataFromWatch.getStringArray("ThisIsTheKey")[i]+",";
                                count++;
                            }

                        }
                    }
                } else if (event.getType() == DataEvent.TYPE_DELETED) {                // Don't really need this, but we deal with the
                    // DataItem deleted                                                // data item being deleted here
                }
            }
        }
    }
}
