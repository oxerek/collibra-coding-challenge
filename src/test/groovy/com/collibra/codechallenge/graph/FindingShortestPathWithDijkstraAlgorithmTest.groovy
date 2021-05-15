package com.collibra.codechallenge.graph

import spock.lang.Specification
import spock.lang.Unroll

class FindingShortestPathWithDijkstraAlgorithmTest extends Specification {

    def nodes = [
            "node" : ["node1": [3], "node2": [1]],
            "node1": ["node3": [5], "node4": [7]],
            "node2": ["node1": [2], "node5": [5]],
            "node3": [:],
            "node4": ["node6": [10], "node7": [2]],
            "node5": ["node7": [4], "node8": [8]],
            "node6": [:],
            "node7": ["node8": [3]],
            "node8": [:],
            "node9": [:]
    ]

    def graph = new DirectedWeightedGraph(nodes, new DijkstraAlgorithmShortestPathResolver(nodes))

    @Unroll
    def "should correctly resolve shortest path between #initial and #terminal"() {

        when: "resolving shortest path"
        def distance = graph.shortestPath(initial, terminal)

        then: "smallest weight correctly resolved"
        distance == result

        where:
        initial | terminal  || result
        "node"  | "node4"   || 10
        "node"  | "node"    || 0
        "node"  | "node5"   || 6
        "node"  | "node2"   || 1
        "node"  | "node3"   || 8
        "node"  | "node8"   || 13
        "node"  | "node9"   || Integer.MAX_VALUE
        "node"  | "node6"   || 20
        "node"  | "node7"   || 10
        "node"  | "node1"   || 3
    }

    @Unroll
    def "should correctly gather nodes closer to #initial than #distance"() {

        when: "finding nodes closer than given distance"
        def nodes = graph.closerThan(initial, distance)

        then: "correct list of nodes gathered"
        nodes == adjacents

        where:
        initial | distance  || adjacents
        "node"  | 11        || "node1,node2,node3,node4,node5,node7"
        "node"  | 13        || "node1,node2,node3,node4,node5,node7"
        "node"  | 14        || "node1,node2,node3,node4,node5,node7,node8"
        "node"  | 0         || ""
        "node"  | -10       || ""
        "node1" | 3         || ""
        "node1" | 6         || "node3"
        "node1" | 12        || "node3,node4,node7"
        "node1" | 13        || "node3,node4,node7,node8"
    }
}
