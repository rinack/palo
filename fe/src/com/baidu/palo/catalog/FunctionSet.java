// Modifications copyright (C) 2017, Baidu.com, Inc.
// Copyright 2017 The Apache Software Foundation

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

package com.baidu.palo.catalog;

import com.baidu.palo.analysis.ArithmeticExpr;
import com.baidu.palo.analysis.BinaryPredicate;
import com.baidu.palo.analysis.CastExpr;
import com.baidu.palo.analysis.InPredicate;
import com.baidu.palo.analysis.IsNullPredicate;
import com.baidu.palo.analysis.LikePredicate;
import com.baidu.palo.builtins.ScalarBuiltins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaochun on 15/10/28.
 */
public class FunctionSet {
    private static final Logger LOG = LogManager.getLogger(FunctionSet.class);

    // All of the registered user functions. The key is the user facing name (e.g. "myUdf"),
    // and the values are all the overloaded variants (e.g. myUdf(double), myUdf(string))
    // This includes both UDFs and UDAs. Updates are made thread safe by synchronizing
    // on this map. Functions are sorted in a canonical order defined by
    // FunctionResolutionOrder.
    private final HashMap<String, List<Function>> functions;

    public FunctionSet() {
        functions = Maps.newHashMap();
    }

    public void init() {
        // Populate all aggregate builtins.
        initAggregateBuiltins();

        ArithmeticExpr.initBuiltins(this);
        BinaryPredicate.initBuiltins(this);
        CastExpr.initBuiltins(this);
        IsNullPredicate.initBuiltins(this);
        ScalarBuiltins.initBuiltins(this);
        LikePredicate.initBuiltins(this);
        InPredicate.initBuiltins(this);
    }

