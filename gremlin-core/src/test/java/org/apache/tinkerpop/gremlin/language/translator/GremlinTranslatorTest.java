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
package org.apache.tinkerpop.gremlin.language.translator;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class GremlinTranslatorTest {

    @RunWith(Parameterized.class)
    public static class VariableTest {

        @Parameterized.Parameter(value = 0)
        public String query;

        @Parameterized.Parameter(value = 1)
        public List<String> expectedVariables;

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"g.V(l1)", Collections.singletonList("l1")},
                    {"g.V().hasLabel('person').has(x, y).as('a').out('knows').as('b').select('a', 'b')", Arrays.asList("x", "y")},
                    {"g.V(x).map(out(y).count())", Arrays.asList("x", "y")},
            });
        }

        @Test
        public void shouldExtractVariablesFromLanguage() {
            final Translation translation = GremlinTranslator.translate(query, Translator.LANGUAGE);
            assertEquals(expectedVariables.size(), translation.getParameters().size());
            assertThat(translation.getParameters().toArray(), arrayContainingInAnyOrder(expectedVariables.toArray()));
        }

        @Test
        public void shouldExtractVariablesFromJava() {
            final Translation translation = GremlinTranslator.translate(query, Translator.JAVA);
            assertEquals(expectedVariables.size(), translation.getParameters().size());
            assertThat(translation.getParameters().toArray(), arrayContainingInAnyOrder(expectedVariables.toArray()));
        }

        @Test
        public void shouldExtractVariablesFromPython() {
            final Translation translation = GremlinTranslator.translate(query, Translator.PYTHON);
            assertEquals(expectedVariables.size(), translation.getParameters().size());
            assertThat(translation.getParameters().toArray(), arrayContainingInAnyOrder(expectedVariables.toArray()));
        }
    }

    @RunWith(Parameterized.class)
    public static class TranslationTest {
        private final String query;
        private final String expectedForLang;
        private final String expectedForAnonymized;
        private final String expectedForGroovy;
        private final String expectedForJava;
        private final String expectedForJavascript;
        private final String expectedForPython;

        /**
         * Test data where first element is the Gremlin query to translate and the following elements are the expected
         * translations for each language.
         * <ol>
         *     <li>Language</li>
         *     <li>Anonymized</li>
         *     <li>Groovy</li>
         *     <li>Java</li>
         *     <li>Javascript</li>
         *     <li>Python</li>
         * </ol>
         * If the translation is expected end in error then just set the value to the expected error message.
         */
        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"g",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.with(\"x\")",
                            null,
                            "g.with(string0)",
                            null,
                            null,
                            "g.with_(\"x\")",
                            "g.with_('x')"},
                    {"g.with(\"x\n\\\"yz\")",
                            null,
                            "g.with(string0)",
                            null,
                            null,
                            "g.with_(\"x\n\\\"yz\")",
                            "g.with_('x\n\\\"yz')"},
                    {"g.with('x', 'xyz')",
                            null,
                            "g.with(string0, string1)",
                            null,
                            "g.with(\"x\", \"xyz\")",
                            "g.with_(\"x\", \"xyz\")",
                            "g.with_('x', 'xyz')"},
                    {"g.with('x','xyz')",
                            "g.with('x', 'xyz')",
                            "g.with(string0, string1)",
                            "g.with('x', 'xyz')",
                            "g.with(\"x\", \"xyz\")",
                            "g.with_(\"x\", \"xyz\")",
                            "g.with_('x', 'xyz')"},
                    {"g.with('x', '')",
                            null,
                            "g.with(string0, string1)",
                            null,
                            "g.with(\"x\", \"\")",
                            "g.with_(\"x\", \"\")",
                            "g.with_('x', '')"},
                    {"g.with('x', '     ')",
                            null,
                            "g.with(string0, string1)",
                            null,
                            "g.with(\"x\", \"     \")",
                            "g.with_(\"x\", \"     \")",
                            "g.with_('x', '     ')"},
                    {"g.with('x', 'x')",
                            null,
                            "g.with(string0, string0)",
                            null,
                            "g.with(\"x\", \"x\")",
                            "g.with_(\"x\", \"x\")",
                            "g.with_('x', 'x')"},
                    {"g.with('x', null)",
                            null,
                            "g.with(string0, object0)",
                            null,
                            "g.with(\"x\", null)",
                            "g.with_(\"x\", null)",
                            "g.with_('x', None)"},
                    {"g.with('x', NaN)",
                            null,
                            "g.with(string0, number0)",
                            null,
                            "g.with(\"x\", Double.NaN)",
                            "g.with_(\"x\", Number.NaN)",
                            "g.with_('x', float('nan'))"},
                    {"g.with('x', Infinity)",
                            null,
                            "g.with(string0, number0)",
                            null,
                            "g.with(\"x\", Double.POSITIVE_INFINITY)",
                            "g.with_(\"x\", Number.POSITIVE_INFINITY)",
                            "g.with_('x', float('inf'))"},
                    {"g.with('x', -Infinity)",
                            null,
                            "g.with(string0, number0)",
                            null,
                            "g.with(\"x\", Double.NEGATIVE_INFINITY)",
                            "g.with_(\"x\", Number.NEGATIVE_INFINITY)",
                            "g.with_('x', float('-inf'))"},
                    {"g.with('x', 1.0)",
                            null,
                            "g.with(string0, number0)",
                            null,
                            "g.with(\"x\", 1.0)",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)",},
                    {"g.with('x', 1.0D)",
                            "g.with('x', 1.0d)",
                            "g.with(string0, double0)",
                            "g.with('x', 1.0d)",
                            "g.with(\"x\", 1.0d)",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)"},
                    {"g.with('x', 1.0d)",
                            null,
                            "g.with(string0, double0)",
                            null,
                            "g.with(\"x\", 1.0d)",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)"},
                    {"g.with('x', -1.0d)",
                            null,
                            "g.with(string0, double0)",
                            null,
                            "g.with(\"x\", -1.0d)",
                            "g.with_(\"x\", -1.0)",
                            "g.with_('x', -1.0)"},
                    {"g.with('x', 1.0F)",
                            "g.with('x', 1.0f)",
                            "g.with(string0, float0)",
                            "g.with('x', 1.0f)",
                            "g.with(\"x\", 1.0f)",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)"},
                    {"g.with('x', 1.0f)",
                            null,
                            "g.with(string0, float0)",
                            null,
                            "g.with(\"x\", 1.0f)",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)"},
                    {"g.with('x', -1.0F)",
                            "g.with('x', -1.0f)",
                            "g.with(string0, float0)",
                            "g.with('x', -1.0f)",
                            "g.with(\"x\", -1.0f)",
                            "g.with_(\"x\", -1.0)",
                            "g.with_('x', -1.0)"},
                    {"g.with('x', 1.0m)",
                            null,
                            "g.with(string0, bigdecimal0)",
                            "g.with('x', 1.0g)",
                            "g.with(\"x\", new BigDecimal(\"1.0\"))",
                            "g.with_(\"x\", 1.0)",
                            "g.with_('x', 1.0)"},
                    {"g.with('x', -1.0m)",
                            null,
                            "g.with(string0, bigdecimal0)",
                            "g.with('x', -1.0g)",
                            "g.with(\"x\", new BigDecimal(\"-1.0\"))",
                            "g.with_(\"x\", -1.0)",
                            "g.with_('x', -1.0)"},
                    {"g.with('x', -1.0M)",
                            "g.with('x', -1.0m)",
                            "g.with(string0, bigdecimal0)",
                            "g.with('x', -1.0g)",
                            "g.with(\"x\", new BigDecimal(\"-1.0\"))",
                            "g.with_(\"x\", -1.0)",
                            "g.with_('x', -1.0)"},
                    {"g.with('x', 1b)",
                            null,
                            "g.with(string0, byte0)",
                            "g.with('x', new Byte(1))",
                            "g.with(\"x\", new Byte(1))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', 1B)",
                            "g.with('x', 1b)",
                            "g.with(string0, byte0)",
                            "g.with('x', new Byte(1))",
                            "g.with(\"x\", new Byte(1))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', -1b)",
                            null,
                            "g.with(string0, byte0)",
                            "g.with('x', new Byte(-1))",
                            "g.with(\"x\", new Byte(-1))",
                            "g.with_(\"x\", -1)",
                            "g.with_('x', -1)"},
                    {"g.with('x', 1s)",
                            null,
                            "g.with(string0, short0)",
                            "g.with('x', new Short(1))",
                            "g.with(\"x\", new Short(1))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', -1s)",
                            null,
                            "g.with(string0, short0)",
                            "g.with('x', new Short(-1))",
                            "g.with(\"x\", new Short(-1))",
                            "g.with_(\"x\", -1)",
                            "g.with_('x', -1)"},
                    {"g.with('x', 1S)",
                            "g.with('x', 1s)",
                            "g.with(string0, short0)",
                            "g.with('x', new Short(1))",
                            "g.with(\"x\", new Short(1))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', 1i)",
                            null,
                            "g.with(string0, integer0)",
                            null,
                            "g.with(\"x\", 1)",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', 1I)",
                            "g.with('x', 1i)",
                            "g.with(string0, integer0)",
                            "g.with('x', 1i)",
                            "g.with(\"x\", 1)",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', -1i)",
                            null,
                            "g.with(string0, integer0)",
                            null,
                            "g.with(\"x\", -1)",
                            "g.with_(\"x\", -1)",
                            "g.with_('x', -1)"},
                    {"g.with('x', 1l)",
                            null,
                            "g.with(string0, long0)",
                            null,
                            "g.with(\"x\", 1l)",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', long(1))"},
                    {"g.with('x', 1L)",
                            "g.with('x', 1l)",
                            "g.with(string0, long0)",
                            "g.with('x', 1l)",
                            "g.with(\"x\", 1l)",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', long(1))"},
                    {"g.with('x', -1l)",
                            null,
                            "g.with(string0, long0)",
                            null,
                            "g.with(\"x\", -1l)",
                            "g.with_(\"x\", -1)",
                            "g.with_('x', long(-1))"},
                    {"g.with('x', 1n)",
                            null,
                            "g.with(string0, biginteger0)",
                            "g.with('x', 1g)",
                            "g.with(\"x\", new BigInteger(\"1\"))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', 1N)",
                            "g.with('x', 1n)",
                            "g.with(string0, biginteger0)",
                            "g.with('x', 1g)",
                            "g.with(\"x\", new BigInteger(\"1\"))",
                            "g.with_(\"x\", 1)",
                            "g.with_('x', 1)"},
                    {"g.with('x', -1n)",
                            null,
                            "g.with(string0, biginteger0)",
                            "g.with('x', -1g)",
                            "g.with(\"x\", new BigInteger(\"-1\"))",
                            "g.with_(\"x\", -1)",
                            "g.with_('x', -1)"},
                    {"g.with('x', datetime('2023-08-02T00:00:00Z'))",
                            null,
                            "g.with(string0, date0)",
                            null,
                            "g.with(\"x\", new Date(1690934400000))",
                            "g.with_(\"x\", new Date(1690934400000))",
                            "g.with_('x', datetime.datetime.utcfromtimestamp(1690934400000 / 1000.0))"},
                    {"g.with('x', [x: 1])",
                            "g.with('x', [x:1])",
                            "g.with(string0, map0)",
                            "g.with('x', [x:1])",
                            "g.with(\"x\", new LinkedHashMap<Object, Object>() {{ put(\"x\", 1); }})",
                            "g.with_(\"x\", new Map([[\"x\", 1]]))",
                            "g.with_('x', { 'x': 1 })"},
                    {"g.with('x', [x:1, new:2])",
                            null,
                            "g.with(string0, map0)",
                            null,
                            "g.with(\"x\", new LinkedHashMap<Object, Object>() {{ put(\"x\", 1); put(\"new\", 2); }})",
                            "g.with_(\"x\", new Map([[\"x\", 1], [\"new\", 2]]))",
                            "g.with_('x', { 'x': 1, 'new': 2 })"},
                    {"g.with('x', [\"x\":1])",
                            null,
                            "g.with(string0, map0)",
                            null,
                            "g.with(\"x\", new LinkedHashMap<Object, Object>() {{ put(\"x\", 1); }})",
                            "g.with_(\"x\", new Map([[\"x\", 1]]))",
                            "g.with_('x', { 'x': 1 })"},
                    {"g.with('x', [1:'x'])",
                            null,
                            "g.with(string0, map0)",
                            null,
                            "g.with(\"x\", new LinkedHashMap<Object, Object>() {{ put(1, \"x\"); }})",
                            "g.with_(\"x\", new Map([[1, \"x\"]]))",
                            "g.with_('x', { 1: 'x' })"},
                    {"g.with('x', [1, 'x'])",
                            null,
                            "g.with(string0, list0)",
                            null,
                            "g.with(\"x\", new ArrayList<Object>() {{ add(1); add(\"x\"); }})",
                            "g.with_(\"x\", [1, \"x\"])",
                            "g.with_('x', [1, 'x'])"},
                    {"g.with('x', 0..5)",
                            null,
                            "g.with(string0, number0..number1)",
                            "g.with('x', 0..5)",
                            "Java does not support range literals",
                            "Javascript does not support range literals",
                            "Python does not support range literals"},
                    {"g.withBulk(false)",
                            null,
                            "g.withBulk(boolean0)",
                            null,
                            null,
                            null,
                            "g.with_bulk(False)"},
                    {"g.withBulk(true)",
                            null,
                            "g.withBulk(boolean0)",
                            null,
                            null,
                            null,
                            "g.with_bulk(True)"},
                    {"g.withBulk( true )",
                            "g.withBulk(true)",
                            "g.withBulk(boolean0)",
                            "g.withBulk(true)",
                            "g.withBulk(true)",
                            "g.withBulk(true)",
                            "g.with_bulk(True)"},
                    {"g.withBulk(x)",
                            null,
                            null,
                            null,
                            null,
                            null,
                            "g.with_bulk(x)"},
                    {"g.withStrategies(ReadOnlyStrategy)",
                            null,
                            null,
                            null,
                            "g.withStrategies(ReadOnlyStrategy.instance())",
                            "g.withStrategies(new ReadOnlyStrategy())",
                            "g.with_strategies(ReadOnlyStrategy())"},
                    {"g.withStrategies(new SeedStrategy(seed:10000))",
                            null,
                            "g.withStrategies(new SeedStrategy(seed:number0))",
                            null,
                            "g.withStrategies(SeedStrategy.build().seed(10000).create())",
                            "g.withStrategies(new SeedStrategy({seed: 10000}))",
                            "g.with_strategies(SeedStrategy(seed=10000))"},
                    {"g.withStrategies(new PartitionStrategy(includeMetaProperties: true, partitionKey:'x'))",
                            "g.withStrategies(new PartitionStrategy(includeMetaProperties:true, partitionKey:'x'))",
                            "g.withStrategies(new PartitionStrategy(includeMetaProperties:boolean0, partitionKey:string0))",
                            "g.withStrategies(new PartitionStrategy(includeMetaProperties:true, partitionKey:'x'))",
                            "g.withStrategies(PartitionStrategy.build().includeMetaProperties(true).partitionKey(\"x\").create())",
                            "g.withStrategies(new PartitionStrategy({includeMetaProperties: true, partitionKey: \"x\"}))",
                            "g.with_strategies(PartitionStrategy(include_meta_properties=True, partition_key='x'))"},
                    {"g.withStrategies(new SubgraphStrategy(vertices:__.has('name', 'vadas'), edges: has('weight', gt(0.5))))",
                            "g.withStrategies(new SubgraphStrategy(vertices:__.has('name', 'vadas'), edges:__.has('weight', P.gt(0.5))))",
                            "g.withStrategies(new SubgraphStrategy(vertices:__.has(string0, string1), edges:__.has(string2, P.gt(number0))))",
                            "g.withStrategies(new SubgraphStrategy(vertices:__.has('name', 'vadas'), edges:__.has('weight', P.gt(0.5))))",
                            "g.withStrategies(SubgraphStrategy.build().vertices(__.has(\"name\", \"vadas\")).edges(__.has(\"weight\", P.gt(0.5))).create())",
                            "g.withStrategies(new SubgraphStrategy({vertices: __.has(\"name\", \"vadas\"), edges: __.has(\"weight\", P.gt(0.5))}))",
                            "g.with_strategies(SubgraphStrategy(vertices=__.has('name', 'vadas'), edges=__.has('weight', P.gt(0.5))))"},
                    {"g.withStrategies(new SubgraphStrategy(checkAdjacentVertices: false,\n" +
                            "                                            vertices: __.has(\"name\", P.within(\"josh\", \"lop\", \"ripple\")),\n" +
                            "                                            edges: __.or(__.has(\"weight\", 0.4).hasLabel(\"created\"),\n" +
                            "                                                         __.has(\"weight\", 1.0).hasLabel(\"created\")))).E()",
                            "g.withStrategies(new SubgraphStrategy(checkAdjacentVertices:false, vertices:__.has(\"name\", P.within(\"josh\", \"lop\", \"ripple\")), edges:__.or(__.has(\"weight\", 0.4).hasLabel(\"created\"), __.has(\"weight\", 1.0).hasLabel(\"created\")))).E()",
                            "g.withStrategies(new SubgraphStrategy(checkAdjacentVertices:boolean0, vertices:__.has(string0, P.within(string1, string2, string3)), edges:__.or(__.has(string4, number0).hasLabel(string5), __.has(string4, number1).hasLabel(string5)))).E()",
                            "g.withStrategies(new SubgraphStrategy(checkAdjacentVertices:false, vertices:__.has(\"name\", P.within(\"josh\", \"lop\", \"ripple\")), edges:__.or(__.has(\"weight\", 0.4).hasLabel(\"created\"), __.has(\"weight\", 1.0).hasLabel(\"created\")))).E()",
                            "g.withStrategies(SubgraphStrategy.build().checkAdjacentVertices(false).vertices(__.has(\"name\", P.within(\"josh\", \"lop\", \"ripple\"))).edges(__.or(__.has(\"weight\", 0.4).hasLabel(\"created\"), __.has(\"weight\", 1.0).hasLabel(\"created\"))).create()).E()",
                            "g.withStrategies(new SubgraphStrategy({checkAdjacentVertices: false, vertices: __.has(\"name\", P.within(\"josh\", \"lop\", \"ripple\")), edges: __.or(__.has(\"weight\", 0.4).hasLabel(\"created\"), __.has(\"weight\", 1.0).hasLabel(\"created\"))})).E()",
                            "g.with_strategies(SubgraphStrategy(check_adjacent_vertices=False, vertices=__.has('name', P.within('josh', 'lop', 'ripple')), edges=__.or_(__.has('weight', 0.4).has_label('created'), __.has('weight', 1.0).has_label('created')))).E()"},
                    {"g.inject(0..5)",
                            null,
                            "g.inject(number0..number1)",
                            "g.inject(0..5)",
                            "Java does not support range literals",
                            "Javascript does not support range literals",
                            "Python does not support range literals"},
                    {"g.inject(1694017707000).asDate()",
                            null,
                            "g.inject(number0).asDate()",
                            null,
                            null,
                            null,
                            "g.inject(long(1694017707000)).as_date()"},
                    {"g.V().hasLabel(null)",
                            null,
                            "g.V().hasLabel(string0)",
                            null,
                            null,
                            null,
                            "g.V().has_label(None)",},
                    {"g.V().hasLabel('person')",
                            null,
                            "g.V().hasLabel(string0)",
                            "g.V().hasLabel('person')",
                            "g.V().hasLabel(\"person\")",
                            "g.V().hasLabel(\"person\")",
                            "g.V().has_label('person')"},
                    {"g.V().hasLabel('person', 'software', 'class')",
                            null,
                            "g.V().hasLabel(string0, string1, string2)",
                            "g.V().hasLabel('person', 'software', 'class')",
                            "g.V().hasLabel(\"person\", \"software\", \"class\")",
                            "g.V().hasLabel(\"person\", \"software\", \"class\")",
                            "g.V().has_label('person', 'software', 'class')"},
                    {"g.V().hasLabel(null, 'software', 'class')",
                            null,
                            "g.V().hasLabel(string0, string1, string2)",
                            "g.V().hasLabel(null, 'software', 'class')",
                            "g.V().hasLabel(null, \"software\", \"class\")",
                            "g.V().hasLabel(null, \"software\", \"class\")",
                            "g.V().has_label(None, 'software', 'class')"},
                    {"g.V().map(__.out().count())",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.V().map(out().count())",
                            "g.V().map(__.out().count())",
                            "g.V().map(__.out().count())",
                            "g.V().map(__.out().count())",
                            "g.V().map(__.out().count())",
                            "g.V().map(__.out().count())",
                            "g.V().map(__.out().count())"},
                    {"g.V().fold().count(Scope.local)",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.V().fold().count(Scope.local)",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.V().has(T.id, 1)",
                            null,
                            "g.V().has(T.id, number0)",
                            null,
                            null,
                            null,
                            "g.V().has(T.id_, 1)"},
                    {"g.V().has(id, 1)",
                            "g.V().has(T.id, 1)",
                            "g.V().has(T.id, number0)",
                            "g.V().has(T.id, 1)",
                            "g.V().has(T.id, 1)",
                            "g.V().has(T.id, 1)",
                            "g.V().has(T.id_, 1)"},
                    {"g.V().has(\"name\", P.within(\"josh\",\"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(string0, P.within(string1, string2))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has('name', P.within('josh', 'stephen'))"},
                    {"g.V().has(\"name\", P.eq(\"josh\"))",
                            null,
                            "g.V().has(string0, P.eq(string1))",
                            null,
                            null,
                            null,
                            "g.V().has('name', P.eq('josh'))"},
                    {"g.V().has(\"name\", P.eq(\"josh\").negate())",
                            null,
                            "g.V().has(string0, P.eq(string1).negate())",
                            null,
                            null,
                            null,
                            "g.V().has('name', P.eq('josh').negate())"},
                    {"g.V().has(\"name\", P.within())",
                            null,
                            "g.V().has(string0, P.within())",
                            null,
                            null,
                            null,
                            "g.V().has('name', P.within())"},
                    {"g.V().has(\"name\", P.within(\"josh\",\"stephen\").or(eq(\"vadas\")))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\").or(P.eq(\"vadas\")))",
                            "g.V().has(string0, P.within(string1, string2).or(P.eq(string3)))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\").or(P.eq(\"vadas\")))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\").or(P.eq(\"vadas\")))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\").or(P.eq(\"vadas\")))",
                            "g.V().has('name', P.within('josh', 'stephen').or_(P.eq('vadas')))"},
                    {"g.V().has(\"name\", within(\"josh\", \"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(string0, P.within(string1, string2))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has(\"name\", P.within(\"josh\", \"stephen\"))",
                            "g.V().has('name', P.within('josh', 'stephen'))"},
                    {"g.V().has(\"name\", TextP.containing(\"j\").negate())",
                            null,
                            "g.V().has(string0, TextP.containing(string1).negate())",
                            null,
                            null,
                            null,
                            "g.V().has('name', TextP.containing('j').negate())"},
                    {"g.V().hasLabel(\"person\").has(\"age\", P.not(P.lte(10).and(P.not(P.between(11, 20)))).and(P.lt(29).or(P.eq(35)))).values(\"name\")",
                            null,
                            "g.V().hasLabel(string0).has(string1, P.not(P.lte(number0).and(P.not(P.between(number1, number2)))).and(P.lt(number3).or(P.eq(number4)))).values(string2)",
                            null,
                            null,
                            null,
                            "g.V().has_label('person').has('age', P.not_(P.lte(10).and_(P.not_(P.between(11, 20)))).and_(P.lt(29).or_(P.eq(35)))).values('name')"},
                    {"g.V().has(\"name\", containing(\"j\"))",
                            "g.V().has(\"name\", TextP.containing(\"j\"))",
                            "g.V().has(string0, TextP.containing(string1))",
                            "g.V().has(\"name\", TextP.containing(\"j\"))",
                            "g.V().has(\"name\", TextP.containing(\"j\"))",
                            "g.V().has(\"name\", TextP.containing(\"j\"))",
                            "g.V().has('name', TextP.containing('j'))"},
                    {"g.V().property(set, \"name\", \"stephen\")",
                            "g.V().property(Cardinality.set, \"name\", \"stephen\")",
                            "g.V().property(Cardinality.set, string0, string1)",
                            "g.V().property(Cardinality.set, \"name\", \"stephen\")",
                            "g.V().property(Cardinality.set, \"name\", \"stephen\")",
                            "g.V().property(Cardinality.set, \"name\", \"stephen\")",
                            "g.V().property(Cardinality.set_, 'name', 'stephen')"},
                    {"g.V().property(Cardinality.set, \"name\", \"stephen\")",
                            null,
                            "g.V().property(Cardinality.set, string0, string1)",
                            null,
                            null,
                            null,
                            "g.V().property(Cardinality.set_, 'name', 'stephen')"},
                    {"g.V().has('name', 'foo').property([\"name\":Cardinality.set(\"bar\"), \"age\":43])",
                            null,
                            "g.V().has(string0, string1).property(map0)",
                            null,
                            "g.V().has(\"name\", \"foo\").property(new LinkedHashMap<Object, Object>() {{ put(\"name\", Cardinality.set(\"bar\")); put(\"age\", 43); }})",
                            "g.V().has(\"name\", \"foo\").property(new Map([[\"name\", CardinalityValue.set(\"bar\")], [\"age\", 43]]))",
                            "g.V().has('name', 'foo').property({ 'name': CardinalityValue.set_('bar'), 'age': 43 })"},
                    {"g.V(new Vertex(1, \"person\")).limit(1)",
                            null,
                            "g.V(new Vertex(number0, string0)).limit(number0)",
                            "g.V(new DetachedVertex(1, \"person\")).limit(1)",
                            "g.V(new DetachedVertex(1, \"person\")).limit(1)",
                            "g.V(new Vertex(1, \"person\")).limit(1)",
                            "g.V(Vertex(1, 'person')).limit(1)",},
                    {"g.V().both().properties().dedup().hasKey(\"age\").value()",
                            null,
                            "g.V().both().properties().dedup().hasKey(string0).value()",
                            null,
                            null,
                            null,
                            "g.V().both().properties().dedup().has_key('age').value()",},
                    {"g.V().connectedComponent().with(ConnectedComponent.propertyName, \"component\")",
                            "g.V().connectedComponent().with(ConnectedComponent.propertyName, \"component\")",
                            "g.V().connectedComponent().with(ConnectedComponent.propertyName, string0)",
                            "g.V().connectedComponent().with(ConnectedComponent.propertyName, \"component\")",
                            "g.V().connectedComponent().with(ConnectedComponent.propertyName, \"component\")",
                            "g.V().connectedComponent().with_(ConnectedComponent.propertyName, \"component\")",
                            "g.V().connected_component().with_(ConnectedComponent.property_name, 'component')"},
                    {"g.withSideEffect(\"c\", xx2).withSideEffect(\"m\", xx3).mergeE(xx1).option(Merge.onCreate, __.select(\"c\")).option(Merge.onMatch, __.select(\"m\"))",
                            null,
                            "g.withSideEffect(string0, xx2).withSideEffect(string1, xx3).mergeE(map0).option(Merge.onCreate, __.select(string0)).option(Merge.onMatch, __.select(string1))",
                            null,
                            null,
                            null,
                            "g.with_side_effect('c', xx2).with_side_effect('m', xx3).merge_e(xx1).option(Merge.on_create, __.select('c')).option(Merge.on_match, __.select('m'))"},
                    {"g.V(1, 2, 3)",
                            null,
                            "g.V(number0, number1, number2)",
                            null,
                            null,
                            null,
                            null},
                    {"g.V().limit(1)",
                            null,
                            "g.V().limit(number0)",
                            null,
                            null,
                            null,
                            null},
                    {"g.V().limit(1L)",
                            "g.V().limit(1l)",
                            "g.V().limit(long0)",
                            "g.V().limit(1l)",
                            "g.V().limit(1l)",
                            "g.V().limit(1)",
                            "g.V().limit(long(1))"},
                    {"g.V().limit(x)",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.V().toList()",
                            null,
                            null,
                            null,
                            null,
                            null,
                            "g.V().to_list()"},
                    {"g.V().iterate()",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
                    {"g.tx().commit()",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null},
            });
        }

        public TranslationTest(final String query, final String expectedForLang,
                               final String expectedForAnonymized,
                               final String expectedForGroovy,
                               final String expectedForJava,
                               final String expectedForJavascript,
                               final String expectedForPython) {
            this.query = query;
            this.expectedForLang = expectedForLang != null ? expectedForLang : query;
            this.expectedForAnonymized = expectedForAnonymized != null ? expectedForAnonymized : query;
            this.expectedForGroovy = expectedForGroovy != null ? expectedForGroovy : query;
            this.expectedForJava = expectedForJava != null ? expectedForJava : query;
            this.expectedForJavascript = expectedForJavascript != null ? expectedForJavascript : query;
            this.expectedForPython = expectedForPython != null ? expectedForPython : query;
        }

        @Test
        public void shouldTranslateForLang() {
            final String translatedQuery = GremlinTranslator.translate(query, Translator.LANGUAGE).getTranslated();
            assertEquals(expectedForLang, translatedQuery);
        }

        @Test
        public void shouldTranslateForAnonymized() {
            final String translatedQuery = GremlinTranslator.translate(query, Translator.ANONYMIZED).getTranslated();
            assertEquals(expectedForAnonymized, translatedQuery);
        }

        @Test
        public void shouldTranslateForGroovy() {
            try {
                final String translatedQuery = GremlinTranslator.translate(query, "g", Translator.GROOVY).getTranslated();
                assertEquals(expectedForGroovy, translatedQuery);
            } catch (TranslatorException e) {
                assertThat(e.getMessage(), startsWith(expectedForGroovy));
            }
        }

        @Test
        public void shouldTranslateForJava() {
            try {
                final String translatedQuery = GremlinTranslator.translate(query, "g", Translator.JAVA).getTranslated();
                assertEquals(expectedForJava, translatedQuery);
            } catch (TranslatorException e) {
                assertThat(e.getMessage(), startsWith(expectedForJava));
            }
        }

        @Test
        public void shouldTranslateForJavascript() {
            try {
                final String translatedQuery = GremlinTranslator.translate(query, "g", Translator.JAVASCRIPT).getTranslated();
                assertEquals(expectedForJavascript, translatedQuery);
            } catch (TranslatorException e) {
                assertThat(e.getMessage(), startsWith(expectedForJavascript));
            }
        }

        @Test
        public void shouldTranslateForPython() {
            try {
                final String translatedQuery = GremlinTranslator.translate(query, "g", Translator.PYTHON).getTranslated();
                assertEquals(expectedForPython, translatedQuery);
            } catch (TranslatorException e) {
                assertThat(e.getMessage(), startsWith(expectedForPython));
            }
        }
    }
}