// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.persist;

import com.google.gson.annotations.SerializedName;
import com.starrocks.alter.AlterJobV2;
import com.starrocks.common.io.Text;
import com.starrocks.common.io.Writable;
import com.starrocks.persist.gson.GsonUtils;

import java.io.DataInput;
import java.io.IOException;

// edit log from removing alter job v2
public class RemoveAlterJobV2OperationLog implements Writable {

    @SerializedName(value = "jobId")
    private long jobId;
    @SerializedName(value = "type")
    private AlterJobV2.JobType type;

    public RemoveAlterJobV2OperationLog(long jobId, AlterJobV2.JobType type) {
        this.jobId = jobId;
        this.type = type;
    }

    public long getJobId() {
        return jobId;
    }

    public AlterJobV2.JobType getType() {
        return type;
    }



    public static RemoveAlterJobV2OperationLog read(DataInput in) throws IOException {
        String json = Text.readString(in);
        return GsonUtils.GSON.fromJson(json, RemoveAlterJobV2OperationLog.class);
    }

}
