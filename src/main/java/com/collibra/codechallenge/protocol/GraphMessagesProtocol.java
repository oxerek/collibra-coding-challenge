package com.collibra.codechallenge.protocol;

import com.collibra.codechallenge.graph.Graph;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

class GraphMessagesProtocol implements Protocol {

    public GraphMessagesProtocol(Graph<String> graph) {
        this.graph = graph;
    }

    private Graph<String> graph;

    private String clientName;

    @Override
    public String helloMessage(String sessionId) {
        return OutboundMessages.HI_I_AM.format(sessionId);
    }

    @Override
    public String byeMessage(String sessionTime) {
        return OutboundMessages.BYE.format(clientName, sessionTime);
    }

    @Override
    public String processMessage(String message, String sessionTime) {
        return InboundMessages.matches(message).map(inboundMessage -> Match(inboundMessage).of(
                Case($(InboundMessages.HI_I_AM), hiIAm),
                Case($(InboundMessages.BYE_MATE), (Function1<String, String>) msg -> byeMessage(sessionTime)),
                Case($(InboundMessages.ADD_NODE), addNode),
                Case($(InboundMessages.REMOVE_NODE), removeNode),
                Case($(InboundMessages.ADD_EDGE), addEdge),
                Case($(InboundMessages.REMOVE_EDGE), removeEdge),
                Case($(InboundMessages.SHORTEST_PATH), shortestPath),
                Case($(InboundMessages.CLOSER_THAN), closerThan)
        ).apply(message)).orElse(OutboundMessages.SORRY.format());
    }

    private final Function1<String, String> hiIAm = (Function1<String, String>) message -> {
        clientName = InboundMessages.HI_I_AM.find(message).group(1);
        return OutboundMessages.HI.format(clientName);
    };

    private final Function1<String, String> addNode = message -> {
        graph.addNode(InboundMessages.ADD_NODE.find(message).group(1));
        return OutboundMessages.NODE_ADDED.format();
    };

    private final Function1<String, String> removeNode = message -> {
        graph.removeNode(InboundMessages.REMOVE_NODE.find(message).group(1));
        return OutboundMessages.NODE_REMOVED.format();
    };

    private final Function1<String, String> addEdge = message -> {
        Matcher matcher = InboundMessages.ADD_EDGE.find(message);
        graph.addEdge(matcher.group(1), matcher.group(2), Integer.valueOf(matcher.group(3)));
        return OutboundMessages.EDGE_ADDED.format();
    };

    private final Function1<String, String> removeEdge = message -> {
        Matcher matcher = InboundMessages.REMOVE_EDGE.find(message);
        graph.removeEdges(matcher.group(1), matcher.group(2));
        return OutboundMessages.EDGE_REMOVED.format();
    };

    private final Function1<String, String> shortestPath = message -> {
        Matcher matcher = InboundMessages.SHORTEST_PATH.find(message);
        Integer weight = graph.shortestPath(matcher.group(1), matcher.group(2));
        return OutboundMessages.WEIGHT.format(weight);
    };

    private final Function1<String, String> closerThan = message -> {
        Matcher matcher = InboundMessages.CLOSER_THAN.find(message);
        String nodes = graph.closerThan(matcher.group(2), Integer.valueOf(matcher.group(1)));
        return OutboundMessages.NODES.format(nodes);
    };

    @RequiredArgsConstructor
    private enum InboundMessages {

        HI_I_AM("HI, I AM ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        BYE_MATE("BYE MATE!" + System.lineSeparator()),
        ADD_NODE("ADD NODE ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        REMOVE_NODE("REMOVE NODE ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        ADD_EDGE("ADD EDGE ([A-Za-z0-9/-]+) ([A-Za-z0-9/-]+) ([0-9]+)" + System.lineSeparator()),
        REMOVE_EDGE("REMOVE EDGE ([A-Za-z0-9/-]+) ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        SHORTEST_PATH("SHORTEST PATH ([A-Za-z0-9/-]+) ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        CLOSER_THAN("CLOSER THAN ([0-9]+) ([A-Za-z0-9/-]+)" + System.lineSeparator()),
        ;

        private final String regex;

        Matcher find(String message) {
            Matcher matcher = Pattern.compile(regex).matcher(message);
            matcher.find();
            return matcher;
        }

        static Optional<InboundMessages> matches(String message) {
            return Stream.of(InboundMessages.values())
                    .filter(inboundMessages -> Pattern.compile(inboundMessages.regex).matcher(message).matches())
                    .findFirst();
        }
    }

    @RequiredArgsConstructor
    private enum OutboundMessages {

        HI_I_AM("HI, I AM %s" + System.lineSeparator()),
        HI("HI %s" + System.lineSeparator()),
        BYE("BYE %s, WE SPOKE FOR %s MS" + System.lineSeparator()),
        NODE_ADDED("NODE ADDED" + System.lineSeparator()),
        SORRY("SORRY, I DID NOT UNDERSTAND THAT" + System.lineSeparator()),
        NODE_REMOVED("NODE REMOVED" + System.lineSeparator()),
        EDGE_ADDED("EDGE ADDED" + System.lineSeparator()),
        EDGE_REMOVED("EDGE REMOVED" + System.lineSeparator()),
        WEIGHT("%s" + System.lineSeparator()),
        NODES("%s" + System.lineSeparator()),
        ;

        private final String format;

        String format(Object... value) {
            return String.format(format, value);
        }
    }
}
