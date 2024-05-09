import { expect } from 'chai';
import { describe, it } from 'mocha';
import { Vertex, VertexProperties, VertexProperty } from '../../lib/structure/graph.js';
import { parseVertex } from '../../lib/utils.js';

describe('Vertex parser', () => {
  it('should parse vertex properties with default cardinality', () => {
    type PetVertex = Vertex<'pet', { name: VertexProperties<'name', string> }>;

    const vertex: PetVertex = new Vertex(0, 'pet', {
      name: [new VertexProperty(0, 'name', 'Pal')],
    });

    const parsedVertex = parseVertex(vertex);

    expect(parsedVertex.name).to.equals('Fred');
  });

  it('should parse vertex properties with different cardinality', () => {
    type PersonVertex = Vertex<
      'person',
      {
        name: VertexProperties<'name', string>;
        roles: VertexProperties<'role', string>;
        weapons: VertexProperties<'weapon', string>;
      }
    >;

    const vertex: PersonVertex = new Vertex(0, 'person', {
      name: [new VertexProperty(0, 'name', 'Fred')],
      roles: [new VertexProperty(0, 'role', 'hunter'), new VertexProperty(0, 'role', 'gatherer')],
      weapons: [new VertexProperty(0, 'weapon', 'spear'), new VertexProperty(0, 'weapon', 'spear')],
    });

    const parsedVertex = parseVertex(vertex, { name: 'single', roles: 'set', weapons: 'list' });

    expect(parsedVertex.name).to.equals('Fred');
    expect(parsedVertex.roles).to.be.an.instanceOf(Set).to.have.all.keys(['hunter', 'gatherer']);
    expect(parsedVertex.weapons).to.be.an.instanceOf(Array).to.includes.members(['spear', 'spear']);
  });
});
