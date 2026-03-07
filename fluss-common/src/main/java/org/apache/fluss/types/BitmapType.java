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

package org.apache.fluss.types;

import org.apache.fluss.annotation.PublicStable;

import java.util.Collections;
import java.util.List;

/**
 * Data type of a RoaringBitmap stored as a compressed binary payload.
 *
 * <p>Physically stored as VARBINARY but logically identified as BITMAP to enable specialized
 * server-side aggregation via the AggregationMergeEngine.
 *
 * <p>Constraints:
 *
 * <ul>
 *   <li>Supports 32-bit unsigned integers only [0, 2^32).
 *   <li>Cannot be used as a primary key or partition key column.
 *   <li>Not orderable: no {@code <}, {@code >}, or {@code ORDER BY} semantics.
 * </ul>
 *
 * @since 0.7
 */
@PublicStable
public final class BitmapType extends DataType {

    private static final long serialVersionUID = 1L;

    private static final String FORMAT = "BITMAP";

    public BitmapType(boolean isNullable) {
        super(isNullable, DataTypeRoot.BITMAP);
    }

    public BitmapType() {
        this(true);
    }

    @Override
    public DataType copy(boolean isNullable) {
        return new BitmapType(isNullable);
    }

    @Override
    public String asSerializableString() {
        return withNullability(FORMAT);
    }

    @Override
    public List<DataType> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public <R> R accept(DataTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
