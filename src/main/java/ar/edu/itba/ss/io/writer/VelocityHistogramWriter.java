package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.Criteria;

import java.io.IOException;
import java.util.List;

public class VelocityHistogramWriter implements ParticlesWriter{

    private final int amountOfCategories;
    private final Criteria criteria;

    public VelocityHistogramWriter(int amountOfCategories, Criteria criteria) {
        this.amountOfCategories = amountOfCategories;
        this.criteria = criteria;
    }

    @Override
    public void write(double time, List<Particle> particles) throws IOException {
        if(criteria.test(time,particles)){
            int[] categories = calculateCategories(particles);
            printCategories(particles, categories);
        }
    }

    private int[] calculateCategories(List<Particle> particles){
        int[] categories = new int[amountOfCategories];
        for(Particle particle : particles){
            final double velocity = particle.velocity().magnitude();
            boolean alreadyInCategory = false;
            for (int i = 1; i < amountOfCategories; i++) {
                if(velocity < i && !alreadyInCategory){
                    categories[i-1]++;
                    alreadyInCategory = true;
                }
            }
            if(!alreadyInCategory){
                categories[amountOfCategories-1]++;
            }
        }
        return categories;
    }

    private void printCategories(List<Particle> particles, int[] categories){
        final int amountOfParticles = particles.size();
        System.out.println("--------------------");
        for (int i = 0; i < amountOfCategories - 1; i++) {
            System.out.println("Between " + i + " and " + (i+1) + ":");
            System.out.println("\t" + (double)categories[i]/amountOfParticles);
        }
        System.out.println("More than " + (amountOfCategories - 1) + ":");
        System.out.println("\t" + (double)categories[amountOfCategories - 1]/amountOfParticles);
        System.out.println("--------------------");
    }
}
