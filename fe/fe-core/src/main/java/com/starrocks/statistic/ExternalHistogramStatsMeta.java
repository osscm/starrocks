// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starrocks.statistic;

import com.google.gson.annotations.SerializedName;
import com.starrocks.common.io.Text;
import com.starrocks.common.io.Writable;
import com.starrocks.persist.gson.GsonUtils;

import java.io.DataInput;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class ExternalHistogramStatsMeta implements Writable {
    @SerializedName("catalogName")
    private String catalogName;
    @SerializedName("dbName")
    private String dbName;

    @SerializedName("tableName")
    private String tableName;

    @SerializedName("column")
    private String column;

    @SerializedName("type")
    private StatsConstants.AnalyzeType type;

    @SerializedName("updateTime")
    private LocalDateTime updateTime;

    @SerializedName("properties")
    private Map<String, String> properties;

    public ExternalHistogramStatsMeta(String catalogName, String dbName, String tableName, String column,
                                      StatsConstants.AnalyzeType type,
                                      LocalDateTime updateTime,
                                      Map<String, String> properties) {
        this.catalogName = catalogName;
        this.dbName = dbName;
        this.tableName = tableName;
        this.column = column;
        this.type = type;
        this.updateTime = updateTime;
        this.properties = properties;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumn() {
        return column;
    }

    public StatsConstants.AnalyzeType getType() {
        return type;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public static ExternalHistogramStatsMeta read(DataInput in) throws IOException {
        String s = Text.readString(in);
        return GsonUtils.GSON.fromJson(s, ExternalHistogramStatsMeta.class);
    }
}
