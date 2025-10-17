/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * @author Jorge Bay Gondra
 */

import assert from 'assert';
import * as glvModule from '../../lib/index.js';
import { process, structure, driver } from '../../lib/index.js'

describe('API', function () {
  it('should export fields under process', function () {
    assert.ok(glvModule);
    assert.ok(process);
    assert.strictEqual(typeof process.Bytecode, 'function');
    assert.strictEqual(typeof process.EnumValue, 'function');
    assert.strictEqual(typeof process.P, 'function');
    assert.strictEqual(typeof process.Traversal, 'function');
    assert.strictEqual(typeof process.TraversalSideEffects, 'function');
    assert.strictEqual(typeof process.TraversalStrategies, 'function');
    assert.strictEqual(typeof process.TraversalStrategy, 'function');
    assert.strictEqual(typeof process.Traverser, 'function');
    assert.strictEqual(typeof process.GraphTraversal, 'function');
    assert.strictEqual(typeof process.GraphTraversalSource, 'function');
    assert.strictEqual(typeof process.barrier, 'object');
    assert.strictEqual(typeof process.cardinality, 'object');
    assert.strictEqual(typeof process.column, 'object');
    assert.strictEqual(typeof process.direction, 'object');
    assert.strictEqual(typeof process.direction.both, 'object');
    assert.strictEqual(process.direction.both.elementName, 'BOTH');
    assert.strictEqual(typeof process.operator, 'object');
    assert.strictEqual(typeof process.order, 'object');
    assert.strictEqual(typeof process.pop, 'object');
    assert.strictEqual(typeof process.scope, 'object');
    assert.strictEqual(typeof process.t, 'object');
    assert.ok(process.statics);
    validateConstructor(process, 'AnonymousTraversalSource');
    assert.strictEqual(typeof process.traversal, 'function');
  });
  it('should expose fields under structure', function () {
    assert.ok(structure);
    assert.ok(structure.io);
    assert.strictEqual(typeof structure.io.GraphSONReader, 'function');
    assert.strictEqual(typeof structure.io.GraphSONWriter, 'function');
    validateConstructor(structure.io, 'GraphSON2Reader');
    validateConstructor(structure.io, 'GraphSON2Writer');
    validateConstructor(structure.io, 'GraphSON3Reader');
    validateConstructor(structure.io, 'GraphSON3Writer');
    assert.strictEqual(structure.io.GraphSONReader, structure.io.GraphSON3Reader);
    assert.strictEqual(structure.io.GraphSONWriter, structure.io.GraphSON3Writer);
    assert.strictEqual(typeof structure.Edge, 'function');
    assert.strictEqual(typeof structure.Graph, 'function');
    assert.strictEqual(typeof structure.Path, 'function');
    assert.strictEqual(typeof structure.Property, 'function');
    assert.strictEqual(typeof structure.Vertex, 'function');
    assert.strictEqual(typeof structure.VertexProperty, 'function');
  });
  it('should expose fields under driver', function () {
    assert.ok(driver);
    validateConstructor(driver, 'RemoteConnection');
    validateConstructor(driver, 'RemoteStrategy');
    validateConstructor(driver, 'RemoteTraversal');
    validateConstructor(driver, 'DriverRemoteConnection');
    validateConstructor(driver, 'Client');
    validateConstructor(driver, 'ResultSet');
  });
});

function validateConstructor(parent, name) {
  assert.strictEqual(typeof parent[name], 'function');
  assert.strictEqual(parent[name].name, name);
}
