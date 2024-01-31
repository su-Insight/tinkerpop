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
package org.apache.tinkerpop.gremlin.language.grammar;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SeedStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.ProductiveByStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.AbstractWarningVerificationStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.EdgeLabelVerificationStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReadOnlyStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReservedKeysVerificationStrategy;
import org.apache.tinkerpop.gremlin.util.GremlinDisabledListDelimiterHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class TraversalStrategyVisitor extends DefaultGremlinBaseVisitor<TraversalStrategy> {
    protected final GremlinAntlrToJava antlr;

    public TraversalStrategyVisitor(final GremlinAntlrToJava antlrToJava) {
        this.antlr = antlrToJava;
    }

    @Override
    public TraversalStrategy visitTraversalStrategy(final GremlinParser.TraversalStrategyContext ctx) {
        // child count of one implies init syntax for the singleton constructed strategies. otherwise, it will
        // fall back to the Builder methods for construction
        if (ctx.getChildCount() == 1) {
            final String strategyName = ctx.classType() != null ? constructClassName(ctx.classType()) : ctx.getChild(0).getText();
            return tryToConstructStrategy(strategyName, getConfiguration(ctx.traversalStrategyArg()));
        } else {
            // start looking at strategies after the "new" keyword
            final int childIndex = ctx.getChild(0).getText().equals("new") ? 1 : 0;
            final String strategyName = ctx.getChild(childIndex).getText();
            return tryToConstructStrategy(strategyName, getConfiguration(ctx.traversalStrategyArg()));
        }
    }

    private Configuration getConfiguration(final List<GremlinParser.TraversalStrategyArgContext> contexts) {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setListDelimiterHandler(GremlinDisabledListDelimiterHandler.instance());
        if (null != contexts) {
            for (GremlinParser.TraversalStrategyArgContext ctx : contexts) {
                final String key = ctx.getChild(0).getText();
                final Object val = antlr.argumentVisitor.visitGenericLiteralArgument(ctx.genericLiteralArgument());
                conf.setProperty(key, val);
            }
        }
        return conf;
    }

    private String constructClassName(final GremlinParser.ClassTypeContext ctx) {
        final StringBuilder builder = new StringBuilder();
        for (ParseTree child : ctx.children) {
            builder.append(child.getText());
        }
        return builder.toString();
    }

    /**
     * Try to instantiate the strategy by checking registered {@link TraversalStrategy} implementations that are
     * registered globally.
     */
    private static TraversalStrategy tryToConstructStrategy(final String strategyName, final Configuration conf) {
        // try to grab the strategy class from registered sources first
        final Optional<? extends Class<? extends TraversalStrategy>> opt = TraversalStrategies.GlobalCache.getRegisteredStrategyClass(strategyName);

        if (!opt.isPresent()) {
            throw new IllegalStateException("Unexpected TraversalStrategy specification - " + strategyName);
        }

        final Class clazz = opt.get();

        try {
            if (conf.isEmpty()) {
                try {
                    return (TraversalStrategy) clazz.getMethod("instance").invoke(null);
                } catch (Exception ex) {
                    try {
                        return (TraversalStrategy) clazz.getConstructor().newInstance();
                    } catch (Exception exinner) {
                        return (TraversalStrategy) clazz.getMethod("create", Configuration.class).invoke(null, conf);
                    }
                }
            } else {
                return (TraversalStrategy) clazz.getMethod("create", Configuration.class).invoke(null, conf);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected TraversalStrategy specification - " + strategyName, ex);
        }
    }
}
