package com.collibra.codechallenge.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class DirectedWeightedGraph implements Graph<String> {

    private final Map<String, Map<String, List<Integer>>> nodes;
    private final ShortestPathResolver<String> shortestPathResolver;

    @Override
    public Map<String, Map<String, List<Integer>>> nodes() {
        return nodes;
    }

    @Override
    public void addNode(String name) throws NodeAlreadyExistsException {
        validateNodeNotExists(name);

        nodes.putIfAbsent(name, Maps.newHashMap());
    }

    @Override
    public void removeNode(String name) throws NodeNotFoundException {
        validateNodeExists(name);

        nodes.values().stream().forEach(node -> node.remove(name));
        nodes.remove(name);
    }

    @Override
    public void addEdge(String initial, String terminal, Integer weight) throws NodeNotFoundException {
        validateNodeExists(initial);
        validateNodeExists(terminal);

        nodes.get(initial).computeIfAbsent(terminal, node -> Lists.newArrayList()).add(weight);
    }

    @Override
    public void removeEdges(String initial, String terminal) throws NodeNotFoundException {
        validateNodeExists(initial);
        validateNodeExists(terminal);

        nodes.get(initial).remove(terminal);
    }

    @Override
    public Integer shortestPath(String initial, String terminal) throws NodeNotFoundException {
        validateNodeExists(initial);
        validateNodeExists(terminal);

        nodes().computeIfAbsent(initial, n -> {
            throw new NodeNotFoundException();
        });
        nodes().computeIfAbsent(terminal, n -> {
            throw new NodeNotFoundException();
        });

        return shortestPathResolver.shortestPath(initial, terminal);
    }

    @Override
    public String closerThan(String initial, Integer distance) throws NodeNotFoundException {
        validateNodeExists(initial);

        return shortestPathResolver.closerThan(distance, initial).stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Override
    public void validateNodeExists(String node) {
        nodes.computeIfAbsent(node, key -> {
            throw new NodeNotFoundException();
        });
    }

    @Override
    public void validateNodeNotExists(String node) {
        nodes.computeIfPresent(node, (key, currentValue) -> {
            throw new NodeAlreadyExistsException();
        });
    }
}
