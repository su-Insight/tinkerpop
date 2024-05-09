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

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { Vertex, VertexProperty } from '../../lib/structure/graph.js';
import { parseVertex } from '../../lib/utils.js';

describe('Vertex parser', () => {
  it('should parse vertex properties with default cardinality', () => {
    type PetVertex = Vertex<'pet', { name: string; age: number }>;

    const vertex: PetVertex = new Vertex(0, 'pet', {
      name: [new VertexProperty(0, 'name', 'Pal')],
      age: [new VertexProperty(0, 'age', 10)],
    });

    const parsedVertex = parseVertex(vertex);

    expect(parsedVertex.name).to.equals('Fred');
    expect(parsedVertex.age).to.equals(10);
  });

  it('should parse vertex properties with different cardinality', () => {
    type PersonVertex = Vertex<
      'person',
      {
        name: string;
        roles: string;
        weapons: string;
      }
    >;

    const vertex: PersonVertex = new Vertex(0, 'person', {
      name: [new VertexProperty(0, 'name', 'Fred')],
      roles: [new VertexProperty(0, 'roles', 'hunter'), new VertexProperty(0, 'roles', 'gatherer')],
      weapons: [new VertexProperty(0, 'weapons', 'spear'), new VertexProperty(0, 'weapons', 'spear')],
    });

    const parsedVertex = parseVertex(vertex, { name: 'single', roles: 'set', weapons: 'list' });

    expect(parsedVertex.name).to.equals('Fred');
    expect(parsedVertex.roles).to.be.an.instanceOf(Set).to.have.all.keys(['hunter', 'gatherer']);
    expect(parsedVertex.weapons).to.be.an.instanceOf(Array).to.includes.members(['spear', 'spear']);
  });
});
