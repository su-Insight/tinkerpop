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

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;

public abstract class AbstractTranslateVisitor extends TranslateVisitor {

    public AbstractTranslateVisitor(final String graphTraversalSourceName) {
        super(graphTraversalSourceName);
    }

    @Override
    public Void visitStringLiteral(final GremlinParser.StringLiteralContext ctx) {
        // remove the first and last character (single or double quotes)
        final String text = removeFirstAndLastCharacters(ctx.getText());
        handleStringLiteralText(text);
        return null;
    }

    @Override
    public Void visitStringNullableLiteral(final GremlinParser.StringNullableLiteralContext ctx) {
        // remove the first and last character (single or double quotes) but only if it is not null
        if (ctx.getText().equals("null")) {
            sb.append("null");
        } else {
            final String text = removeFirstAndLastCharacters(ctx.getText());
            handleStringLiteralText(text);
        }
        return null;
    }

    @Override
    public Void visitTraversalStrategyArgs_ProductiveByStrategy(final GremlinParser.TraversalStrategyArgs_ProductiveByStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    @Override
    public Void visitTraversalStrategyArgs_PartitionStrategy(final GremlinParser.TraversalStrategyArgs_PartitionStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    @Override
    public Void visitTraversalStrategyArgs_SubgraphStrategy(final GremlinParser.TraversalStrategyArgs_SubgraphStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    @Override
    public Void visitTraversalStrategyArgs_EdgeLabelVerificationStrategy(final GremlinParser.TraversalStrategyArgs_EdgeLabelVerificationStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    @Override
    public Void visitTraversalStrategyArgs_ReservedKeysVerificationStrategy(final GremlinParser.TraversalStrategyArgs_ReservedKeysVerificationStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    @Override
    public Void visitTraversalStrategyArgs_SeedStrategy(final GremlinParser.TraversalStrategyArgs_SeedStrategyContext ctx) {
        return appendStrategyArguments(ctx);
    }

    protected void handleStringLiteralText(final String text) {
        sb.append("\"");
        sb.append(text);
        sb.append("\"");
    }

    /**
     * Translate an argument pair for a traversal strategy.
     */
    protected abstract Void appendStrategyArguments(final ParseTree ctx);
}
