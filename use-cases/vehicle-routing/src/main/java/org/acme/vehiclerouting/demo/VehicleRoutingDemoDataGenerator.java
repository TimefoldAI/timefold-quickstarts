package org.acme.vehiclerouting.demo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.vehiclerouting.dto.input.VehicleRoutePlanConfigOverrides;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;

@ApplicationScoped
public class VehicleRoutingDemoDataGenerator implements DemoDataGenerator {

    private static final List<String> TAGS =
            List.of("vehicle capacity", "time windows", "unassigned visits", "driving time");

    private static final String LONG_DESCRIPTION_TEMPLATE = """
            Routes %d vehicles from their own home locations past %d visits around %s, all on the same day. \
            Each vehicle leaves at 07:30 and carries a limited amount of demand over its whole route; each visit \
            takes 10 to 40 minutes of servicing and only accepts a vehicle inside a morning or an afternoon time \
            window. Once solved, every vehicle drives one route that stays within its capacity and inside the time \
            windows of its visits, with as few visits left unserviced and as little driving time as possible.""";

    /**
     * The demo datasets, keyed by their id, in the order the UI's data picker lists them.
     */
    private static final Map<String, Dataset> DATASETS = datasets();

    private static Map<String, Dataset> datasets() {
        Map<String, Dataset> datasets = new LinkedHashMap<>();
        datasets.put("PHILADELPHIA", new Dataset("Philadelphia", 55, DemoDataBuilder::philadelphia));
        datasets.put("GHENT", new Dataset("Ghent", 65, DemoDataBuilder::ghent));
        datasets.put("HARTFORT", new Dataset("Hartfort", 50, DemoDataBuilder::hartfort));
        datasets.put("FIRENZE", new Dataset("Firenze", 77, DemoDataBuilder::firenze));
        return datasets;
    }

    @Override
    public List<DemoMetaData> demoMetaData() {
        return DATASETS.entrySet().stream()
                .map(entry -> metaData(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public DemoData generateDemoData(String id) {
        Dataset dataset = DATASETS.get(id);
        if (dataset == null) {
            throw new IllegalArgumentException("Unknown demo data id (%s).".formatted(id));
        }
        VehicleRoutePlanInput problem = dataset.problemSupplier().get();
        Configuration<VehicleRoutePlanConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(id), ModelConfig.empty());
        return new DemoData(metaData(id, dataset), new ModelRequest<>(configuration, problem));
    }

    private static DemoMetaData metaData(String id, Dataset dataset) {
        return new DemoMetaData(id,
                "%s: %d visits for 6 vehicles.".formatted(dataset.cityName(), dataset.visitCount()),
                LONG_DESCRIPTION_TEMPLATE.formatted(6, dataset.visitCount(), dataset.cityName()),
                TAGS,
                List.of());
    }

    private record Dataset(String cityName, int visitCount, Supplier<VehicleRoutePlanInput> problemSupplier) {
    }
}
