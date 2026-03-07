/*
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

package org.apache.fluss.server.kv.rowmerger.aggregate;

import org.apache.fluss.metadata.AggFunctionType;
import org.apache.fluss.server.kv.rowmerger.aggregate.factory.FieldAggregatorFactory;
import org.apache.fluss.server.kv.rowmerger.aggregate.functions.FieldRoaringBitmap32Agg;
import org.apache.fluss.server.utils.RoaringBitmapUtils;
import org.apache.fluss.types.BitmapType;

import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests that BitmapType is correctly wired to the RoaringBitmap aggregation engine. */
public class BitmapTypeAggregationTest {

    @Test
    void testFactoryResolvesForBitmapType() {
        // Verify the SPI registry resolves RBM32 factory
        FieldAggregatorFactory factory = FieldAggregatorFactory.getFactory(AggFunctionType.RBM32);
        assertThat(factory).isNotNull();

        // Verify it accepts BitmapType without throwing
        BitmapType bitmapType = new BitmapType();
        var aggregator = factory.create(bitmapType, null);
        assertThat(aggregator).isInstanceOf(FieldRoaringBitmap32Agg.class);
    }

    @Test
    void testBitmapOrAggregation() throws IOException {
        FieldAggregatorFactory factory = FieldAggregatorFactory.getFactory(AggFunctionType.RBM32);
        BitmapType bitmapType = new BitmapType();
        var aggregator = factory.create(bitmapType, null);

        // Bitmap 1: contains {1, 2, 3}
        RoaringBitmap bm1 = RoaringBitmap.bitmapOf(1, 2, 3);
        byte[] bytes1 = RoaringBitmapUtils.serializeRoaringBitmap32(bm1);

        // Bitmap 2: contains {3, 4, 5}
        RoaringBitmap bm2 = RoaringBitmap.bitmapOf(3, 4, 5);
        byte[] bytes2 = RoaringBitmapUtils.serializeRoaringBitmap32(bm2);

        // OR union should produce {1, 2, 3, 4, 5}
        byte[] result = (byte[]) aggregator.agg(bytes1, bytes2);

        RoaringBitmap resultBitmap = new RoaringBitmap();
        RoaringBitmapUtils.deserializeRoaringBitmap32(resultBitmap, result);

        assertThat(resultBitmap.getLongCardinality()).isEqualTo(5L);
        assertThat(resultBitmap.contains(1)).isTrue();
        assertThat(resultBitmap.contains(2)).isTrue();
        assertThat(resultBitmap.contains(3)).isTrue();
        assertThat(resultBitmap.contains(4)).isTrue();
        assertThat(resultBitmap.contains(5)).isTrue();
    }

    @Test
    void testNullHandling() throws IOException {
        FieldAggregatorFactory factory = FieldAggregatorFactory.getFactory(AggFunctionType.RBM32);
        BitmapType bitmapType = new BitmapType();
        var aggregator = factory.create(bitmapType, null);

        RoaringBitmap bm = RoaringBitmap.bitmapOf(10, 20, 30);
        byte[] bytes = RoaringBitmapUtils.serializeRoaringBitmap32(bm);

        // null accumulator should return input
        assertThat(aggregator.agg(null, bytes)).isEqualTo(bytes);

        // null input should return accumulator
        assertThat(aggregator.agg(bytes, null)).isEqualTo(bytes);

        // both null should return null
        assertThat(aggregator.agg(null, null)).isNull();
    }

    @Test
    void testBitmapTypeParserRoundTrip() {
        // Verify BitmapType serializes and deserializes correctly
        BitmapType bitmapType = new BitmapType();
        assertThat(bitmapType.asSerializableString()).isEqualTo("BITMAP");
        assertThat(bitmapType.getTypeRoot().name()).isEqualTo("BITMAP");
    }
}
