package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Scatter2DChart;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.*;

public class EnergyWriter implements ParticlesWriter{

    List<Point2D> points;
    final double epsilon;
    final double rm;

    public EnergyWriter(double epsilon, double rm){
        this.epsilon = epsilon;
        this.rm = rm;
        this.points = new ArrayList<>();
        Scatter2DChart.initialize("Energía del sistema a lo largo del tiempo",
                "tiempo [s]", 0, 5, 1,
                "energía [J]", 4000, 7000, 250);
    }

    @Override
    public void write(double time, List<Particle> particles) throws IOException {

    }

    @Override
    public void write(double time, Map<Particle, Set<Neighbour>> neighbours) throws IOException {
        double kinetic = neighbours.keySet().stream()
                .mapToDouble(p -> kineticEnergy(p)).sum();
        double potential = neighbours.entrySet().stream()
                .mapToDouble(e -> potentialEnergy(e.getKey(),e.getValue())).sum();
        double energy = kinetic + potential;
        points.add(new Point2D(time, energy));
    }

    private double kineticEnergy(Particle particle){
        return 0.5 * particle.mass() * particle.velocity().magnitude() * particle.velocity().magnitude();
    }

    private double potentialEnergy(Particle particle, Set<Neighbour> neighbours){
        double potential = 0;
        double rm7 = Math.pow(rm,7);
        double rm13 = Math.pow(rm,13);
        for(Neighbour neighbour : neighbours){
            potential += 12 * epsilon * (1.0/rm) *
                    (rm7 / (6 * Math.pow(neighbour.getDistance(),6)) -
                            rm13 / (12 * Math.pow(neighbour.getDistance(),12)));
        }
        return potential;
    }

    public void addSeries(String name){
        Platform.runLater(() -> Scatter2DChart.addSeries(name, points));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clear(){
        points = new LinkedList<>();
    }
}
