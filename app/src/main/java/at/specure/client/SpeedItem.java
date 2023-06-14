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
package at.specure.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SpeedItem
{
    public final String direction;
    public final int thread;
    public final long time;
    public final long bytes;
    
    public SpeedItem(final boolean upload, final int thread, final long time, final long bytes)
    {
        this.direction = upload ? "upload" : "download";
        this.thread = thread;
        this.time = time;
        this.bytes = bytes;
    }

	@Override
	public String toString() {
		return "SpeedItem [direction=" + direction + ", thread=" + thread + ", time="
				+ time + ", bytes=" + bytes + "]";
	}
}
