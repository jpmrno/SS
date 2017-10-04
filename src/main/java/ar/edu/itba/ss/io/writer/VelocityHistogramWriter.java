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
            double[] categories = setCategories(particles);
            int[] distribution = calculateDistribution(particles,categories);
            print(particles, distribution, categories);
        }
    }

    private int[] calculateDistribution(List<Particle> particles, double[] categories){
        int[] distribution = new int[amountOfCategories];
        for(Particle particle : particles){
            final double velocity = particle.velocity().magnitude();
            boolean alreadyInCategory = false;
            for (int i = 1; i < amountOfCategories; i++) {
                if(velocity < categories[i] && !alreadyInCategory){
                    distribution[i-1]++;
                    alreadyInCategory = true;
                }
            }
            if(!alreadyInCategory){
                distribution[amountOfCategories-1]++;
            }
        }
        return distribution;
    }

    private void print(List<Particle> particles, int[] distributions, double[] categories){
        final int amountOfParticles = particles.size();
        System.out.println("--------------------");
        for (int i = 0; i < amountOfCategories - 1; i++) {
            System.out.println("Category " + categories[i] + ":");
            System.out.println("\t" + (double)distributions[i]/amountOfParticles);
        }
        System.out.println("More than " + categories[amountOfCategories - 1] + ":");
        System.out.println("\t" + (double)distributions[amountOfCategories - 1]/amountOfParticles);
        System.out.println("--------------------");
    }

    private double[] setCategories(List<Particle> particles){
        double[] categories = new double[amountOfCategories];
        double max = particles.stream().mapToDouble(p->p.velocity().magnitude()).max().getAsDouble();
        double min = particles.stream().mapToDouble(p->p.velocity().magnitude()).min().getAsDouble();
        double delta = (max - min)/amountOfCategories;
        double deltaAccum = 0;
        for (int i = 0; i < amountOfCategories; i++) {
            categories[i] = deltaAccum;
            deltaAccum += delta;
        }
        return categories;
    }
}
