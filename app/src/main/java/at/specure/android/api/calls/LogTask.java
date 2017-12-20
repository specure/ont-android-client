/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.specure.android.api.calls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.adapter.result.OnCompleteListener;

public class LogTask extends AsyncTask<String, Void, Void>
{
	private final static String LOGFILE_PREFIX = "com.specure.nettest";
		
    private final MainActivity activity;
    
    ControlServerConnection serverConn;
    
    private OnCompleteListener onCompleteListener;   
    
    public LogTask(final MainActivity activity, final OnCompleteListener listener)
    {
        this.activity = activity;
        this.onCompleteListener = listener;
    }
    
    /**
     * 
     * @param listener
     */
    public void setOnCompleteListener(OnCompleteListener listener) {
    	this.onCompleteListener = listener;
    }
    
    @Override
    protected Void doInBackground(final String... params)
    {
    	try {
	        serverConn = new ControlServerConnection(activity);
	
	        final List<File> logFiles = new ArrayList<File>(); 
	        
	        if (params == null || params.length == 0) {
	    		File f = new File(Environment.getExternalStorageDirectory(), "qosdebug");
	            final File[] logs = f.listFiles();
	            
	            if (logs != null) {
		            for (File l : logs) {
		            	if (l.length() > 0) {
		            		logFiles.add(l);
		            	}
		            	else {
		            		//delete old empty log file
		            		l.delete();
		            	}
		            }
	            }
	        }
	        else {
	        	for (String fileName : params) {
	        		File f = new File(fileName);
	        		if (f.exists() && f.length() > 0) {
	        			logFiles.add(f);
	        		}
	        	}
	        }
	        
	        System.out.println("log files found: " + logFiles);
	        
	        if (logFiles.size() > 0) {
	        	for (File logFile : logFiles) {
					System.out.println("Sending file: " + logFile.getAbsolutePath());
					Scanner s = null;
					try {
						BufferedReader br = new BufferedReader(new FileReader(logFile));
						try {
						    StringBuilder sb = new StringBuilder();
						    String line = br.readLine();
						
						    while (line != null) {
						        sb.append(line);
						        sb.append("\n");
						        line = br.readLine();
						    }
							Gson gson = new Gson();
							final JsonObject requestData = new JsonObject();
							requestData.add("content", gson.toJsonTree(sb.toString()));
							requestData.add("logfile",  gson.toJsonTree(LOGFILE_PREFIX + "_" + ConfigHelper.getUUID(activity) + "_" + logFile.getName()));
							final JsonObject fileTimes = new JsonObject();
							fileTimes.add("last_access",  gson.toJsonTree(TimeUnit.SECONDS.convert(logFile.lastModified(), TimeUnit.MILLISECONDS)));
							fileTimes.add("last_modified",  gson.toJsonTree(TimeUnit.SECONDS.convert(logFile.lastModified(), TimeUnit.MILLISECONDS)));
							fileTimes.add("created",  gson.toJsonTree(TimeUnit.SECONDS.convert(logFile.lastModified(), TimeUnit.MILLISECONDS)));
							requestData.add("file_times", fileTimes);
							JsonArray result = serverConn.sendLogReport(requestData);
							if (result != null) {
								final String resultStatus = result.get(0).getAsJsonObject().get("status").getAsString();
								if ("OK".equals(resultStatus.toUpperCase(Locale.US))) {
									System.out.println("Log file sent successfully. Deleting.");
									br.close();
									logFile.delete();
								}
							}
						} finally {
						    br.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (s != null) {
							s.close();
						}
					}
	        	}
	        }
    	}
    	catch (Throwable t) {
    		t.printStackTrace();
    	}
        
        return null;
    }
    
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    @Override
    protected void onPostExecute(Void result)
    {
    	if (onCompleteListener != null) {
    		onCompleteListener.onComplete(0, result);
    	}
    }
    
}
