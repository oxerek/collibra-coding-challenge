package com.collibra.codechallenge.ioc;

import com.collibra.codechallenge.graph.Graph;
import com.collibra.codechallenge.graph.ShortestPathResolver;
import com.collibra.codechallenge.protocol.Protocol;
import com.collibra.codechallenge.server.ChannelHandler;
import com.collibra.codechallenge.server.Server;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class Configuration {

    public static Server server() {
        return new Server();
    }

    public static ChannelHandler channelHandler(Map<String, Map<String, List<Integer>>> nodes) {
        return new ChannelHandler(nodes);
    }

    public static Protocol graphMessagesProtocol(Map<String, Map<String, List<Integer>>> nodes) {
        return InstancesFactory.instance(
                Protocol.class,
                "GraphMessagesProtocol",
                Lists.newArrayList(directedWeightedGraph(nodes, dijkstraAlgorithmShortestPathResolver(nodes))),
                Lists.newArrayList(Graph.class));
    }

    @SuppressWarnings("unchecked")
    public static ShortestPathResolver<String> dijkstraAlgorithmShortestPathResolver(Map<String, Map<String, List<Integer>>> nodes) {
        return InstancesFactory.instance(
                ShortestPathResolver.class,
                "DijkstraAlgorithmShortestPathResolver",
                Lists.newArrayList(nodes),
                Lists.newArrayList(Map.class));
    }

    @SuppressWarnings("unchecked")
    public static Graph<String> directedWeightedGraph(Map<String, Map<String, List<Integer>>> nodes, ShortestPathResolver<String> shortestPathResolver) {
        return InstancesFactory.instance(
                Graph.class,
                "DirectedWeightedGraph",
                Lists.newArrayList(nodes, shortestPathResolver),
                Lists.newArrayList(Map.class, ShortestPathResolver.class));
    }
}
