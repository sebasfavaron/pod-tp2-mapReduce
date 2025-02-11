package itba.client.query;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import itba.client.Writer;
import itba.model.Movement;
import itba.model.query2.g10CabotageMovementsPerAirlineCombiner;
import itba.model.query2.g10CabotageMovementsPerAirlineMapper;
import itba.model.query2.g10CabotageMovementsPerAirlineReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Query2 implements Query {

    private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);

    private HazelcastInstance hazelcastInstance;
    private IList<Movement> movements;
    private Integer n;
    private Writer writer;

    public Query2(final HazelcastInstance hazelcastInstance, final IList<Movement> movements, final Integer n, final String outPath) {
        this.hazelcastInstance = hazelcastInstance;
        this.movements = movements;
        this.n = n;
        this.writer = new Writer(outPath + "query2.csv");
    }

    @Override
    public void run() throws InterruptedException, ExecutionException {

        JobTracker jobTracker = hazelcastInstance.getJobTracker("Query2");

        KeyValueSource<String, Movement> source = KeyValueSource.fromList(movements);
        Job<String, Movement> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Integer>> completableFuture = job
                .mapper(new g10CabotageMovementsPerAirlineMapper())
                .combiner(new g10CabotageMovementsPerAirlineCombiner())
                .reducer(new g10CabotageMovementsPerAirlineReducer())
                .submit();

        Map<String, Integer> cabotageMovementsPerAirline = completableFuture.get();

        AtomicInteger totalCabotageFlights = getTotalCabotageFlights(cabotageMovementsPerAirline);

        printResults(cabotageMovementsPerAirline, totalCabotageFlights.get());
    }

    private AtomicInteger getTotalCabotageFlights(Map<String, Integer> cabotageMovementsPerAirline) {
        AtomicInteger totalCabotageFlights = new AtomicInteger();

        cabotageMovementsPerAirline.forEach((key, value) -> {
            totalCabotageFlights.addAndGet(value);
        });

        return totalCabotageFlights;
    }

    private void printResults(Map<String, Integer> airlanes, Integer totalCabotageFlights) {
        AtomicInteger topFlights = new AtomicInteger();

        final Map<String, Integer> topAirlines = airlanes.entrySet()
                .stream()
                .filter(airline -> !airline.getKey().equals("N/A") && !airline.getKey().equals(""))
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed().thenComparing(Map.Entry::getKey))
                .limit(this.n)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        writer.writeString("Aerolínea;Porcentaje\n");

        topAirlines.forEach((key, value) -> {
            topFlights.addAndGet(value);
            double percentage = (double) value / totalCabotageFlights;
            printLine(key, percentage * 100);
        });

        if (topFlights.get() != totalCabotageFlights) {
            double percentage = (double) topFlights.get() / totalCabotageFlights;
            printLine("Otros", (1 - percentage) * 100);
        }
    }

    private void printLine(String airlane, Double percentage) {
        writer.writeString(airlane + ";" + String.format("%.2f", percentage) + "%\n");
    }
}
