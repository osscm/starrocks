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

package com.starrocks.planner;

import com.starrocks.common.FeConstants;
import com.starrocks.common.util.UUIDUtil;
import com.starrocks.qe.ConnectContext;
import com.starrocks.thrift.TExplainLevel;
import com.starrocks.utframe.StarRocksAssert;
import com.starrocks.utframe.UtFrameUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PreAggregationTest {
    private static ConnectContext connectContext;
    private static StarRocksAssert starRocksAssert;
    private static String DB_NAME = "test";

    @BeforeClass
    public static void beforeClass() throws Exception {
        UtFrameUtils.createMinStarRocksCluster();

        FeConstants.enablePruneEmptyOutputScan = false;

        // create connect context
        connectContext = UtFrameUtils.createDefaultCtx();
        connectContext.setQueryId(UUIDUtil.genUUID());
        starRocksAssert = new StarRocksAssert(connectContext);
        starRocksAssert.withDatabase(DB_NAME).useDatabase(DB_NAME);

        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `test_agg_2` (\n" +
                "  `k1` int(11) NULL,\n" +
                "  `k2` int(11) NULL,\n" +
                "  `k3` int(11) NULL,\n" +
                "  `v1` int SUM NULL,\n" +
                "  `v2` bigint SUM NULL,\n" +
                "  `v3` largeint SUM NULL,\n" +
                "  `v4` double SUM NULL,\n" +
                "  `v5` decimal(10, 3) SUM NULL\n" +
                ") ENGINE=OLAP\n" +
                "AGGREGATE KEY(`k1`, `k2`, `k3`)\n" +
                "DISTRIBUTED BY HASH(`k2`) BUCKETS 10\n" +
                "PROPERTIES (\n" +
                " \"replication_num\" = \"1\"\n" +
                ");");

        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `test_agg_3` (\n" +
                "  `k1` int(11) NULL,\n" +
                "  `v1` int SUM NULL\n" +
                ") ENGINE=OLAP\n" +
                "AGGREGATE KEY(`k1`)\n" +
                "DISTRIBUTED BY HASH(`k1`) BUCKETS 1\n" +
                "PROPERTIES (\n" +
                " \"replication_num\" = \"1\"\n" +
                ");");
    }

    public String getFragmentPlan(String sql) throws Exception {
        return UtFrameUtils.getPlanAndFragment(connectContext, sql).second.
                getExplainString(TExplainLevel.NORMAL);
    }

    public static void assertContains(String text, String... pattern) {
        for (String s : pattern) {
            Assert.assertTrue(text, text.contains(s));
        }
    }

    @Test
    public void testPreAggregationCaseWhen() throws Exception {
        String sql = "select sum(case when k1 = 1 then v1 else +0 end), " +
                           " sum(case when k1 = 1 then v1 else -0 end), " +
                           " sum(case when k1 = 1 then v2 else +0 end), " +
                           " sum(case when k1 = 1 then v2 else -0 end), " +
                           " sum(case when k1 = 1 then v3 else +0 end), " +
                           " sum(case when k1 = 1 then v3 else -0 end), " +
                           " sum(case when k1 = 1 then +0 else v1 end), " +
                           " sum(case when k1 = 1 then -0 else v1 end), " +
                           " sum(case when k1 = 1 then +0 else v2 end), " +
                           " sum(case when k1 = 1 then -0 else v2 end), " +
                           " sum(case when k1 = 1 then +0 else v3 end), " +
                           " sum(case when k1 = 1 then -0 else v3 end) from test_agg_2";
        String plan = getFragmentPlan(sql);
        assertContains(plan, "0:OlapScanNode\n" +
                "     TABLE: test_agg_2\n" +
                "     PREAGGREGATION: ON\n");

        sql = "select sum(case when k1 = 1 then v1 else 0.0 end) from test_agg_2";
        plan = getFragmentPlan(sql);
        assertContains(plan, "0:OlapScanNode\n" +
                "     TABLE: test_agg_2\n" +
                "     PREAGGREGATION: OFF. Reason: The result of THEN isn't value column\n");

        sql = "select sum(case when k1 = 1 then v4 else +0.0 end), " +
                    " sum(case when k1 = 1 then v4 else -0.0 end), " +
                    " sum(case when k1 = 1 then +0.0 else v4 end), " +
                    " sum(case when k1 = 1 then -0.0 else v4 end) from test_agg_2";
        plan = getFragmentPlan(sql);
        assertContains(plan, "0:OlapScanNode\n" +
                "     TABLE: test_agg_2\n" +
                "     PREAGGREGATION: ON\n");

        sql = "select sum(case when k1 = 1 then v5 else +0.0 end), " +
                    " sum(case when k1 = 1 then v5 else -0.0 end), " +
                    " sum(case when k1 = 1 then +0.0 else v5 end), " +
                    " sum(case when k1 = 1 then -0.0 else v5 end) from test_agg_2";
        plan = getFragmentPlan(sql);
        assertContains(plan, "0:OlapScanNode\n" +
                "     TABLE: test_agg_2\n" +
                "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testMetricTypeOfAggTableNotMatchAggragationReturnType() throws Exception {
        String sql = "select \n" +
                "col1, \n" +
                "col2 \n" +
                "from (\n" +
                "select k1 col1, \n" +
                "IFNULL(SUM(v1),0) col2 \n" +
                "from test_agg_3 \n" +
                "group by k1\n" +
                ")tmp \n" +
                "where 1=1 and col2 > 1 \n" +
                "order by col1 \n" +
                "asc limit 0,5";
        String plan = getFragmentPlan(sql);
        assertContains(plan, "  1:Project\n" +
                "  |  <slot 1> : 1: k1\n" +
                "  |  <slot 4> : ifnull(CAST(2: v1 AS BIGINT), 0)");
    }
}
