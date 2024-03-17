package com.snezhinskii.hapifhirdemo.service;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientObservationsServiceTest {
    @InjectMocks
    private PatientObservationsService observationsService;

    @Mock
    private PatientObservationsBundleService bundleService;

    @Mock
    private CsvWriterService csvWriterService;

    @Mock
    private Executor executor;

    @Captor
    private ArgumentCaptor<String> patientCaptor;

    @Captor
    private ArgumentCaptor<List<List<String>>> csvDataCaptor;

    private String patientId = "1111";

    @BeforeEach
    void setUp() {
        doAnswer(
            (InvocationOnMock invocation) -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        ).when(executor).execute(any(Runnable.class));
    }

    @Test
    void retrieveAndExport_loadFirstPageOk_test() throws ExecutionException, InterruptedException {
        // Preparations
        Bundle mockBundle = createBundle(0, 1, 1);

        when(bundleService.loadFirstPageBundleWithTotal(anyString(), anyInt()))
            .thenReturn(mockBundle);

        // Execution
        CompletableFuture<List<Observation>> cfo = observationsService.retrieveAndExport(patientId, null);

        // Assertions
        List<Observation> observations = cfo.get();

        verify(bundleService, times(1)).loadFirstPageBundleWithTotal(patientCaptor.capture(), anyInt());
        verify(bundleService, times(0)).loadPageBundle(any(), any(), any());

        assertEquals(1, observations.size());
        assertEquals(mockBundle.getEntry().get(0).getResource().getId(), observations.get(0).getId());
        assertEquals(patientId, patientCaptor.getValue());
    }

    @Test
    void retrieveAndExport_loadTwoPagesOk_test() throws ExecutionException, InterruptedException {
        // Preparations
        Bundle mockBundle_page1 = createBundle(0, 21, 1);
        Bundle mockBundle_page2 = createBundle(1, 1, 1);

        when(bundleService.loadFirstPageBundleWithTotal(anyString(), anyInt()))
            .thenReturn(mockBundle_page1);

        when(bundleService.loadPageBundle(anyString(), anyInt(), anyInt()))
            .thenReturn(mockBundle_page2);

        // Execution
        CompletableFuture<List<Observation>> cfo = observationsService.retrieveAndExport(patientId, null);

        // Assertions
        List<Observation> observations = cfo.get();

        verify(bundleService, times(1)).loadFirstPageBundleWithTotal(patientCaptor.capture(), anyInt());
        assertEquals(patientId, patientCaptor.getValue());

        verify(bundleService, times(1)).loadPageBundle(patientCaptor.capture(), any(), any());
        assertEquals(patientId, patientCaptor.getValue());

        assertEquals(2, observations.size());
        assertEquals(mockBundle_page1.getEntry().get(0).getResource().getId(), observations.get(0).getId());
        assertEquals(mockBundle_page2.getEntry().get(0).getResource().getId(), observations.get(1).getId());
    }

    @Test
    void retrieveAndExport_exceptionOnFirstPage_test() throws ExecutionException, InterruptedException {
        // Preparations
        when(bundleService.loadFirstPageBundleWithTotal(anyString(), anyInt()))
            .thenThrow(new RuntimeException());

        // Execution
        CompletableFuture<List<Observation>> cfo = observationsService.retrieveAndExport(patientId, null);

        // Assertions
        List<Observation> observations = cfo.get();

        verify(bundleService, times(1)).loadFirstPageBundleWithTotal(patientCaptor.capture(), anyInt());
        verify(bundleService, times(0)).loadPageBundle(any(), any(), any());

        assertEquals(0, observations.size());
        assertEquals(patientId, patientCaptor.getValue());
    }

    @Test
    void retrieveAndExport_exceptionOnSecondPage_test() throws ExecutionException, InterruptedException {
        // Preparations
        Bundle mockBundle_page1 = createBundle(0, 21, 1);

        when(bundleService.loadFirstPageBundleWithTotal(anyString(), anyInt()))
            .thenReturn(mockBundle_page1);

        when(bundleService.loadPageBundle(anyString(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException());

        // Execution
        CompletableFuture<List<Observation>> cfo = observationsService.retrieveAndExport(patientId, null);

        // Assertions
        List<Observation> observations = cfo.get();

        verify(bundleService, times(1)).loadFirstPageBundleWithTotal(patientCaptor.capture(), anyInt());
        assertEquals(patientId, patientCaptor.getValue());

        verify(bundleService, times(1)).loadPageBundle(patientCaptor.capture(), any(), any());
        assertEquals(patientId, patientCaptor.getValue());

        assertEquals(1, observations.size());
        assertEquals(mockBundle_page1.getEntry().get(0).getResource().getId(), observations.get(0).getId());
    }

    @Test
    void retrieveAndExport_exceptionOnCSVDataSave_test() throws ExecutionException, InterruptedException {
        // Preparations
        Bundle mockBundle = createBundle(0, 1, 1);

        when(bundleService.loadFirstPageBundleWithTotal(anyString(), anyInt()))
            .thenReturn(mockBundle);

        doThrow(new RuntimeException()).when(csvWriterService).write(anyList(), anyString());

        // Execution
        CompletableFuture<List<Observation>> cfo = observationsService.retrieveAndExport(patientId, "some_path");

        // Assertions
        List<Observation> observations = cfo.get();

        verify(csvWriterService, times(1)).write(csvDataCaptor.capture(), anyString());

        assertEquals(1, observations.size());
        assertEquals(csvDataCaptor.getValue().get(0).get(0), observations.get(0).getId());
    }

    private Bundle createBundle(int startId, int total, int expectedQty) {
        Bundle bundle = new Bundle();
        bundle.setTotal(total);

        for (int i = startId; i < startId + expectedQty; i++) {
            Observation o = new Observation();
            o.setId("ID_"+i);
            o.setCode(new CodeableConcept(new Coding("code", "_"+i, "")));
            o.setValue(new Quantity(i));
            o.setIssued(new Date());

            Bundle.BundleEntryComponent component = new Bundle.BundleEntryComponent();

            component.setResource(o);

            bundle.addEntry(component);
        }

        return bundle;
    }
}