    private static final Map<Type, String> MIN_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                    "3minIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.TINYINT,
                    "3minIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.SMALLINT,
                    "3minIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.INT,
                    "3minIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.BIGINT,
                    "3minIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.FLOAT,
                    "3minIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DOUBLE,
                    "3minIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PS6_")
               // .put(Type.CHAR,
               //     "3minIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.VARCHAR,
                    "3minIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATE,
                    "3minIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATETIME,
                    "3minIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DECIMAL,
                    "3minIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.LARGEINT,
                    "3minIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .build();

    private static final Map<Type, String> MAX_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                    "3maxIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.TINYINT,
                    "3maxIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.SMALLINT,
                    "3maxIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.INT,
                    "3maxIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.BIGINT,
                    "3maxIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.FLOAT,
                    "3maxIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DOUBLE,
                    "3maxIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PS6_")
                // .put(Type.CHAR,
                //    "3maxIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.VARCHAR,
                        "3maxIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATE,
                    "3maxIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATETIME,
                    "3maxIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DECIMAL,
                    "3maxIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.LARGEINT,
                    "3maxIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .build();

    private static final Map<Type, String> STDDEV_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.TINYINT,
                        "16knuth_var_updateIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.SMALLINT,
                        "16knuth_var_updateIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.INT,
                        "16knuth_var_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.BIGINT,
                        "16knuth_var_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.FLOAT,
                        "16knuth_var_updateIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.DOUBLE,
                        "16knuth_var_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .build();

    private static final Map<Type, String> HLL_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                    "10hll_updateIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.TINYINT,
                    "10hll_updateIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.SMALLINT,
                    "10hll_updateIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.INT,
                    "10hll_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.BIGINT,
                    "10hll_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.FLOAT,
                    "10hll_updateIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.DOUBLE,
                    "10hll_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                // .put(Type.CHAR,
                //    "10hll_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS3_")
                .put(Type.VARCHAR,
                    "10hll_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS3_")
                .put(Type.DATE,
                    "10hll_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.DATETIME,
                    "10hll_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.DECIMAL,
                    "10hll_updateIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .put(Type.LARGEINT,
                    "10hll_updateIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE")
                .build();
   

    private static final Map<Type, String> HLL_UNION_AGG_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.VARCHAR,
                    "20hll_union_agg_updateEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_")
                .put(Type.HLL,
                    "20hll_union_agg_updateEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_")
                .build();
 
    private static final Map<Type, String> OFFSET_FN_INIT_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "14offset_fn_initIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.DECIMAL,
                     "14offset_fn_initIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.TINYINT,
                     "14offset_fn_initIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.SMALLINT,
                     "14offset_fn_initIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.DATE,
                     "14offset_fn_initIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.DATETIME,
                     "14offset_fn_initIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.INT,
                     "14offset_fn_initIN8palo_udf6IntValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.FLOAT,
                     "14offset_fn_initIN8palo_udf8FloatValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.BIGINT,
                     "14offset_fn_initIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.DOUBLE,
                     "14offset_fn_initIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextEPT_")
                // .put(Type.CHAR,
                //     "14offset_fn_initIN8palo_udf9StringValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.VARCHAR,
                     "14offset_fn_initIN8palo_udf9StringValEEEvPNS2_15FunctionContextEPT_")
                .put(Type.LARGEINT,
                     "14offset_fn_initIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextEPT_")

                .build();

    private static final Map<Type, String> OFFSET_FN_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "16offset_fn_updateIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.DECIMAL,
                     "16offset_fn_updateIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.TINYINT,
                     "16offset_fn_updateIN8palo_udf10TinyIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.SMALLINT,
                     "16offset_fn_updateIN8palo_udf11SmallIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.DATE,
                     "16offset_fn_updateIN8palo_udf11DateTimeValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.DATETIME,
                     "16offset_fn_updateIN8palo_udf11DateTimeValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.INT,
                     "16offset_fn_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.FLOAT,
                     "16offset_fn_updateIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.BIGINT,
                     "16offset_fn_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_RKS3_S8_PS6_")
                .put(Type.DOUBLE,
                     "16offset_fn_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                // .put(Type.CHAR,
                //     "16offset_fn_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.VARCHAR,
                     "16offset_fn_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .put(Type.LARGEINT,
                     "16offset_fn_updateIN8palo_udf11LargeIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValES8_PS6_")
                .build();

    private static final Map<Type, String> LAST_VALUE_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "15last_val_updateIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DECIMAL,
                     "15last_val_updateIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.TINYINT,
                     "15last_val_updateIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.SMALLINT,
                     "15last_val_updateIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATE,
                     "15last_val_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATETIME,
                     "15last_val_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.INT,
                     "15last_val_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.FLOAT,
                     "15last_val_updateIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.BIGINT,
                     "15last_val_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DOUBLE,
                     "15last_val_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PS6_")
                // .put(Type.CHAR,
                //     "15last_val_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.VARCHAR,
                     "15last_val_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.LARGEINT,
                     "15last_val_updateIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .build();

    private static final Map<Type, String> FIRST_VALUE_REWRITE_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "24first_val_rewrite_updateIN8palo_udf10BooleanValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.DECIMAL,
                     "24first_val_rewrite_updateIN8palo_udf10DecimalValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.TINYINT,
                     "24first_val_rewrite_updateIN8palo_udf10TinyIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.SMALLINT,
                     "24first_val_rewrite_updateIN8palo_udf11SmallIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.DATE,
                     "24first_val_rewrite_updateIN8palo_udf11DateTimeValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.DATETIME,
                     "24first_val_rewrite_updateIN8palo_udf11DateTimeValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.INT,
                     "24first_val_rewrite_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.FLOAT,
                     "24first_val_rewrite_updateIN8palo_udf8FloatValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.BIGINT,
                     "24first_val_rewrite_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_RKS3_PS6_")
                .put(Type.DOUBLE,
                     "24first_val_rewrite_updateIN8palo_udf9DoubleValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.VARCHAR,
                     "24first_val_rewrite_updateIN8palo_udf9StringValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                .put(Type.LARGEINT,
                     "24first_val_rewrite_updateIN8palo_udf11LargeIntValEEEvPNS2_15"
                     + "FunctionContextERKT_RKNS2_9BigIntValEPS6_")
                // .put(Type.VARCHAR,
                //     "15last_val_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .build();

    private static final Map<Type, String> LAST_VALUE_REMOVE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "15last_val_removeIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DECIMAL,
                     "15last_val_removeIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.TINYINT,
                     "15last_val_removeIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.SMALLINT,
                     "15last_val_removeIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATE,
                     "15last_val_removeIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATETIME,
                     "15last_val_removeIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.INT,
                     "15last_val_removeIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.FLOAT,
                     "15last_val_removeIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.BIGINT,
                     "15last_val_removeIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DOUBLE,
                     "15last_val_removeIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PS6_")
                // .put(Type.CHAR,
                //     "15last_val_removeIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.VARCHAR,
                     "15last_val_removeIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.LARGEINT,
                     "15last_val_removeIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .build();

    private static final Map<Type, String> FIRST_VALUE_UPDATE_SYMBOL =
        ImmutableMap.<Type, String>builder()
                .put(Type.BOOLEAN,
                     "16first_val_updateIN8palo_udf10BooleanValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DECIMAL,
                     "16first_val_updateIN8palo_udf10DecimalValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.TINYINT,
                     "16first_val_updateIN8palo_udf10TinyIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.SMALLINT,
                     "16first_val_updateIN8palo_udf11SmallIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATE,
                     "16first_val_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DATETIME,
                     "16first_val_updateIN8palo_udf11DateTimeValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.INT,
                     "16first_val_updateIN8palo_udf6IntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.FLOAT,
                     "16first_val_updateIN8palo_udf8FloatValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.BIGINT,
                     "16first_val_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.DOUBLE,
                     "16first_val_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PS6_")
                // .put(Type.CHAR,
                //     "16first_val_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.VARCHAR,
                     "16first_val_updateIN8palo_udf9StringValEEEvPNS2_15FunctionContextERKT_PS6_")
                .put(Type.LARGEINT,
                     "16first_val_updateIN8palo_udf11LargeIntValEEEvPNS2_15FunctionContextERKT_PS6_")

                .build();

    public Function getFunction(Function desc, Function.CompareMode mode) {
        List<Function> fns = functions.get(desc.functionName());
        if (fns == null) {
            return null;
        }

        // First check for identical
        for (Function f : fns) {
            if (f.compare(desc, Function.CompareMode.IS_IDENTICAL)) {
                return f;
            }
        }
        if (mode == Function.CompareMode.IS_IDENTICAL) {
            return null;
        }

        // Next check for indistinguishable
        for (Function f : fns) {
            if (f.compare(desc, Function.CompareMode.IS_INDISTINGUISHABLE)) {
                return f;
            }
        }
        if (mode == Function.CompareMode.IS_INDISTINGUISHABLE) {
            return null;
        }

        // Next check for strict supertypes
        for (Function f : fns) {
            if (f.compare(desc, Function.CompareMode.IS_SUPERTYPE_OF)) {
                return f;
            }
        }
        if (mode == Function.CompareMode.IS_SUPERTYPE_OF) {
            return null;
        }

        // Finally check for non-strict supertypes
        for (Function f : fns) {
            if (f.compare(desc, Function.CompareMode.IS_NONSTRICT_SUPERTYPE_OF)) {
                return f;
            }
        }
        return null;
    }

    public Function getFunction(String signatureString) {
        for (List<Function> fns : functions.values()) {
            for (Function f : fns) {
                if (f.signatureString().equals(signatureString)) {
                    return f;
                }
            }
        }
        return null;
    }

    // Only used
    public boolean addFunction(Function fn) {
        // TODO: add this to persistent store
        if (getFunction(fn, Function.CompareMode.IS_INDISTINGUISHABLE) != null) {
            return false;
        }
        List<Function> fns = functions.get(fn.functionName());
        if (fns == null) {
            fns = Lists.newArrayList();
            functions.put(fn.functionName(), fns);
        }
        if (fns.add(fn)) {
            return true;
        }
        return false;
    }

    /**
     * Add a builtin with the specified name and signatures to this
     * This defaults to not using a Prepare/Close function.
     */
    public void addScalarBuiltin(String fnName, String symbol, boolean userVisible,
                                 boolean varArgs, PrimitiveType retType, PrimitiveType ... args) {
        addScalarBuiltin(fnName, symbol, userVisible, null, null, varArgs, retType, args);
    }

    /**
     * Add a builtin with the specified name and signatures to this db.
     */
    public void addScalarBuiltin(String fnName, String symbol, boolean userVisible,
                                 String prepareFnSymbol, String closeFnSymbol, boolean varArgs,
                                 PrimitiveType retType, PrimitiveType ... args) {
        ArrayList<Type> argsType = new ArrayList<Type>();
        for (PrimitiveType type : args) {
            argsType.add(Type.fromPrimitiveType(type));
        }
        addBuiltin(ScalarFunction.createBuiltin(
                fnName, argsType, varArgs, Type.fromPrimitiveType(retType),
                symbol, prepareFnSymbol, closeFnSymbol, userVisible));
    }

    /**
     * Adds a builtin to this database. The function must not already exist.
     */
    public void addBuiltin(Function fn) {
        addFunction(fn);
    }

    // Populate all the aggregate builtins in the catalog.
    // null symbols indicate the function does not need that step of the evaluation.
    // An empty symbol indicates a TODO for the BE to implement the function.
    private void initAggregateBuiltins() {
        final String prefix = "_ZN4palo18AggregateFunctions";
        final String initNull = prefix + "9init_nullEPN8palo_udf15FunctionContextEPNS1_6AnyValE";
        final String initNullString = prefix
                + "16init_null_stringEPN8palo_udf15FunctionContextEPNS1_9StringValE";
        final String stringValSerializeOrFinalize = prefix
                + "32string_val_serialize_or_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE";
        final String stringValGetValue = prefix
                + "20string_val_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE";

        // Type stringType[] = {Type.CHAR, Type.VARCHAR};
        // count(*)
        addBuiltin(AggregateFunction.createBuiltin("count",
            new ArrayList<Type>(), Type.BIGINT, Type.BIGINT,
            prefix + "9init_zeroIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextEPT_",
            prefix + "17count_star_updateEPN8palo_udf15FunctionContextEPNS1_9BigIntValE",
            prefix + "11count_mergeEPN8palo_udf15FunctionContextERKNS1_9BigIntValEPS4_",
            null, null,
            prefix + "17count_star_removeEPN8palo_udf15FunctionContextEPNS1_9BigIntValE",
            null, false, true, true));

        for (Type t : Type.getSupportedTypes()) {
            if (t.isNull()) {
                continue; // NULL is handled through type promotion.
            }
            if (t.isScalarType(PrimitiveType.CHAR)) {
                continue; // promoted to STRING
            }
            // Count
            addBuiltin(AggregateFunction.createBuiltin("count",
                    Lists.newArrayList(t), Type.BIGINT, Type.BIGINT,
                    prefix + "9init_zeroIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextEPT_",
                    prefix + "12count_updateEPN8palo_udf15FunctionContextERKNS1_6AnyValEPNS1_9BigIntValE",
                    prefix + "11count_mergeEPN8palo_udf15FunctionContextERKNS1_9BigIntValEPS4_",
                    null, null,
                    prefix + "12count_removeEPN8palo_udf15FunctionContextERKNS1_6AnyValEPNS1_9BigIntValE",
                    null, false, true, true));

            // Count_distinct
            addBuiltin(AggregateFunction.createBuiltin("count_distinct",
                    Lists.newArrayList(t), Type.BIGINT, Type.BIGINT,
                    prefix + "9init_zeroIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextEPT_",
                    prefix + "12count_updateEPN8palo_udf15FunctionContextERKNS1_6AnyValEPNS1_9BigIntValE",
                    prefix + "11count_mergeEPN8palo_udf15FunctionContextERKNS1_9BigIntValEPS4_",
                    null, null,
                    prefix + "12count_removeEPN8palo_udf15FunctionContextERKNS1_6AnyValEPNS1_9BigIntValE",
                    null, false, true, true));

            // Min
            String minMaxInit = t.isStringType() ? initNullString : initNull;
            String minMaxSerializeOrFinalize = t.isStringType() ? stringValSerializeOrFinalize : null;
            String minMaxGetValue = t.isStringType() ? stringValGetValue : null;
            addBuiltin(AggregateFunction.createBuiltin("min",
                    Lists.newArrayList(t), t, t, minMaxInit,
                    prefix + MIN_UPDATE_SYMBOL.get(t),
                    prefix + MIN_UPDATE_SYMBOL.get(t),
                    minMaxSerializeOrFinalize, minMaxGetValue,
                    null, minMaxSerializeOrFinalize, true, true, false));

            // Max
            addBuiltin(AggregateFunction.createBuiltin("max",
                    Lists.newArrayList(t), t, t, minMaxInit,
                    prefix + MAX_UPDATE_SYMBOL.get(t),
                    prefix + MAX_UPDATE_SYMBOL.get(t),
                    minMaxSerializeOrFinalize, minMaxGetValue,
                    null, minMaxSerializeOrFinalize, true, true, false));

            // NDV
            // ndv return string
            addBuiltin(AggregateFunction.createBuiltin("ndv",
                    Lists.newArrayList(t), Type.VARCHAR, Type.VARCHAR,
                    prefix + "8hll_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                    prefix + HLL_UPDATE_SYMBOL.get(t),
                    prefix + "9hll_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                    null,
                    prefix + "12hll_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                    true, false, true));

            // HLL_UNION_AGG
            addBuiltin(AggregateFunction.createBuiltin("hll_union_agg",
                    Lists.newArrayList(t), Type.VARCHAR, Type.VARCHAR,
                    prefix + "18hll_union_agg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                    prefix + HLL_UNION_AGG_UPDATE_SYMBOL.get(t),
                    prefix + "19hll_union_agg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                    null,
                    prefix + "22hll_union_agg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                    true, false, true));

            if (STDDEV_UPDATE_SYMBOL.containsKey(t)) {
                addBuiltin(AggregateFunction.createBuiltin("stddev",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "21knuth_stddev_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("stddev_samp",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "21knuth_stddev_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("stddev_pop",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "25knuth_stddev_pop_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("variance",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "18knuth_var_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("variance_samp",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "18knuth_var_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("var_samp",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "18knuth_var_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("variance_pop",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "22knuth_var_pop_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
                addBuiltin(AggregateFunction.createBuiltin("var_pop",
                        Lists.newArrayList(t), Type.DOUBLE, Type.VARCHAR,
                        prefix + "14knuth_var_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                        prefix + STDDEV_UPDATE_SYMBOL.get(t),
                        prefix + "15knuth_var_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                        null,
                        prefix + "22knuth_var_pop_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                        false, false, false));
            }
        }


        // Sum
        String []sumNames = {"sum", "sum_distinct"};
        for (String name : sumNames) {
            addBuiltin(AggregateFunction.createBuiltin(name,
                    Lists.<Type>newArrayList(Type.BIGINT), Type.BIGINT, Type.BIGINT, initNull,
                    prefix + "3sumIN8palo_udf9BigIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    prefix + "3sumIN8palo_udf9BigIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, null,
                    prefix + "10sum_removeIN8palo_udf9BigIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, false, true, false));
            addBuiltin(AggregateFunction.createBuiltin(name,
                    Lists.<Type>newArrayList(Type.DOUBLE), Type.DOUBLE, Type.DOUBLE, initNull,
                    prefix + "3sumIN8palo_udf9DoubleValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    prefix + "3sumIN8palo_udf9DoubleValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, null,
                    prefix + "10sum_removeIN8palo_udf9DoubleValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, false, true, false));
            addBuiltin(AggregateFunction.createBuiltin(name,
                    Lists.<Type>newArrayList(Type.DECIMAL), Type.DECIMAL, Type.DECIMAL, initNull,
                    prefix + "3sumIN8palo_udf10DecimalValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    prefix + "3sumIN8palo_udf10DecimalValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, null,
                    prefix + "10sum_removeIN8palo_udf10DecimalValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, false, true, false));
            addBuiltin(AggregateFunction.createBuiltin(name,
                    Lists.<Type>newArrayList(Type.LARGEINT), Type.LARGEINT, Type.LARGEINT, initNull,
                    prefix + "3sumIN8palo_udf11LargeIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    prefix + "3sumIN8palo_udf11LargeIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, null,
                    prefix + "10sum_removeIN8palo_udf11LargeIntValES3_EEvPNS2_15FunctionContextERKT_PT0_",
                    null, false, true, false));
        }


        // Avg
        // TODO: switch to CHAR(sizeof(AvgIntermediateType) when that becomes available
        addBuiltin(AggregateFunction.createBuiltin("avg",
                Lists.<Type>newArrayList(Type.BIGINT), Type.DOUBLE, Type.VARCHAR,
                prefix + "8avg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "10avg_updateIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE",
                prefix + "9avg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "13avg_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                prefix + "10avg_removeIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE",
                prefix + "12avg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, true, false));
        addBuiltin(AggregateFunction.createBuiltin("avg",
                Lists.<Type>newArrayList(Type.DOUBLE), Type.DOUBLE, Type.VARCHAR,
                prefix + "8avg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "10avg_updateIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE",
                prefix + "9avg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "13avg_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                prefix + "10avg_removeIN8palo_udf9DoubleValEEEvPNS2_15FunctionContextERKT_PNS2_9StringValE",
                prefix + "12avg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, true, false));
        addBuiltin(AggregateFunction.createBuiltin("avg",
                Lists.<Type>newArrayList(Type.DECIMAL), Type.DECIMAL, Type.VARCHAR,
                prefix + "16decimal_avg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "18decimal_avg_updateEPN8palo_udf15FunctionContextERKNS1_10DecimalValEPNS1_9StringValE",
                prefix + "17decimal_avg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "21decimal_avg_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                prefix + "18decimal_avg_removeEPN8palo_udf15FunctionContextERKNS1_10DecimalValEPNS1_9StringValE",
                prefix + "20decimal_avg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, true, false));
        // Avg(Timestamp)
        addBuiltin(AggregateFunction.createBuiltin("avg",
                Lists.<Type>newArrayList(Type.DATE), Type.DATE, Type.VARCHAR,
                prefix + "8avg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "20timestamp_avg_updateEPN8palo_udf15FunctionContextERKNS1_11DateTimeValEPNS1_9StringValE",
                prefix + "9avg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "23timestamp_avg_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                prefix + "20timestamp_avg_removeEPN8palo_udf15FunctionContextERKNS1_11DateTimeValEPNS1_9StringValE",
                prefix + "22timestamp_avg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, true, false));
        addBuiltin(AggregateFunction.createBuiltin("avg",
                Lists.<Type>newArrayList(Type.DATETIME), Type.DATETIME, Type.DATETIME,
                prefix + "8avg_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "20timestamp_avg_updateEPN8palo_udf15FunctionContextERKNS1_11DateTimeValEPNS1_9StringValE",
                prefix + "9avg_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "23timestamp_avg_get_valueEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                prefix + "20timestamp_avg_removeEPN8palo_udf15FunctionContextERKNS1_11DateTimeValEPNS1_9StringValE",
                prefix + "22timestamp_avg_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, true, false));
        // Group_concat(string)
        addBuiltin(AggregateFunction.createBuiltin("group_concat",
                Lists.<Type>newArrayList(Type.VARCHAR), Type.VARCHAR, Type.VARCHAR, initNullString,
                prefix + "20string_concat_updateEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                prefix + "19string_concat_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "22string_concat_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, false, false));
        // Group_concat(string, string)
        addBuiltin(AggregateFunction.createBuiltin("group_concat",
                Lists.<Type>newArrayList(Type.VARCHAR, Type.VARCHAR), Type.VARCHAR, Type.VARCHAR,
                initNullString,
                prefix + "20string_concat_updateEPN8palo_udf15FunctionContextERKNS1_9StringValES6_PS4_",
                prefix + "19string_concat_mergeEPN8palo_udf15FunctionContextERKNS1_9StringValEPS4_",
                stringValSerializeOrFinalize,
                prefix + "22string_concat_finalizeEPN8palo_udf15FunctionContextERKNS1_9StringValE",
                false, false, false));

        // analytic functions
        // Rank
        addBuiltin(AggregateFunction.createAnalyticBuiltin("rank",
                Lists.<Type>newArrayList(), Type.BIGINT, Type.VARCHAR,
                prefix + "9rank_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "11rank_updateEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                null,
                prefix + "14rank_get_valueEPN8palo_udf15FunctionContextERNS1_9StringValE",
                prefix + "13rank_finalizeEPN8palo_udf15FunctionContextERNS1_9StringValE"));
        // Dense rank
        addBuiltin(AggregateFunction.createAnalyticBuiltin("dense_rank",
                Lists.<Type>newArrayList(), Type.BIGINT, Type.VARCHAR,
                prefix + "9rank_initEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                prefix + "17dense_rank_updateEPN8palo_udf15FunctionContextEPNS1_9StringValE",
                null,
                prefix + "20dense_rank_get_valueEPN8palo_udf15FunctionContextERNS1_9StringValE",
                prefix + "13rank_finalizeEPN8palo_udf15FunctionContextERNS1_9StringValE"));
        addBuiltin(AggregateFunction.createAnalyticBuiltin( "row_number",
                new ArrayList<Type>(), Type.BIGINT, Type.BIGINT,
                prefix + "9init_zeroIN8palo_udf9BigIntValEEEvPNS2_15FunctionContextEPT_",
                prefix + "17count_star_updateEPN8palo_udf15FunctionContextEPNS1_9BigIntValE",
                prefix + "11count_mergeEPN8palo_udf15FunctionContextERKNS1_9BigIntValEPS4_",
                null, null));


        for (Type t : Type.getSupportedTypes()) {
            if (t.isNull()) {
                continue; // NULL is handled through type promotion.
            }
            if (t.isScalarType(PrimitiveType.CHAR)) {
                continue; // promoted to STRING
            }
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "first_value", Lists.newArrayList(t), t, t,
                    t.isStringType() ? initNullString : initNull,
                    prefix + FIRST_VALUE_UPDATE_SYMBOL.get(t),
                    null,
                    t == Type.VARCHAR ? stringValGetValue : null,
                    t == Type.VARCHAR ? stringValSerializeOrFinalize : null));
            // Implements FIRST_VALUE for some windows that require rewrites during planning.
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "first_value_rewrite", Lists.newArrayList(t, Type.BIGINT), t, t,
                    t.isStringType() ? initNullString : initNull,
                    prefix + FIRST_VALUE_REWRITE_UPDATE_SYMBOL.get(t),
                    null,
                    t == Type.VARCHAR ? stringValGetValue : null,
                    t == Type.VARCHAR ? stringValSerializeOrFinalize : null,
                    false));

            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "last_value", Lists.newArrayList(t), t, t,
                    t.isStringType() ? initNullString : initNull,
                    prefix + LAST_VALUE_UPDATE_SYMBOL.get(t),
                    prefix + LAST_VALUE_REMOVE_SYMBOL.get(t),
                    t == Type.VARCHAR ? stringValGetValue : null,
                    t == Type.VARCHAR ? stringValSerializeOrFinalize : null));

            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lag", Lists.newArrayList(t, Type.BIGINT, t), t, t,
                    prefix + OFFSET_FN_INIT_SYMBOL.get(t),
                    prefix + OFFSET_FN_UPDATE_SYMBOL.get(t),
                    null, null, null));
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lead", Lists.newArrayList(t, Type.BIGINT, t), t, t,
                    prefix + OFFSET_FN_INIT_SYMBOL.get(t),
                    prefix + OFFSET_FN_UPDATE_SYMBOL.get(t),
                    null, null, null));

            // lead() and lag() the default offset and the default value should be
            // rewritten to call the overrides that take all parameters.
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lag", Lists.newArrayList(t), t, t));
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lag", Lists.newArrayList(t, Type.BIGINT), t, t));
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lead", Lists.newArrayList(t), t, t));
            addBuiltin(AggregateFunction.createAnalyticBuiltin(
                    "lead", Lists.newArrayList(t, Type.BIGINT), t, t));
        }

    }
}
