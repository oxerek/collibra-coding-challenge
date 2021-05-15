package com.collibra.codechallenge.graph

import spock.lang.Specification

class AddingAndRemovingNodesTest extends Specification {

    def "should correctly add node"() {

        setup: "empty graph"
        def graph = new DirectedWeightedGraph([:], null)

        when: "adding node"
        graph.addNode("node")

        then: "node added"
        graph.nodes == ["node":[:]]
    }

    def "should throw exception when node was already added"() {

        setup: "graph with one node"
        def graph = new DirectedWeightedGraph(["node":[:]], null)

        when: "adding same node second time"
        graph.addNode("node")

        then: "NodeAlreadyExistsException was thrown"
        def exception = thrown(RuntimeException)
        exception?.class == Graph.NodeAlreadyExistsException
    }

    def "should correctly remove node"() {

        setup: "graph with two nodes and one adjacent node"
        def graph = new DirectedWeightedGraph(["node":[:], "node1":["node":[0]]], null)

        when: "removing node"
        graph.removeNode("node")

        then: "node removed from nodes and from adjacents"
        graph.nodes == ["node1":[:]]
    }

    def "should throw exception when node to remove does not exist"() {

        setup: "empty graph"
        def graph = new DirectedWeightedGraph([:], null)

        when: "removing nonexistent node"
        graph.removeNode("node")

        then: "NodeNotFoundException was thrown"
        def exception = thrown(RuntimeException)
        exception?.class == Graph.NodeNotFoundException
    }
}
