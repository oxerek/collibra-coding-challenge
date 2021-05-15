package com.collibra.codechallenge.graph;

import java.util.List;

public interface ShortestPathResolver<NODE> {

    Integer shortestPath(NODE initial, NODE terminal);

    List<NODE> closerThan(Integer weight, NODE initial);
}
