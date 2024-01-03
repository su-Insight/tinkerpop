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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;
import org.apache.tinkerpop.gremlin.util.DatetimeHelper;

import java.util.Date;
import java.util.List;

/**
 * Converts a Gremlin traversal string into a Java source code representation of that traversal with an aim at
 * sacrificing some formatting for the ability to compile correctly.
 * <ul>
 *     <li>Range syntax has no direct support</li>
 *     <li>Normalizes whitespace</li>
 *     <li>Normalize numeric suffixes to lower case</li>
 *     <li>If floats are not suffixed they will translate as BigDecimal</li>
 *     <li>Makes anonymous traversals explicit with double underscore</li>
 *     <li>Makes enums explicit with their proper name</li>
 * </ul>
 */
public class JavaTranslateVisitor extends AbstractTranslateVisitor {
    public JavaTranslateVisitor() {
        super("g");
    }

    public JavaTranslateVisitor(final String graphTraversalSourceName) {
        super(graphTraversalSourceName);
    }

    @Override
    public Void visitStructureVertex(final GremlinParser.StructureVertexContext ctx) {
        sb.append("new DetachedVertex(");
        visit(ctx.getChild(3)); // id
        sb.append(", ");
        visit(ctx.getChild(5)); // label
        sb.append(")");
        return null;
    }

    @Override
    public Void visitTraversalStrategy(final GremlinParser.TraversalStrategyContext ctx) {
        if (ctx.getChildCount() == 1)
            sb.append(ctx.getText()).append(".instance()");
        else {
            sb.append(ctx.getChild(1).getText()).append(".build()");

            // get a list of all the arguments to the strategy - i.e. anything not a terminal node
            final List<ParseTree> strategyArgs = ctx.children.stream()
                    .filter(c -> !(c instanceof TerminalNode))
                    .collect(java.util.stream.Collectors.toList());

            for (ParseTree arg : strategyArgs) {
                sb.append(".");
                visit(arg);
            }
            sb.append(".create()");
        }

        return null;
    }

    @Override
    public Void visitGenericLiteralMap(final GremlinParser.GenericLiteralMapContext ctx) {
        sb.append("new LinkedHashMap<Object, Object>() {{ ");
        for (int i = 0; i < ctx.mapEntry().size(); i++) {
            final GremlinParser.MapEntryContext mapEntryContext = ctx.mapEntry(i);
            visit(mapEntryContext);
            if (i < ctx.mapEntry().size() - 1)
                sb.append(" ");
        }
        sb.append(" }}");
        return null;
    }

    @Override
    public Void visitMapEntry(final GremlinParser.MapEntryContext ctx) {
        sb.append("put(");
        // if it is a terminal node then it has to be processed as a string for Java but otherwise it can 
        // just be handled as a generic literal 
        if (ctx.getChild(0) instanceof TerminalNode) {
            handleStringLiteralText(ctx.getChild(0).getText());
        }  else {
            visit(ctx.getChild(0));
        }
        sb.append(", ");
        visit(ctx.getChild(2)); // value
        sb.append(");");
        return null;
    }

    @Override
    public Void visitDateLiteral(final GremlinParser.DateLiteralContext ctx) {
        // child at 2 is the date argument to datetime() and comes enclosed in quotes
        final String dtString = ctx.getChild(2).getText();
        final Date dt = DatetimeHelper.parse(removeFirstAndLastCharacters(dtString));
        sb.append("new Date(");
        sb.append(dt.getTime());
        sb.append(")");
        return null;
    }

    @Override
    public Void visitNanLiteral(final GremlinParser.NanLiteralContext ctx) {
        sb.append("Double.NaN");
        return null;
    }

    @Override
    public Void visitInfLiteral(final GremlinParser.InfLiteralContext ctx) {
        if (ctx.SignedInfLiteral().getText().equals("-Infinity"))
            sb.append("Double.NEGATIVE_INFINITY");
        else
            sb.append("Double.POSITIVE_INFINITY");
        return null;
    }

    @Override
    public Void visitIntegerLiteral(final GremlinParser.IntegerLiteralContext ctx) {
        final String integerLiteral = ctx.getText().toLowerCase();

        // check suffix
        final int lastCharIndex = integerLiteral.length() - 1;
        final char lastCharacter = integerLiteral.charAt(lastCharIndex);
        switch (lastCharacter) {
            case 'b':
                // parse B/b as byte
                sb.append("new Byte(");
                sb.append(integerLiteral, 0, lastCharIndex);
                sb.append(")");
                break;
            case 's':
                // parse S/s as short
                sb.append("new Short(");
                sb.append(integerLiteral, 0, lastCharIndex);
                sb.append(")");
                break;
            case 'i':
                // parse I/i as integer
                sb.append(integerLiteral, 0, lastCharIndex);
                break;
            case 'l':
                // parse L/l as long
                sb.append(integerLiteral);
                break;
            case 'n':
                // parse N/n as BigInteger
                sb.append("new BigInteger(\"");
                sb.append(integerLiteral, 0, lastCharIndex);
                sb.append("\")");
                break;
            default:
                // everything else just goes as specified
                sb.append(integerLiteral);
                break;
        }
        return null;
    }

    @Override
    public Void visitFloatLiteral(final GremlinParser.FloatLiteralContext ctx) {
        final String floatLiteral = ctx.getText().toLowerCase();

        // check suffix
        final int lastCharIndex = floatLiteral.length() - 1;
        final char lastCharacter = floatLiteral.charAt(lastCharIndex);
        switch (lastCharacter) {
            case 'f':
            case 'd':
                // parse F/f as Float and D/d suffix as Double
                sb.append(floatLiteral);
                break;
            case 'm':
                // parse M/m or whatever which could be a parse exception
                sb.append("new BigDecimal(\"");
                sb.append(floatLiteral, 0, lastCharIndex);
                sb.append("\")");
                break;
            default:
                // everything else just goes as specified
                sb.append(floatLiteral);
                break;
        }
        return null;
    }

    @Override
    public Void visitGenericLiteralRange(final GremlinParser.GenericLiteralRangeContext ctx) {
        throw new TranslatorException("Java does not support range literals");
    }

    @Override
    public Void visitGenericLiteralCollection(final GremlinParser.GenericLiteralCollectionContext ctx) {
        sb.append("new ArrayList<Object>() {{ ");
        for (int i = 0; i < ctx.genericLiteral().size(); i++) {
            final GremlinParser.GenericLiteralContext genericLiteralContext = ctx.genericLiteral(i);
            sb.append("add(");
            visit(genericLiteralContext);
            sb.append(");");
            if (i < ctx.genericLiteral().size() - 1)
                sb.append(" ");
        }
        sb.append(" }}");
        return null;
    }

    @Override
    protected Void appendStrategyArguments(final ParseTree ctx) {
        sb.append(ctx.getChild(0)).append("(");
        visit(ctx.getChild(2));
        sb.append(")");
        return null;
    }

}
