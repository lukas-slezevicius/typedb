/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package hypergraph.graph.vertex.impl;

import hypergraph.common.exception.Error;
import hypergraph.common.exception.HypergraphException;
import hypergraph.graph.ThingGraph;
import hypergraph.graph.adjacency.Adjacency;
import hypergraph.graph.adjacency.ThingAdjacency;
import hypergraph.graph.adjacency.impl.ThingAdjacencyImpl;
import hypergraph.graph.edge.Edge;
import hypergraph.graph.util.IID;
import hypergraph.graph.util.Schema;
import hypergraph.graph.vertex.AttributeVertex;

import java.time.LocalDateTime;

public abstract class AttributeVertexImpl<VALUE> extends ThingVertexImpl implements AttributeVertex<VALUE> {

    private final IID.Vertex.Attribute<VALUE> attributeIID;

    AttributeVertexImpl(ThingGraph graph, IID.Vertex.Attribute<VALUE> iid, boolean isInferred) {
        super(graph, iid, isInferred);
        this.attributeIID = iid;
    }

    protected abstract IID.Index.Attribute index();

    @Override
    protected ThingAdjacency newAdjacency(Adjacency.Direction direction) {
        return new ThingAdjacencyImpl.Persisted(this, direction);
    }

    @Override
    public IID.Vertex.Attribute<VALUE> iid() {
        return attributeIID;
    }

    @Override
    public Schema.Status status() {
        return Schema.Status.IMMUTABLE;
    }

    @Override
    public VALUE value() {
        if (type().valueType().isIndexable()) {
            return attributeIID.value();
        } else {
            // TODO: implement for ValueType.TEXT
            return null;
        }
    }

    @Override
    public void delete() {
        graph.storage().delete(attributeIID.bytes());
        graph.delete(this);
    }

    /**
     * Commits this vertex to be persisted onto storage.
     *
     * This method is not thread-safe. It uses needs to access and manipulate
     * {@code AttributeSync} which is not a thread-safe object.
     */
    @Override
    public void commit() {
        if (isInferred) throw new HypergraphException(Error.Transaction.ILLEGAL_OPERATION);
        graph.storage().putUntracked(attributeIID.bytes());
        graph.storage().putUntracked(index().bytes(), attributeIID.bytes());
        outs.forEach(Edge::commit);
        ins.forEach(Edge::commit);
    }

    public static class Boolean extends AttributeVertexImpl<java.lang.Boolean> {

        public Boolean(ThingGraph graph, IID.Vertex.Attribute<java.lang.Boolean> iid, boolean isInferred) {
            super(graph, iid, isInferred);
        }

        @Override
        protected IID.Index.Attribute index() {
            return IID.Index.Attribute.of(value(), type().iid());
        }
    }

    public static class Long extends AttributeVertexImpl<java.lang.Long> {

        public Long(ThingGraph graph, IID.Vertex.Attribute<java.lang.Long> iid, boolean isInferred) {
            super(graph, iid, isInferred);
        }

        @Override
        protected IID.Index.Attribute index() {
            return IID.Index.Attribute.of(value(), type().iid());
        }
    }

    public static class Double extends AttributeVertexImpl<java.lang.Double> {

        public Double(ThingGraph graph, IID.Vertex.Attribute<java.lang.Double> iid, boolean isInferred) {
            super(graph, iid, isInferred);
        }

        @Override
        protected IID.Index.Attribute index() {
            return IID.Index.Attribute.of(value(), type().iid());
        }
    }

    public static class String extends AttributeVertexImpl<java.lang.String> {

        public String(ThingGraph graph, IID.Vertex.Attribute<java.lang.String> iid, boolean isInferred) {
            super(graph, iid, isInferred);
        }

        @Override
        protected IID.Index.Attribute index() {
            return IID.Index.Attribute.of(value(), type().iid());
        }
    }

    public static class DateTime extends AttributeVertexImpl<java.time.LocalDateTime> {

        public DateTime(ThingGraph graph, IID.Vertex.Attribute<LocalDateTime> iid, boolean isInferred) {
            super(graph, iid, isInferred);
        }

        @Override
        protected IID.Index.Attribute index() {
            return IID.Index.Attribute.of(value(), type().iid());
        }
    }
}
