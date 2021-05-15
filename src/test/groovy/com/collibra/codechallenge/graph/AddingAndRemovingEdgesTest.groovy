package com.collibra.codechallenge.graph

import spock.lang.Specification
import spock.lang.Unroll

class AddingAndRemovingEdgesTest extends Specification {

    def "should correctly add edge"() {

        given: "graph with two nodes"
        def graph = new DirectedWeightedGraph(["node":[:], "node1":[:]], null)

        when: "adding edge"
        graph.addEdge("node", "node1", 0)

        then: "edge added"
        graph.nodes == ["node":["node1":[0]], "node1":[:]]
    }

    @Unroll
    def "should throw exception when initial: '#initial' or terminal: '#terminal' node not found"() {

        setup: "graph"
        def graph = new DirectedWeightedGraph(nodes, null)

        when: "adding edge to nonexistent node"
        graph.addEdge("node", "node1", 0)

        then: "NodeNotFoundException was thrown"
        def exception = thrown(RuntimeException)
        exception?.class == Graph.NodeNotFoundException

        where:
        nodes                       | initial   | terminal
        ["node1":[:], "node2":[:]]  | "node"    | "node1"
        ["node":[:], "node2":[:]]   | "node"    | "node1"
        [:]                         | "node"    | "node1"
    }

    def "should correctly remove edges"() {

        given: "graph with three nodes and five edges"
        def graph = new DirectedWeightedGraph(["node":["node1":[1, 3], "node2":[5, 7, 9]], "node1":[:], "node2":[:]], null)

        when: "removing edges"
        graph.removeEdges("node", "node1")

        then: "edges removed"
        graph.nodes == ["node":["node2":[5,7,9]], "node1":[:], "node2":[:]]
    }
}
