/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapred;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StopSkewedTaskAction extends TaskTrackerAction {

	private TaskAttemptID taskid;
	private long stopSign = 0;
	
	public StopSkewedTaskAction() {
		super(TaskTrackerAction.ActionType.STOP_SIGN);
		taskid = new TaskAttemptID();
	}

	public StopSkewedTaskAction(TaskAttemptID taskid, long stopSign){
		super(TaskTrackerAction.ActionType.STOP_SIGN);
		this.taskid = taskid;
		this.stopSign = stopSign;
	}
	
	public TaskAttemptID getTaskID(){
		return taskid;
	}
	public long getStopSign(){
		return stopSign;
	}
	public void setTaskID(TaskAttemptID taskid){
		this.taskid = taskid;
	}
	public void setStopSign(long stopSign){
		this.stopSign = stopSign;
	}

	public void write(DataOutput out) throws IOException {
		taskid.write(out);
		out.writeLong(stopSign);
	}

	public void readFields(DataInput in) throws IOException {
		taskid.readFields(in);
		stopSign = in.readLong();	
	}
}
