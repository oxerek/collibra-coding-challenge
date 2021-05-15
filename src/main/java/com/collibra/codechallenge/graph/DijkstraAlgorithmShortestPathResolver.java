package com.collibra.codechallenge.graph;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class DijkstraAlgorithmShortestPathResolver implements ShortestPathResolver<String> {

    private final Map<String, Map<String, List<Integer>>> nodes;

    @Override
    public Integer shortestPath(String initial, String terminal) {
        return new DijkstraShortestPathAlgorithm(nodes).shortestPath(initial, terminal);
    }

    @Override
    public List<String> closerThan(Integer weight, String initial) {
        return new DijkstraShortestPathAlgorithm(nodes).closerThan(weight, initial);
    }

    @RequiredArgsConstructor
    private class DijkstraShortestPathAlgorithm {

        private final Map<String, Map<String, List<Integer>>> nodes;

        private Set<String> settled;
        private Set<String> unsettled;

        private Map<String, DistanceAndShortestPath> shortestPathsAndDistances;

        Integer shortestPath(String initial, String terminal) {
            initializeAlgorithm();
            initialNodeWithZeroAndOthersWithMaxDistance(initial);
            addInitialNodeToUnsettled(initial);
            whileUnsettledIsNotEmptyCalculate();
            return getShortestPathFromResults(terminal);
        }

        List<String> closerThan(Integer weight, String initial) {
            initializeAlgorithm();
            initialNodeWithZeroAndOthersWithMaxDistance(initial);
            addInitialNodeToUnsettled(initial);
            whileUnsettledIsNotEmptyCalculate();
            return chooseCloserThanFromResults(weight, initial);
        }

        private void initializeAlgorithm() {
            settled = Sets.newHashSet();
            unsettled = Sets.newHashSet();
            shortestPathsAndDistances = Maps.newHashMap();
        }

        private void initialNodeWithZeroAndOthersWithMaxDistance(String initial) {
            nodes.keySet().stream()
                    .forEach(node -> shortestPathsAndDistances.put(node, DistanceAndShortestPath
                            .createWithZeroDistanceForInitialNodeOrWithMaxDistanceForOthers(initial, node)));
        }

        private void addInitialNodeToUnsettled(String initial) {
            unsettled.add(initial);
        }

        private void whileUnsettledIsNotEmptyCalculate() {
            while (unsettled.size() != 0) {
                String current = chooseNodeWithLowestDistanceFromSource(unsettled);
                unsettled.remove(current);
                for (Map.Entry<String, List<Integer>> adjacents : nodes.get(current).entrySet()) {
                    String adjacent = adjacents.getKey();
                    for(Integer weight : adjacents.getValue()) {
                        if (!settled.contains(adjacent)) {
                            calculateShortestPathAndMinimumDistance(adjacent, weight, current);
                            unsettled.add(adjacent);
                        }
                    }
                }
                settled.add(current);
            }
        }

        private String chooseNodeWithLowestDistanceFromSource(Set<String> unsettled) {
            String lowestDistanceNode = null;
            Integer lowestDistance = Integer.MAX_VALUE;
            for (String node: unsettled) {
                Integer nodeDistance = shortestPathsAndDistances.get(node).getDistance();
                if (nodeDistance < lowestDistance) {
                    lowestDistance = nodeDistance;
                    lowestDistanceNode = node;
                }
            }
            return lowestDistanceNode;
        }

        private void calculateShortestPathAndMinimumDistance(String current, Integer weight, String source) {
            DistanceAndShortestPath sourceDistanceAndShortestPath = shortestPathsAndDistances.get(source);
            DistanceAndShortestPath currentDistanceAndShortestPath = shortestPathsAndDistances.get(current);
            if (sourceDistanceAndShortestPath.getDistance() + weight < currentDistanceAndShortestPath.getDistance()) {
                currentDistanceAndShortestPath.setDistance(sourceDistanceAndShortestPath.getDistance() + weight);
                currentDistanceAndShortestPath.addAll(sourceDistanceAndShortestPath.getShortestPath());
                currentDistanceAndShortestPath.add(source);
            }
        }

        private Integer getShortestPathFromResults(String terminal) {
            return shortestPathsAndDistances.get(terminal).getDistance();
        }

        private List<String> chooseCloserThanFromResults(Integer weight, String initial) {
            Set<String> closerThan = Sets.newHashSet();
            for(Map.Entry<String, DistanceAndShortestPath> shortestPathAndDistance : shortestPathsAndDistances.entrySet()) {
                if(shortestPathAndDistance.getValue().getDistance() < weight && !initial.equals(shortestPathAndDistance.getKey())) {
                    closerThan.add(shortestPathAndDistance.getKey());
                    closerThan.addAll(shortestPathAndDistance.getValue().getShortestPath().stream()
                            .filter(node -> !node.equals(initial))
                            .collect(Collectors.toList()));
                }
            }
            return closerThan.stream().collect(Collectors.toList());
        }

    }

    @Getter
    @Setter
    @EqualsAndHashCode
    private static class DistanceAndShortestPath {

        @Delegate
        private Set<String> shortestPath = Sets.newHashSet();
        private Integer distance;

        DistanceAndShortestPath(Integer distance) {
            this.distance = distance;
        }

        static DistanceAndShortestPath createWithZeroDistanceForInitialNodeOrWithMaxDistanceForOthers(String initial, String node) {
            return node.equals(initial) ? new DistanceAndShortestPath(0) : new DistanceAndShortestPath(Integer.MAX_VALUE);
        }
    }
}
