package com.collibra.codechallenge.graph;

import java.util.List;
import java.util.Map;

public interface Graph<NODE> {

    Map<NODE, Map<NODE, List<Integer>>> nodes();

    void addNode(NODE name) throws NodeAlreadyExistsException;

    void removeNode(NODE name) throws NodeNotFoundException;

    void addEdge(NODE initial, NODE terminal, Integer weight) throws NodeNotFoundException;

    void removeEdges(NODE initial, NODE terminal) throws NodeNotFoundException;

    Integer shortestPath(NODE initial, NODE terminal) throws NodeNotFoundException;

    String closerThan(NODE terminal, Integer distance) throws NodeNotFoundException;

    default void validateNodeNotExists(NODE node) {
        if(nodes().containsKey(node)) {
            throw new NodeAlreadyExistsException();
        }
    }

    default void validateNodeExists(NODE node) {
        if(!nodes().containsKey(node)) {
            throw new NodeNotFoundException();
        }
    }

    class NodeAlreadyExistsException extends RuntimeException {

        NodeAlreadyExistsException() {
            super("ERROR: NODE ALREADY EXISTS" + System.lineSeparator());
        }
    }

    class NodeNotFoundException extends RuntimeException {

        NodeNotFoundException() {
            super("ERROR: NODE NOT FOUND" + System.lineSeparator());
        }
    }
}
