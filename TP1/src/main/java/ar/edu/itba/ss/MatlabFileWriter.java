package ar.edu.itba.ss;

import javafx.geometry.Point2D;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MatlabFileWriter {

    public void writeNeighbourParticlesFile(Map<Particle, Point2D> positions, Map<Particle, List<Neighbour>> neighbours){
        writeNeighbourParticlesFile("./default",positions,neighbours);
    }

    public void writeNeighbourParticlesFile(String filename, Map<Particle, Point2D> positions, Map<Particle, List<Neighbour>> neighbours){
        positions = positions.entrySet().stream().sorted(new Comparator<Map.Entry<Particle, Point2D>>() {
            @Override
            public int compare(Map.Entry<Particle, Point2D> o1, Map.Entry<Particle, Point2D> o2) {
                return Integer.valueOf(o1.getKey().getId()).compareTo(o2.getKey().getId());
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        neighbours = neighbours.entrySet().stream().sorted(new Comparator<Map.Entry<Particle, List<Neighbour>>>() {
            @Override
            public int compare(Map.Entry<Particle, List<Neighbour>> o1, Map.Entry<Particle, List<Neighbour>> o2) {
                return o1.getKey().getId() - o2.getKey().getId();
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".m"))){
            String positionsStr = "positions = [";
            for(Point2D position : positions.values()){
                positionsStr += position.getX() + " " + position.getY() + "; ";
            }
            positionsStr += "];\n";

            String neighboursStr = "neighbours = {";
            for(List<Neighbour> neighbourList : neighbours.values()){
                neighboursStr += "[";
                for(Neighbour neighbour : neighbourList){
                    neighboursStr += neighbour.getNeighbourParticle().getId()+1 + ", ";
                }
                neighboursStr += "], ";
            }
            neighboursStr += "};\n";

            bw.write(positionsStr + neighboursStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
