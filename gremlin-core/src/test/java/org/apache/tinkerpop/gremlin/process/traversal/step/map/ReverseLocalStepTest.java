/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.StepTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ReverseLocalStepTest extends StepTest {
    @Override
    protected List<Traversal> getTraversals() {
        return Collections.singletonList(__.reverse());
    }
//
//    @Test
//    public void testReturnTypes() {
//        assertTrue(__.__(new int[]{}).reverse().hasNext());
//        assertArrayEquals(new Object[] {7, 10}, __.__(new int[] {10, 7}).reverse().next().toArray());
//    }
//
//    @Test
//    public void testSetTraverser() {
//        final Set<Integer> numbers = new HashSet<>();
//        numbers.add(10);
//        numbers.add(11);
//
//        final Throwable thrown = assertThrows(IllegalArgumentException.class, () -> __.__(numbers).reverse().hasNext());
//        assertEquals("Incoming traverser for reverse step must be either a list or an array.", thrown.getMessage());
//    }
//
//    @Test
//    public void testArrayTraverser() {
//        final int[] numbers = new int[] {10, 11};
//
//        final List result = __.__(numbers).reverse().next();
//        assertEquals(11, result.get(0));
//        assertEquals(10, result.get(1));
//    }
//
//    @Test
//    public void testListTraverser() {
//        final List<Integer> numbers = new ArrayList<>();
//        numbers.add(10);
//        numbers.add(11);
//
//        final List result = __.__(numbers).reverse().next();
//        assertEquals(11, result.get(0));
//        assertEquals(10, result.get(1));
//    }

}
