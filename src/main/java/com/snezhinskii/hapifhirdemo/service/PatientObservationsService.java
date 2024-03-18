package com.snezhinskii.hapifhirdemo.service;

import com.snezhinskii.hapifhirdemo.api.mapper.ObservationMapper;
import lombok.extern.log4j.Log4j2;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class PatientObservationsService {
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PatientObservationsBundleService bundleService;
    private final CsvWriterService csvWriterService;
    private final Executor executor;

    @Value("${hapi-fhir.handle-timeout:12}")
    private int handleTimeout; //sec

    public PatientObservationsService(
        PatientObservationsBundleService bundleService,
        CsvWriterService csvWriterService,
        @Qualifier("hapiFhirTaskExecutor") Executor executor
    ) {
        this.bundleService = bundleService;
        this.csvWriterService = csvWriterService;
        this.executor = executor;
    }

    public CompletableFuture<List<Observation>> retrieveAndExport(String patientId, String csvFilePath) {
        /**
         * This map is needed to ensure that the resulting list contains rows
         * according to the sequence of pages
         */
        final ConcurrentHashMap<Integer, List<Observation>> accumulatorMap = new ConcurrentHashMap<>();

        final CompletableFuture<List<Observation>> futureObservations = CompletableFuture.supplyAsync(() -> {
                final Bundle bundle = bundleService.loadFirstPageBundleWithTotal(patientId, DEFAULT_PAGE_SIZE);

                final List<Observation> observations = bundle.getEntry().stream()
                    .map(entry -> (Observation) entry.getResource())
                    .collect(Collectors.toList());

                accumulatorMap.put(0, observations);

                return bundle.getTotal();

            }, executor)
            .orTimeout(handleTimeout, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                log.warn("Initial request error: {}", ex);
                if (accumulatorMap.size() == 0) {
                    accumulatorMap.put(0, Collections.emptyList());
                }
                return 0;
            })
            .thenCompose(total -> {
                if (total == null || total <= DEFAULT_PAGE_SIZE) {
                    log.debug("Only one page found for patientId: {}", patientId);
                    return CompletableFuture.completedFuture(accumulatorMap.get(0));
                }

                log.debug("More than one page found for patientId: {}", patientId);

                int pages = (total + DEFAULT_PAGE_SIZE -1)/DEFAULT_PAGE_SIZE;
                final List<CompletableFuture<Void>> pageableFutures = new ArrayList<>();

                for (int page = 1; page < pages; page++) {
                    final int currentPage = page;

                    final CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {

                        Bundle subBundle = bundleService.loadPageBundle(
                            patientId, DEFAULT_PAGE_SIZE, currentPage * DEFAULT_PAGE_SIZE
                        );

                        return subBundle.getEntry().stream()
                            .map(entry -> (Observation) entry.getResource())
                            .toList();
                    }, executor)
                    .orTimeout(handleTimeout, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.warn("Got error on inner page[{}] download: {}", currentPage, ex);
                        return Collections.emptyList();
                    })
                    .thenAccept(list -> accumulatorMap.put(currentPage, list));

                    pageableFutures.add(future);
                }

                return CompletableFuture.allOf(
                        pageableFutures.toArray(new CompletableFuture[0])
                    )
                    .thenApply(v -> accumulatorMap.entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getKey))
                        .flatMap(entry -> entry.getValue().stream())
                        .collect(Collectors.toList())
                    );
            }).exceptionally(ex -> {
                log.warn("Compose stage error: {}", ex);
                return Collections.emptyList();
            });

        if (StringUtils.hasText(csvFilePath)) {
            futureObservations.thenAcceptAsync(list -> {
                    final List<List<String>> data = list.stream()
                        .map(ObservationMapper::mapEntry)
                        .collect(Collectors.toList());

                    log.debug("CSV data size: {}", data.size());

                    csvWriterService.write(data, csvFilePath);
                }, executor)
                .exceptionally(ex -> {
                    log.warn("CSV Data saving error: {}", ex);
                    return null;
                });
        } else {
            log.debug("Skip CSV Data saving because csvFilePath is empty");
        }

        return futureObservations;
    }
}
